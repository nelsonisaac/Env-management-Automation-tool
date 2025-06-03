import { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

function Dashboard() {
  const [environments, setEnvironments] = useState([]);
  const [name, setName] = useState('');
  const [port, setPort] = useState(8081);
  const [envVars, setEnvVars] = useState(['JAVA_OPTS=-Xms64m -Xmx512m']);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEnvironments = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/environments', {
          headers: { Authorization: `Bearer ${token}` }
        });
        console.log("dashboard: " + response.data)
        setEnvironments(response.data);
      } catch (err) {
        console.log("in dashboard error: " + err);
        navigate('/login');
      }
    };
    fetchEnvironments();
  }, [navigate]);

  const handleCreate = async () => {
    try {
      const token = localStorage.getItem('token');
      await axios.post('http://localhost:8080/api/environments', { name, port, envVars }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const response = await axios.get('http://localhost:8080/api/environments', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setEnvironments(response.data);
    } catch (err) {
      console.error('Environment creation failed');
    }
  };

  const handleDelete = async (id) => {
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8080/api/environments/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setEnvironments(environments.filter(env => env.id !== id));
    } catch (err) {
      console.error('Environment deletion failed');
    }
  };

  return (
    <div className="dashboard-container">
      <h1>Environment Management Dashboard</h1>
      <div className="create-section">
        <h2>Create Environment</h2>
        <div className="create-box">
          <input
            type="text"
            placeholder="Environment Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="form-input"
          />
          <input
            type="number"
            placeholder="Port"
            value={port}
            onChange={(e) => setPort(Number(e.target.value))}
            className="form-input"
          />
          <input
            type="text"
            placeholder="Environment Variables (comma-separated)"
            value={envVars.join(',')}
            onChange={(e) => setEnvVars(e.target.value.split(','))}
            className="form-input"
          />
          <button onClick={handleCreate} className="btn btn-success">
            Create
          </button>
        </div>
      </div>
      <h2>Environments</h2>
      <div className="environment-grid">
        {environments.map(env => (
          <div key={env.id} className="environment-card">
            <h3>{env.name}</h3>
            <p>Status: {env.status}</p>
            <p>Port: {env.port}</p>
            <p>Created: {new Date(env.createdAt).toLocaleString()}</p>
            <div className="card-actions">
              <button
                onClick={() => navigate(`/environment/${env.id}`)}
                className="btn btn-primary"
              >
                Details
              </button>
              <button
                onClick={() => handleDelete(env.id)}
                className="btn btn-danger"
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Dashboard;