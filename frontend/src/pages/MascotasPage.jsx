import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { listarMascotasDeDueno, obtenerDueno } from '../api'
import { formatearFecha } from '../formato'

export default function MascotasPage() {
  const { id } = useParams()
  const [dueno, setDueno] = useState(null)
  const [mascotas, setMascotas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setCargando(true)
    setError('')
    Promise.all([obtenerDueno(id), listarMascotasDeDueno(id)])
      .then(([datosDueno, datosMascotas]) => {
        setDueno(datosDueno)
        setMascotas(datosMascotas)
      })
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false))
  }, [id])

  if (cargando) return <p>Cargando mascotas...</p>

  return (
    <section>
      <Link to="/" className="volver">← Volver a dueños</Link>

      {error ? (
        <p className="aviso error">{error}</p>
      ) : (
        <>
          <h2>Mascotas de {dueno.nombre}</h2>
          <p className="subtitulo">
            {dueno.telefono} · {dueno.email}
          </p>

          {mascotas.length === 0 ? (
            <p className="vacio">Este dueño no tiene mascotas registradas.</p>
          ) : (
            <div className="tabla">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Especie</th>
                    <th>Raza</th>
                    <th>Nacimiento</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {mascotas.map((mascota) => (
                    <tr key={mascota.id}>
                      <td>{mascota.nombre}</td>
                      <td>{mascota.especie}</td>
                      <td>{mascota.raza || '—'}</td>
                      <td>{formatearFecha(mascota.fechaNacimiento)}</td>
                      <td>
                        <Link to={`/mascotas/${mascota.id}/cita`} className="boton">
                          Registrar cita
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </section>
  )
}
