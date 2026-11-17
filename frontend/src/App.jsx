import { Link, Route, Routes } from 'react-router-dom'
import DuenosPage from './pages/DuenosPage'
import MascotasPage from './pages/MascotasPage'
import CitaPage from './pages/CitaPage'

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
          <Route path="/duenos/:id" element={<MascotasPage />} />
          <Route path="/mascotas/:id/cita" element={<CitaPage />} />
          <Route path="*" element={<p>Página no encontrada. <Link to="/">Volver al inicio</Link></p>} />
        </Routes>
      </main>
    </>
  )
}
