import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  actualizarMascota,
  crearMascota,
  eliminarMascota,
  listarMascotasDeDueno,
  obtenerDueno,
} from '../api'
import Buscador from '../Buscador.jsx'
import Campo from '../Campo.jsx'
import { coincide } from '../busqueda'
import { formatearFecha, iniciales } from '../formato'
import { validarMascota } from '../validaciones'

const formularioVacio = { nombre: '', especie: '', raza: '', fechaNacimiento: '' }

export default function MascotasPage() {
  const { id } = useParams()
  const [dueno, setDueno] = useState(null)
  const [mascotas, setMascotas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState(null)
  const [busqueda, setBusqueda] = useState('')

  const [formAbierto, setFormAbierto] = useState(false)
  const [editandoId, setEditandoId] = useState(null)
  const [form, setForm] = useState(formularioVacio)
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)

  function cargar() {
    setError('')
    return Promise.all([obtenerDueno(id), listarMascotasDeDueno(id)])
        .then(([datosDueno, datosMascotas]) => {
          setDueno(datosDueno)
          setMascotas(datosMascotas)
        })
        .catch((e) => setError(e.message))
        .finally(() => setCargando(false))
  }

  useEffect(() => {
    setCargando(true)
    cargar()
  }, [id])

  function abrirNueva() {
    setEditandoId(null)
    setForm(formularioVacio)
    setErrores({})
    setAviso(null)
    setFormAbierto(true)
  }

  function abrirEditar(mascota) {
    setEditandoId(mascota.id)
    setForm({
      nombre: mascota.nombre,
      especie: mascota.especie,
      raza: mascota.raza ?? '',
      fechaNacimiento: mascota.fechaNacimiento ?? '',
    })
    setErrores({})
    setAviso(null)
    setFormAbierto(true)
  }

  function cerrarFormulario() {
    setFormAbierto(false)
    setEditandoId(null)
    setForm(formularioVacio)
    setErrores({})
  }

  function cambiar(e) {
    const { name, value } = e.target
    setForm((actual) => ({ ...actual, [name]: value }))
  }

  async function enviar(e) {
    e.preventDefault()
    setAviso(null)

    const erroresLocales = validarMascota(form)
    if (Object.keys(erroresLocales).length > 0) {
      setErrores(erroresLocales)
      return
    }

    const esEdicion = editandoId !== null
    const datos = {
      nombre: form.nombre.trim(),
      especie: form.especie.trim(),
      raza: form.raza.trim(),
      fechaNacimiento: form.fechaNacimiento,
      duenoId: Number(id),
    }

    setEnviando(true)
    setErrores({})
    try {
      if (esEdicion) await actualizarMascota(editandoId, datos)
      else await crearMascota(datos)
      cerrarFormulario()
      setAviso({ tipo: 'ok', texto: esEdicion ? 'Mascota actualizada.' : 'Mascota registrada.' })
      await cargar()
    } catch (err) {
      setErrores(err.errores ?? {})
      setAviso({ tipo: 'error', texto: err.message })
    } finally {
      setEnviando(false)
    }
  }

  async function eliminar(mascota) {
    if (!window.confirm(`¿Eliminar a ${mascota.nombre}? Esta acción no se puede deshacer.`)) return
    setAviso(null)
    try {
      await eliminarMascota(mascota.id)
      setAviso({ tipo: 'ok', texto: 'Mascota eliminada.' })
      await cargar()
    } catch (err) {
      setAviso({ tipo: 'error', texto: err.message })
    }
  }

  if (cargando) return <p className="cargando">Cargando mascotas...</p>

  if (error) {
    return (
        <section>
          <Link to="/" className="volver">← Volver a dueños</Link>
          <p className="aviso error">{error}</p>
        </section>
    )
  }

  const hoy = new Date().toISOString().slice(0, 10)
  const visibles = mascotas.filter((m) => coincide(busqueda, m.nombre, m.especie, m.raza))

  return (
      <section>
        <Link to="/" className="volver">← Volver a dueños</Link>

        <div className="encabezado">
          <div className="perfil">
            <span className="avatar grande" aria-hidden="true">{iniciales(dueno.nombre)}</span>
            <div>
              <h2>
                Mascotas de {dueno.nombre} <span className="contador">{mascotas.length}</span>
              </h2>
              <p className="subtitulo">
                {dueno.rut} · {dueno.telefono} · {dueno.email}
              </p>
            </div>
          </div>
          {!formAbierto && (
              <button type="button" className="boton" onClick={abrirNueva}>
                + Nueva mascota
              </button>
          )}
        </div>

        {aviso && <p className={`aviso ${aviso.tipo} separado`}>{aviso.texto}</p>}

        {formAbierto && (
            <form className="formulario" onSubmit={enviar} noValidate>
              <h3>{editandoId ? 'Editar mascota' : 'Nueva mascota'}</h3>

              <Campo etiqueta="Nombre" nombre="nombre" error={errores.nombre}>
                <input id="nombre" name="nombre" type="text" maxLength={60} value={form.nombre} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Especie" nombre="especie" error={errores.especie}>
                <input id="especie" name="especie" type="text" maxLength={40} placeholder="Perro, gato..." value={form.especie} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Raza (opcional)" nombre="raza" error={errores.raza}>
                <input id="raza" name="raza" type="text" maxLength={60} value={form.raza} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Fecha de nacimiento (opcional)" nombre="fechaNacimiento" error={errores.fechaNacimiento}>
                <input id="fechaNacimiento" name="fechaNacimiento" type="date" max={hoy} value={form.fechaNacimiento} onChange={cambiar} />
              </Campo>

              <div className="acciones">
                <button type="submit" className="boton" disabled={enviando}>
                  {enviando ? 'Guardando...' : 'Guardar'}
                </button>
                <button type="button" className="boton secundario" onClick={cerrarFormulario}>
                  Cancelar
                </button>
              </div>
            </form>
        )}

        {mascotas.length > 0 && (
            <div className="barra">
              <Buscador
                  valor={busqueda}
                  onCambio={setBusqueda}
                  placeholder="Buscar por nombre, especie o raza..."
              />
              {busqueda.trim() && (
                  <span className="resultados">{visibles.length} de {mascotas.length}</span>
              )}
            </div>
        )}
        {mascotas.length === 0 ? (
            <p className="vacio">Este dueño no tiene mascotas registradas.</p>
        ) : visibles.length === 0 ? (
            <p className="vacio">Ninguna mascota coincide con la búsqueda.</p>
        ) : (
            <div className="tabla">
              <table>
                <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Especie</th>
                  <th>Raza</th>
                  <th>Nacimiento</th>
                  <th className="col-acciones"></th>
                </tr>
                </thead>
                <tbody>
                {visibles.map((mascota) => (
                    <tr key={mascota.id}>
                      <td className="persona-nombre">{mascota.nombre}</td>
                      <td><span className="insignia">{mascota.especie}</span></td>
                      <td className="suave">{mascota.raza || '—'}</td>
                      <td className="suave">{formatearFecha(mascota.fechaNacimiento)}</td>
                      <td className="col-acciones">
                        <div className="acciones">
                          <Link to={`/mascotas/${mascota.id}/cita`} className="boton pequeno">
                            Citas
                          </Link>
                          <button type="button" className="boton secundario pequeno" onClick={() => abrirEditar(mascota)}>
                            Editar
                          </button>
                          <button type="button" className="boton peligro pequeno" onClick={() => eliminar(mascota)}>
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
      </section>
  )
}