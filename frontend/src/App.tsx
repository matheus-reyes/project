import { Route, Routes } from 'react-router-dom'
import AnalyticsPage from './AnalyticsPage'
import HomePage from './HomePage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/analytics/:code" element={<AnalyticsPage />} />
    </Routes>
  )
}

export default App
