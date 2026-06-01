import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'

import Home from './pages/Home'
import HomeNav from './components/HomeNav'
import Login from './pages/Login/Login'
import Register from './pages/Login/Register'
import DashboardNav from './components/DashboardNav'
import Dashboard from './pages/StockView/Dashboard'
import BuyStocks from './pages/StockView/BuyStocks'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/HomeNav" element={<HomeNav />} />
        <Route path="/DashboardNav" element={<DashboardNav />} />
        <Route path="/Login" element={<Login />} />
        <Route path="/Register" element={<Register />} />
        <Route path="/Dashboard" element={<Dashboard />} />
        <Route path="/BuyStocks" element={<BuyStocks />} />
      </Routes>
    </Router>
  )
}

export default App
