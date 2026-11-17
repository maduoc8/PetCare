import { Link, Route, Routes } from 'react-router-dom'

export default function App() {
  return (
    <>
      <header className="cabecera">
        <Link to="/" className="marca">PetCare</Link>
        <span>Clínica veterinaria</span>
      </header>
      <main className="contenido">
        <Routes>
          <Route path="/" element={<p>Bienvenido a PetCare.</p>} />
          <Route path="*" element={<p>Página no encontrada. <Link to="/">Volver al inicio</Link></p>} />
        </Routes>
      </main>
    </>
  )
}
