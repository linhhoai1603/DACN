import React, { useState } from 'react';
import { Shield, CloudSync, Lock, User, Phone, Eye } from 'lucide-react';
import './Loginpage.css';

const API_BASE = 'http://localhost:8080';

const LoginPage = ({ onSignIn, onNavigateSignUp }) => {
    const [form, setForm] = useState({ email: '', phoneNumber: '', password: '' });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const handleChange = (e) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setError('');
    };

    const handleSubmit = async () => {
        setLoading(true);
        setError('');
        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form),
            });

            const data = await res.text();

            if (!res.ok) {
                // data có thể là plain string lỗi hoặc JSON validation errors
                try {
                    const parsed = JSON.parse(data);
                    const messages = Object.values(parsed).join(', ');
                    setError(messages);
                } catch {
                    setError(data || 'Login failed');
                }
                return;
            }

            // data là JWT token
            localStorage.setItem('token', data);
            onSignIn();
        } catch {
            setError('Cannot connect to server. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="outer-container">
            <header className="main-header">
                <div className="logo">DocuManage</div>
                <div className="header-right">
                    <a href="" className="help-link">Help</a>
                    <button className="btn-signup-header" onClick={onNavigateSignUp}>
                        Sign Up
                    </button>
                </div>
            </header>

            <div className="login-page-wrapper">
            {/* Cột trái - Branding */}
                <div className="branding-section">
                    <div className="badge-security">
                        <Shield size={16} /> Enterprise Grade Security
                    </div>

                    <h1 className="hero-title">
                        Streamline Your<br />Document Lifecycle
                    </h1>

                    <p className="hero-subtitle">
                        The next generation of document management for modern enterprises.
                    </p>

                    <div className="feature-cards-row">
                        <div className="feature-card">
                            <CloudSync size={24} />
                            <h3>Real-time Sync</h3>
                        </div>
                        <div className="feature-card">
                            <Shield size={24} />
                            <h3>AES-256</h3>
                        </div>
                    </div>
                </div>

                {/* Cột phải - Form */}
                <div className="form-section">
                    <div className="login-form-container">
                        <h2 className="form-heading">Welcome Back</h2>
                        <p className="form-desc">Enter your credentials to access your workspace.</p>

                        {error && <div className="error-banner">{error}</div>}

                        <form className="login-form" onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
                            <div className="input-group">
                                <label>Username (Email)</label>
                                <div className="input-wrapper">
                                    <User className="icon-left" size={18} />
                                    <input
                                        type="text"
                                        name="email"
                                        className="input-field"
                                        placeholder="j.doe@company.com"
                                        value={form.email}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Phone Number</label>
                                <div className="input-wrapper">
                                    <Phone className="icon-left" size={18} />

                                    <input
                                        type="text"
                                        name="phoneNumber"
                                        className="input-field"
                                        placeholder="0912345678"
                                        value={form.phoneNumber}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Password</label>
                                <div className="input-wrapper">
                                    <Lock className="icon-left" size={18} />
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        name="password"
                                        className="input-field"
                                        placeholder="••••••••"
                                        value={form.password}
                                        onChange={handleChange}
                                    />
                                    <Eye
                                        className="icon-right"
                                        size={18}
                                        style={{ cursor: 'pointer' }}
                                        onClick={() => setShowPassword((v) => !v)}
                                    />
                                </div>
                            </div>

                            <button type="submit" className="btn-submit" disabled={loading}>
                                {loading ? 'Signing in...' : 'Sign In'}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;