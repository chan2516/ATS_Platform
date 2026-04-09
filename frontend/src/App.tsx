import { Link, Route, Routes } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { HealthPage } from './pages/HealthPage'
import './App.css'

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <strong>ATS Platform</strong>
        <nav>
          <Link to="/">Home</Link>
          <Link to="/health">API health</Link>
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/health" element={<HealthPage />} />
        </Routes>
      </main>
    </div>
  )
}
