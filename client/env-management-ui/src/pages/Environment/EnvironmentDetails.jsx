import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './EnvironmentDetails.css';

function EnvironmentDetails() {
  const { id } = useParams();
  const [environment, setEnvironment] = useState(null);
  const [health, setHealth] = useState('loading');
  const [deployments, setDeployments] = useState([]);
  const [file, setFile] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    let interval;
    const fetchEnvironment = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/environments/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setEnvironment(response.data);
        const healthResponse = await axios.get(`http://localhost:8080/api/environments/${id}/health`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setHealth(healthResponse.data);
        // const fetchHealth = async () => {
        //   try {
        //     const health = await axios.get(`http://localhost:8080/api/environments/${id}/health`, {
        //       headers: { Authorization: `Bearer ${token}` }
        //     });
        //     setHealth(health.data);
        //     console.log(health.data);
        //   } catch (err) {
        //     console.log("caught error: " + err)
        //     setHealth(health);
        //   }
        // }
        // await fetchHealth();
        // interval = setInterval(fetchHealth, 10000);
        const deploymentsResponse = await axios.get(`http://localhost:8080/api/environments/${id}/deployments`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setDeployments(deploymentsResponse.data);
      } catch (err) {
        console.log(err);
        navigate('/login');
      }
    };
    fetchEnvironment();
  }, [id, navigate]);

  const handleDeploy = async () => {
    try {
      const token = localStorage.getItem('token');
      const formData = new FormData();
      formData.append('file', file);
      await axios.post(`http://localhost:8080/api/environments/${id}/deploy`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      alert('WAR deployed successfully');
      const deploymentsResponse = await axios.get(`http://localhost:8080/api/environments/${id}/deployments`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setDeployments(deploymentsResponse.data);
    } catch (err) {
      alert('Deployment failed');
    }
  };

  if (!environment) return <div>Loading...</div>;

  return (
    <div className="details-container">
      <h1>Environment: {environment.name}</h1>
      <div className="details-box">
        <p>Status: {health}</p>
        <p>Port: {environment.port}</p>
        <p>Environment Variables: {environment.envVars}</p>
        <p>Created: {new Date(environment.createdAt).toLocaleString()}</p>
        <h2>Deploy WAR</h2>
        <input
          type="file"
          accept=".war"
          onChange={(e) => setFile(e.target.files[0])}
          className="file-input"
        />
        <button onClick={handleDeploy} className="btn btn-primary">
          Deploy
        </button>
      </div>
      <h2>Deployment History</h2>
      <div className="deployments-box">
        {deployments.length === 0 ? (
          <p>No deployments yet.</p>
        ) : (
          <table className="deployments-table">
            <thead>
              <tr>
                <th>WAR File</th>
                <th>Status</th>
                <th>Deployed At</th>
              </tr>
            </thead>
            <tbody>
              {deployments.map(dep => (
                <tr key={dep.id}>
                  <td>{dep.warFileName}</td>
                  <td>{dep.status}</td>
                  <td>{new Date(dep.deployedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default EnvironmentDetails;