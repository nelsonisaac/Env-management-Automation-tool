import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './pages/Login/Login'
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import EnvironmentDetails from './pages/Environment/EnvironmentDetails';

function App() {
  return (
    <Router>
      <div>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/environment/:id" element={<EnvironmentDetails />} />
          <Route path="/" element={<Login />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;