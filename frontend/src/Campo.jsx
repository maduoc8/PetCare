export default function Campo({ etiqueta, nombre, error, children }) {
    return (
        <div className={error ? 'campo invalido' : 'campo'}>
            <label htmlFor={nombre}>{etiqueta}</label>
            {children}
            {error && <span className="error-campo">{error}</span>}
        </div>
    )
}