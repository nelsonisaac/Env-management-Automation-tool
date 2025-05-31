import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    // try {
    //   const response = await axios.post('http://localhost:8080/api/auth/login', { email, password });
    //   localStorage.setItem('token', response.data);
    //   console.log(response.data);
    //   navigate('/dashboard');
    // } catch (err) {
    //   setError('Invalid credentials');
    // }
    try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials:'include',
      body: JSON.stringify({ "email": email, "password": password })
    });
    console.log(response);
    const token = await response.text();
    localStorage.setItem('token', token);
    console.log("after storage: "+token)
    navigate('/dashboard');
  } catch (err) {
    console.error('Login error:', err);
    console.log("after error: "+err)
    setError('Login failed: ' + err.message);
  }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>Login</h2>
        {error && <p className="error">{error}</p>}
        <div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="form-input"
              required
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="form-input"
              required
            />
          </div>
          <button onClick={handleSubmit} className="btn btn-primary">
            Login
          </button>
          <p className="register-link">
            Don't have an account? <a href="/register">Register</a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;