// Pasa a minúsculas y quita tildes para que "munoz" encuentre "Muñoz"
export const normalizar = (texto) =>
  String(texto ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()

// Sin texto de búsqueda, todo coincide
export function coincide(busqueda, ...campos) {
  const q = normalizar(busqueda)
  if (!q) return true
  return campos.some((campo) => normalizar(campo).includes(q))
}

// Permite buscar el RUT con o sin puntos (12.345.678-5 o 12345678-5)
export function coincideRut(busqueda, rut) {
  const q = normalizar(busqueda).replaceAll('.', '')
  return q !== '' && normalizar(rut).includes(q)
}
