import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { listarDuenos, listarMascotas } from './api'
import Buscador from './Buscador.jsx'
import { coincide, coincideRut } from './busqueda'
import { iniciales } from './formato'

const MAX_RESULTADOS = 6

export default function BuscadorGlobal() {
  const [texto, setTexto] = useState('')
  const [abierto, setAbierto] = useState(false)
  const [datos, setDatos] = useState({ duenos: [], mascotas: [] })
  const [cargado, setCargado] = useState(false)
  const [falloCarga, setFalloCarga] = useState(false)
  const contenedor = useRef(null)
  const { pathname } = useLocation()

  // Al navegar a un resultado se cierra y limpia el buscador
  useEffect(() => {
    setAbierto(false)
    setTexto('')
  }, [pathname])

  useEffect(() => {
    function clicFuera(e) {
      if (!contenedor.current?.contains(e.target)) setAbierto(false)
    }
    document.addEventListener('mousedown', clicFuera)
    return () => document.removeEventListener('mousedown', clicFuera)
  }, [])

  // Se recarga en cada foco para reflejar altas y bajas recientes
  function cargar() {
    Promise.all([listarDuenos(), listarMascotas()])
      .then(([duenos, mascotas]) => {
        setDatos({ duenos, mascotas })
        setCargado(true)
        setFalloCarga(false)
      })
      .catch(() => setFalloCarga(true))
  }

  function teclear(e) {
    if (e.key === 'Escape') {
      setAbierto(false)
      e.currentTarget.blur()
    }
  }

  const q = texto.trim()
  const duenos = q
    ? datos.duenos
        .filter((d) => coincide(q, d.nombre, d.email) || coincideRut(q, d.rut))
        .slice(0, MAX_RESULTADOS)
    : []
  const mascotas = q
    ? datos.mascotas
        .filter((m) => coincide(q, m.nombre, m.especie, m.raza, m.dueno?.nombre))
        .slice(0, MAX_RESULTADOS)
    : []

  let contenido
  if (falloCarga) {
    contenido = <p className="resultados-vacio">No se pudo buscar. Revisa que el backend esté corriendo.</p>
  } else if (!cargado) {
    contenido = <p className="resultados-vacio">Buscando...</p>
  } else if (duenos.length === 0 && mascotas.length === 0) {
    contenido = <p className="resultados-vacio">Sin resultados para “{q}”.</p>
  } else {
    contenido = (
      <>
        {duenos.length > 0 && <p className="resultados-grupo">Dueños</p>}
        {duenos.map((d) => (
          <Link key={`d${d.id}`} to={`/duenos/${d.id}`} className="resultado">
            <span className="avatar" aria-hidden="true">{iniciales(d.nombre)}</span>
            <span>
              {d.nombre}
              <span className="resultado-detalle">{d.rut}</span>
            </span>
          </Link>
        ))}
        {mascotas.length > 0 && <p className="resultados-grupo">Mascotas</p>}
        {mascotas.map((m) => (
          <Link key={`m${m.id}`} to={`/mascotas/${m.id}/cita`} className="resultado">
            <span>
              {m.nombre}
              <span className="resultado-detalle">
                {m.especie} · {m.dueno?.nombre}
              </span>
            </span>
          </Link>
        ))}
      </>
    )
  }

  return (
    <div className="buscador-global" ref={contenedor}>
      <Buscador
        valor={texto}
        onCambio={(valor) => {
          setTexto(valor)
          setAbierto(true)
        }}
        placeholder="Buscar dueño o mascota..."
        onFocus={() => {
          setAbierto(true)
          cargar()
        }}
        onKeyDown={teclear}
      />
      {abierto && q && <div className="resultados-globales">{contenido}</div>}
    </div>
  )
}
