import React from 'react';
import { Shield, CloudSync, Lock, User, Phone, Eye } from 'lucide-react';
import './Loginpage.css';

const LoginPage = () => {
    return (
        <div className="outer-container">
            <header className="main-header">
                <div className="logo">DocuManage</div>
                <div className="header-right">
                    <a href="#" className="help-link">Help</a>
                    <button className="btn-signup-header">Sign Up</button>
                </div>
            </header>

            {/* Khối Login chính chia làm 2 cột */}
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

                        <form className="login-form">
                            <div className="input-group">
                                <label>Username</label>
                                <div className="input-wrapper">
                                    <User className="icon-left" size={18} />
                                    <input type="text" className="input-field" placeholder="j.doe@company.com" />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Phone Number</label>
                                <div className="input-wrapper">
                                    <Phone className="icon-left" size={18} />
                                    <input type="text" className="input-field" placeholder="+1 (555) 000-0000" />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Password</label>
                                <div className="input-wrapper">
                                    <Lock className="icon-left" size={18} />
                                    <input type="password" className="input-field" placeholder="••••••••" />
                                    <Eye className="icon-right" size={18} />
                                </div>
                            </div>

                            <button type="button" className="btn-submit">Sign In</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;