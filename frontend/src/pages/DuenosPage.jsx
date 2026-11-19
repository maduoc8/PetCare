import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { actualizarDueno, crearDueno, eliminarDueno, listarDuenos } from '../api'
import Buscador from '../Buscador.jsx'
import Campo from '../Campo.jsx'
import { coincide, coincideRut } from '../busqueda'
import { iniciales } from '../formato'
import { validarDueno } from '../validaciones'

const formularioVacio = { rut: '', nombre: '', telefono: '', email: '' }

export default function DuenosPage() {
  const [duenos, setDuenos] = useState([])
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
    return listarDuenos()
        .then(setDuenos)
        .catch((e) => setError(e.message))
        .finally(() => setCargando(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  function abrirNuevo() {
    setEditandoId(null)
    setForm(formularioVacio)
    setErrores({})
    setAviso(null)
    setFormAbierto(true)
  }

  function abrirEditar(dueno) {
    setEditandoId(dueno.id)
    setForm({
      rut: dueno.rut,
      nombre: dueno.nombre,
      telefono: dueno.telefono,
      email: dueno.email,
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

    const erroresLocales = validarDueno(form)
    if (Object.keys(erroresLocales).length > 0) {
      setErrores(erroresLocales)
      return
    }

    const esEdicion = editandoId !== null
    const datos = {
      rut: form.rut.trim(),
      nombre: form.nombre.trim(),
      telefono: form.telefono.trim(),
      email: form.email.trim(),
    }

    setEnviando(true)
    setErrores({})
    try {
      if (esEdicion) await actualizarDueno(editandoId, datos)
      else await crearDueno(datos)
      cerrarFormulario()
      setAviso({ tipo: 'ok', texto: esEdicion ? 'Dueño actualizado.' : 'Dueño creado.' })
      await cargar()
    } catch (err) {
      setErrores(err.errores ?? {})
      setAviso({ tipo: 'error', texto: err.message })
    } finally {
      setEnviando(false)
    }
  }

  async function eliminar(dueno) {
    if (!window.confirm(`¿Eliminar a ${dueno.nombre}? Esta acción no se puede deshacer.`)) return
    setAviso(null)
    try {
      await eliminarDueno(dueno.id)
      setAviso({ tipo: 'ok', texto: 'Dueño eliminado.' })
      await cargar()
    } catch (err) {
      setAviso({ tipo: 'error', texto: err.message })
    }
  }

  if (cargando) return <p className="cargando">Cargando dueños...</p>
  if (error) return <p className="aviso error">{error}</p>

  const visibles = duenos.filter(
      (d) => coincide(busqueda, d.nombre, d.email, d.telefono) || coincideRut(busqueda, d.rut),
  )

  return (
      <section>
        <div className="encabezado">
          <div>
            <h2>Dueños <span className="contador">{duenos.length}</span></h2>
            <p className="subtitulo">Elige un dueño para ver sus mascotas.</p>
          </div>
          {!formAbierto && (
              <button type="button" className="boton" onClick={abrirNuevo}>
                + Nuevo dueño
              </button>
          )}
        </div>

        {aviso && <p className={`aviso ${aviso.tipo} separado`}>{aviso.texto}</p>}

        {formAbierto && (
            <form className="formulario" onSubmit={enviar} noValidate>
              <h3>{editandoId ? 'Editar dueño' : 'Nuevo dueño'}</h3>

              <Campo etiqueta="RUT" nombre="rut" error={errores.rut}>
                <input id="rut" name="rut" type="text" placeholder="12345678-9" value={form.rut} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Nombre" nombre="nombre" error={errores.nombre}>
                <input id="nombre" name="nombre" type="text" maxLength={100} value={form.nombre} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Teléfono" nombre="telefono" error={errores.telefono}>
                <input id="telefono" name="telefono" type="tel" maxLength={20} value={form.telefono} onChange={cambiar} />
              </Campo>
              <Campo etiqueta="Email" nombre="email" error={errores.email}>
                <input id="email" name="email" type="email" maxLength={100} value={form.email} onChange={cambiar} />
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

        {duenos.length > 0 && (
            <div className="barra">
              <Buscador
                  valor={busqueda}
                  onCambio={setBusqueda}
                  placeholder="Buscar por nombre, RUT, email o teléfono..."
              />
              {busqueda.trim() && (
                  <span className="resultados">{visibles.length} de {duenos.length}</span>
              )}
            </div>
        )}

        {duenos.length === 0 ? (
            <p className="vacio">No hay dueños registrados.</p>
        ) : visibles.length === 0 ? (
            <p className="vacio">Ningún dueño coincide con la búsqueda.</p>
        ) : (
            <div className="tabla">
              <table>
                <thead>
                <tr>
                  <th>Nombre</th>
                  <th>RUT</th>
                  <th>Teléfono</th>
                  <th>Email</th>
                  <th className="col-acciones"></th>
                </tr>
                </thead>
                <tbody>
                {visibles.map((dueno) => (
                    <tr key={dueno.id}>
                      <td>
                        <div className="persona">
                          <span className="avatar" aria-hidden="true">{iniciales(dueno.nombre)}</span>
                          <span className="persona-nombre">{dueno.nombre}</span>
                        </div>
                      </td>
                      <td>{dueno.rut}</td>
                      <td className="suave">{dueno.telefono}</td>
                      <td className="suave">{dueno.email}</td>
                      <td className="col-acciones">
                        <div className="acciones">
                          <Link to={`/duenos/${dueno.id}`} className="boton pequeno">Ver mascotas</Link>
                          <button type="button" className="boton secundario pequeno" onClick={() => abrirEditar(dueno)}>
                            Editar
                          </button>
                          <button type="button" className="boton peligro pequeno" onClick={() => eliminar(dueno)}>
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