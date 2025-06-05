import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css';

function Home() {
    const navigate = useNavigate();

    return (
        <div className="home-container">
            <header className="home-header">
                <h1 className="home-title">Automated Environment Management Tool</h1>
            </header>
            <main className="home-content">
                <section className="hero-section">
                    <h2 className="hero-title">Streamline Your WildFly Environments</h2>
                    <p className="hero-description">
                        Simplify the management of your WildFly containers with our intuitive tool. 
                        Deploy, monitor, and manage your environments effortlessly.
                    </p>
                    <button
                        onClick={() => navigate('/login')}
                        className="btn btn-primary cta-btn"
                    >
                        Get Started
                    </button>
                </section>
                <section className="features-section">
                    <h3 className="features-title">Key Features</h3>
                    <div className="features-grid">
                        <div className="feature-card">
                            <h4>Create Environments</h4>
                            <p>Set up WildFly containers with custom ports and environment variables in just a few clicks.</p>
                        </div>
                        <div className="feature-card">
                            <h4>Deploy WAR Files</h4>
                            <p>Upload and deploy WAR files to your environments seamlessly with real-time feedback.</p>
                        </div>
                        <div className="feature-card">
                            <h4>Monitor Health</h4>
                            <p>Check the health status of your environments and ensure your applications are running smoothly.</p>
                        </div>
                        <div className="feature-card">
                            <h4>Track Deployments</h4>
                            <p>View deployment history to keep track of all changes made to your environments.</p>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
}

export default Home;