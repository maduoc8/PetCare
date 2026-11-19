import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { crearCita, eliminarCita, listarCitasDeMascota, obtenerMascota } from '../api'
import { formatearFechaHora, formatearPesos } from '../formato'

const formularioVacio = { fecha: '', motivo: '', diagnostico: '', costo: '' }

function leerCosto(texto) {
  const limpio = texto.trim()
  if (limpio === '') return { valor: null }
  if (!/^(\d+|\d{1,3}(\.\d{3})+)$/.test(limpio)) {
    return { error: 'Escribe el costo en pesos, sin decimales (ej: 25000 o 25.000)' }
  }
  const valor = Number(limpio.replaceAll('.', ''))
  if (valor > 999999999) return { error: 'El costo es demasiado alto' }
  return { valor }
}

function Campo({ etiqueta, nombre, error, children }) {
  return (
    <div className={error ? 'campo invalido' : 'campo'}>
      <label htmlFor={nombre}>{etiqueta}</label>
      {children}
      {error && <span className="error-campo">{error}</span>}
    </div>
  )
}

export default function CitaPage() {
  const { id } = useParams()
  const [mascota, setMascota] = useState(null)
  const [citas, setCitas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState('')

  const [form, setForm] = useState(formularioVacio)
  const [errores, setErrores] = useState({})
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    setCargando(true)
    setErrorCarga('')
    Promise.all([obtenerMascota(id), listarCitasDeMascota(id)])
      .then(([datosMascota, datosCitas]) => {
        setMascota(datosMascota)
        setCitas(datosCitas)
      })
      .catch((e) => setErrorCarga(e.message))
      .finally(() => setCargando(false))
  }, [id])

  function cambiar(e) {
    const { name, value } = e.target
    setForm((actual) => ({ ...actual, [name]: value }))
  }

  async function enviar(e) {
    e.preventDefault()
    setAviso(null)

    const costo = leerCosto(form.costo)
    if (costo.error) {
      setErrores({ costo: costo.error })
      return
    }

    setEnviando(true)
    setErrores({})

    try {
      await crearCita({
        fecha: form.fecha || null,
        motivo: form.motivo,
        diagnostico: form.diagnostico,
        costo: costo.valor,
        mascotaId: Number(id),
      })
    } catch (err) {
      const nuevosErrores = { ...err.errores }
      if (nuevosErrores.fecha) nuevosErrores.fecha = 'Completa la fecha y la hora.'
      setErrores(nuevosErrores)
      setAviso({ tipo: 'error', texto: err.message })
      setEnviando(false)
      return
    }

    setForm(formularioVacio)
    setAviso({ tipo: 'ok', texto: 'Cita registrada correctamente.' })
    listarCitasDeMascota(id)
      .then(setCitas)
      .catch(() => {})
      .finally(() => setEnviando(false))
  }
  async function eliminar(cita) {
    if (!window.confirm('¿Eliminar esta cita? Esta acción no se puede deshacer.')) return
    setAviso(null)
    try {
      await eliminarCita(cita.id)
      setCitas((actuales) => actuales.filter((c) => c.id !== cita.id))
      setAviso({ tipo: 'ok', texto: 'Cita eliminada.' })
    } catch (err) {
      setAviso({ tipo: 'error', texto: err.message })
    }
  }

  if (cargando) return <p>Cargando mascota...</p>

  if (errorCarga) {
    return (
      <section>
        <Link to="/" className="volver">← Volver a dueños</Link>
        <p className="aviso error">{errorCarga}</p>
      </section>
    )
  }

  return (
    <section>
      <Link to={`/duenos/${mascota.dueno.id}`} className="volver">
        ← Volver a las mascotas de {mascota.dueno.nombre}
      </Link>

      <h2>Registrar cita para {mascota.nombre}</h2>
      <p className="subtitulo">
        {mascota.especie}
        {mascota.raza ? ` · ${mascota.raza}` : ''}
      </p>

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo etiqueta="Fecha y hora" nombre="fecha" error={errores.fecha}>
          <input id="fecha" name="fecha" type="datetime-local" value={form.fecha} onChange={cambiar} />
        </Campo>

        <Campo etiqueta="Motivo" nombre="motivo" error={errores.motivo}>
          <input id="motivo" name="motivo" type="text" maxLength={200} value={form.motivo} onChange={cambiar} />
        </Campo>

        <Campo etiqueta="Diagnóstico (opcional)" nombre="diagnostico" error={errores.diagnostico}>
          <textarea id="diagnostico" name="diagnostico" maxLength={500} value={form.diagnostico} onChange={cambiar} />
        </Campo>

        <Campo etiqueta="Costo (CLP)" nombre="costo" error={errores.costo}>
          <input
            id="costo"
            name="costo"
            type="text"
            inputMode="numeric"
            placeholder="25000"
            value={form.costo}
            onChange={cambiar}
          />
        </Campo>

        {aviso && <p className={`aviso ${aviso.tipo}`}>{aviso.texto}</p>}

        <button type="submit" className="boton" disabled={enviando}>
          {enviando ? 'Guardando...' : 'Registrar cita'}
        </button>
      </form>

      <h3>Citas de {mascota.nombre}</h3>
      {citas.length === 0 ? (
        <p className="vacio">Esta mascota no tiene citas registradas.</p>
      ) : (
        <div className="tabla">
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Motivo</th>
                <th>Diagnóstico</th>
                <th>Costo</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {citas.map((cita) => (
                <tr key={cita.id}>
                  <td>{formatearFechaHora(cita.fecha)}</td>
                  <td className="texto-largo">{cita.motivo}</td>
                  <td className="texto-largo">{cita.diagnostico || '—'}</td>
                  <td>{formatearPesos(cita.costo)}</td>
                  <td>
                    <button type="button" className="boton peligro pequeno" onClick={() => eliminar(cita)}>
                      Eliminar
                    </button>
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
