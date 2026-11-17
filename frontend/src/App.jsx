import { Link, Route, Routes } from 'react-router-dom'
import DuenosPage from './pages/DuenosPage'

export default function App() {
  return (
    <>
      <header className="cabecera">
        <Link to="/" className="marca">PetCare</Link>
        <span>Clínica veterinaria</span>
      </header>
      <main className="contenido">
        <Routes>
          <Route path="/" element={<DuenosPage />} />
          <Route path="*" element={<p>Página no encontrada. <Link to="/">Volver al inicio</Link></p>} />
        </Routes>
      </main>
    </>
  )
}
