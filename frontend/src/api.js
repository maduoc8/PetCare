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
