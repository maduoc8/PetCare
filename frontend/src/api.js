const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  constructor(mensaje, status, errores = null) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status
    this.errores = errores
  }
}

async function pedir(ruta, opciones = {}) {
  let respuesta
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, opciones)
  } catch {
    throw new ApiError('No se pudo conectar con la API. Revisa que el backend esté corriendo.', 0)
  }

  if (respuesta.status === 204) return null

  const datos = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new ApiError(datos?.mensaje ?? `Error ${respuesta.status}`, respuesta.status, datos?.errores ?? null)
  }
  return datos
}

export const listarDuenos = () => pedir('/api/duenos')

export const obtenerDueno = (id) => pedir(`/api/duenos/${id}`)

export const listarMascotasDeDueno = (id) => pedir(`/api/duenos/${id}/mascotas`)

export const obtenerMascota = (id) => pedir(`/api/mascotas/${id}`)

export const listarCitasDeMascota = (id) => pedir(`/api/mascotas/${id}/citas`)

export function crearCita({ fecha, motivo, diagnostico, costo, mascotaId }) {
  return pedir('/api/citas', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fecha, motivo, diagnostico, costo, mascota: { id: mascotaId } }),
  })
}

const CABECERAS_JSON = { 'Content-Type': 'application/json' }

const enviarJson = (metodo, ruta, cuerpo) =>
    pedir(ruta, { method: metodo, headers: CABECERAS_JSON, body: JSON.stringify(cuerpo) })

// Dueños
export const crearDueno = (datos) => enviarJson('POST', '/api/duenos', datos)
export const actualizarDueno = (id, datos) => enviarJson('PUT', `/api/duenos/${id}`, datos)
export const eliminarDueno = (id) => pedir(`/api/duenos/${id}`, { method: 'DELETE' })

// Mascotas
const cuerpoMascota = ({ nombre, especie, raza, fechaNacimiento, duenoId }) => ({
  nombre,
  especie,
  raza: raza || null,
  fechaNacimiento: fechaNacimiento || null,
  dueno: { id: duenoId },
})
export const crearMascota = (datos) => enviarJson('POST', '/api/mascotas', cuerpoMascota(datos))
export const actualizarMascota = (id, datos) =>
    enviarJson('PUT', `/api/mascotas/${id}`, cuerpoMascota(datos))
export const eliminarMascota = (id) => pedir(`/api/mascotas/${id}`, { method: 'DELETE' })

// Citas
export const eliminarCita = (id) => pedir(`/api/citas/${id}`, { method: 'DELETE' })
