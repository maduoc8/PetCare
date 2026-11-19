export const REGEX_RUT = /^\d{7,8}-[\dkK]$/
const REGEX_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const obligatorio = (valor, nombre) =>
    !valor || !String(valor).trim() ? `${nombre} es obligatorio` : null

const maximo = (valor, max, nombre) =>
    valor && String(valor).length > max ? `${nombre} no puede superar los ${max} caracteres` : null

export function validarDueno({ rut, nombre, telefono, email }) {
    const errores = {}
    if (obligatorio(rut, 'El RUT')) errores.rut = obligatorio(rut, 'El RUT')
    else if (!REGEX_RUT.test(rut)) errores.rut = 'Formato 12345678-9 (sin puntos y con guion)'

    errores.nombre = obligatorio(nombre, 'El nombre') || maximo(nombre, 100, 'El nombre')
    errores.telefono = obligatorio(telefono, 'El teléfono') || maximo(telefono, 20, 'El teléfono')

    if (obligatorio(email, 'El email')) errores.email = obligatorio(email, 'El email')
    else if (!REGEX_EMAIL.test(email)) errores.email = 'El email no tiene un formato válido'

    return limpiar(errores)
}

export function validarMascota({ nombre, especie, raza, fechaNacimiento }) {
    const errores = {}
    errores.nombre = obligatorio(nombre, 'El nombre') || maximo(nombre, 60, 'El nombre')
    errores.especie = obligatorio(especie, 'La especie') || maximo(especie, 40, 'La especie')
    errores.raza = maximo(raza, 60, 'La raza')
    if (fechaNacimiento && new Date(fechaNacimiento) > new Date()) {
        errores.fechaNacimiento = 'La fecha de nacimiento no puede ser futura'
    }
    return limpiar(errores)
}

export function validarCita({ fecha, motivo, diagnostico, costo }) {
    const errores = {}
    errores.fecha = obligatorio(fecha, 'La fecha')
    errores.motivo = obligatorio(motivo, 'El motivo') || maximo(motivo, 200, 'El motivo')
    errores.diagnostico = maximo(diagnostico, 500, 'El diagnóstico')
    if (costo === '' || costo === null || costo === undefined) errores.costo = 'El costo es obligatorio'
    else if (!Number.isInteger(Number(costo)) || Number(costo) < 0) errores.costo = 'El costo debe ser un entero mayor o igual a 0'
    return limpiar(errores)
}

// Quita las claves sin error
const limpiar = (errores) =>
    Object.fromEntries(Object.entries(errores).filter(([, v]) => v))