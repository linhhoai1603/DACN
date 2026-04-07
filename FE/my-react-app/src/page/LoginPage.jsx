import React, { useState } from 'react';
import { Shield, CloudSync, Lock, User, Phone, Eye, EyeOff } from 'lucide-react';
import './Loginpage.css';

const LoginPage = ({ onSignIn, onNavigateSignUp }) => {
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSignIn = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, phoneNumber, password }),
            });

            if (!response.ok) {
                const message = await response.text();
                setError(message || 'Đăng nhập thất bại');
                return;
            }

            const token = await response.text();
            localStorage.setItem('token', token);
            onSignIn();
        } catch (err) {
            setError('Không thể kết nối đến server');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="outer-container">
            <header className="main-header">
                <div className="logo">DocuManage</div>
                <div className="header-right">
                    <a href="#" className="help-link">Help</a>
                    <button className="btn-signup-header" onClick={onNavigateSignUp}>Sign Up</button>
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

                        {error && <p className="error-message">{error}</p>}

                        <form className="login-form" onSubmit={handleSignIn}>
                            <div className="input-group">
                                <label>Email</label>
                                <div className="input-wrapper">
                                    <User className="icon-left" size={18} />
                                    <input
                                        type="email"
                                        className="input-field"
                                        placeholder="j.doe@company.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Phone Number</label>
                                <div className="input-wrapper">
                                    <Phone className="icon-left" size={18} />
                                    <input
                                        type="text"
                                        className="input-field"
                                        placeholder="0912345678"
                                        value={phoneNumber}
                                        onChange={(e) => setPhoneNumber(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Password</label>
                                <div className="input-wrapper">
                                    <Lock className="icon-left" size={18} />
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        className="input-field"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                    />
                                    <button
                                        type="button"
                                        className="icon-right icon-btn"
                                        onClick={() => setShowPassword(!showPassword)}
                                        aria-label="Toggle password visibility"
                                    >
                                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            <button type="submit" className="btn-submit" disabled={loading}>
                                {loading ? 'Signing in...' : 'Sign In'}
                            </button>
                        </form>

                        <p className="form-desc" style={{ marginTop: '16px', textAlign: 'center' }}>
                            Chưa có tài khoản?{' '}
                            <button type="button" className="link-btn" onClick={onNavigateSignUp}>
                                Sign Up
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;