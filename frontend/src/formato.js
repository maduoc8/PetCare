export function formatearFecha(fecha) {
  if (!fecha) return '—'
  const [anio, mes, dia] = fecha.slice(0, 10).split('-')
  return `${dia}-${mes}-${anio}`
}

export function formatearFechaHora(fechaHora) {
  if (!fechaHora) return '—'
  return `${formatearFecha(fechaHora)} ${fechaHora.slice(11, 16)}`
}

export function formatearPesos(monto) {
  return `$${Number(monto).toLocaleString('es-CL')}`
}
