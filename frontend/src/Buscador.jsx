export default function Buscador({ valor, onCambio, placeholder, etiqueta, ...resto }) {
  return (
    <div className="buscador">
      <svg
        className="buscador-icono"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
      </svg>
      <input
        type="search"
        aria-label={etiqueta ?? placeholder}
        placeholder={placeholder}
        value={valor}
        onChange={(e) => onCambio(e.target.value)}
        {...resto}
      />
      {valor && (
        <button type="button" className="buscador-limpiar" aria-label="Limpiar búsqueda" onClick={() => onCambio('')}>
          ×
        </button>
      )}
    </div>
  )
}
