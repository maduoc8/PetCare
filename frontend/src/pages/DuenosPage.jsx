import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarDuenos } from '../api'

export default function DuenosPage() {
  const [duenos, setDuenos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    listarDuenos()
      .then(setDuenos)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false))
  }, [])

  if (cargando) return <p>Cargando dueños...</p>
  if (error) return <p className="aviso error">{error}</p>

  return (
    <section>
      <h2>Dueños</h2>
      <p className="subtitulo">Elige un dueño para ver sus mascotas.</p>

      {duenos.length === 0 ? (
        <p className="vacio">No hay dueños registrados.</p>
      ) : (
        <div className="tabla">
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>RUT</th>
                <th>Teléfono</th>
                <th>Email</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {duenos.map((dueno) => (
                <tr key={dueno.id}>
                  <td>{dueno.nombre}</td>
                  <td>{dueno.rut}</td>
                  <td>{dueno.telefono}</td>
                  <td>{dueno.email}</td>
                  <td>
                    <Link to={`/duenos/${dueno.id}`}>Ver mascotas</Link>
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
