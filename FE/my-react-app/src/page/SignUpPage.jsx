import React, { useState } from 'react';
// Thêm MapPin và ChevronDown vào import
import { Shield, CloudSync, Lock, User, Phone, Eye, EyeOff, Mail, MapPin, ChevronDown } from 'lucide-react';
import './Loginpage.css';

const SignUpPage = ({ onNavigate }) => {
    const [showPass, setShowPass] = useState(false);

    return (
        <div className="outer-container">
            <header className="main-header">
                <div className="logo">DocuManage</div>
                <div className="header-right">
                    <a href="#" className="help-link">Help</a>
                    <button className="btn-signup-header" onClick={onNavigate}>Sign In</button>
                </div>
            </header>

            <div className="login-page-wrapper">
                <div className="branding-section">
                    <div className="badge-security">
                        <Shield size={16} /> Enterprise Grade Security
                    </div>
                    <h1 className="hero-title">Join the Future of<br />Data Management</h1>
                    <p className="hero-subtitle">Create your account and start streamlining your document lifecycle today.</p>
                    <div className="feature-cards-row">
                        <div className="feature-card"><CloudSync size={24} /><h3>Unlimited Storage</h3></div>
                        <div className="feature-card"><Shield size={24} /><h3>Fraud Protection</h3></div>
                    </div>
                </div>

                <div className="form-section">
                    <div className="login-form-container">
                        <h2 className="form-heading">Create Account</h2>
                        <p className="form-desc">Fill in the details below to set up your workspace.</p>

                        <form className="login-form">
                            {/* Full Name */}
                            <div className="input-group">
                                <label>Full Name</label>
                                <div className="input-wrapper">
                                    <User className="icon-left" size={18} />
                                    <input type="text" className="input-field" placeholder="John Doe" />
                                </div>
                            </div>

                            {/* Address - MỚI */}
                            <div className="input-group">
                                <label>Address</label>
                                <div className="input-wrapper">
                                    <MapPin className="icon-left" size={18} />
                                    <input type="text" className="input-field" placeholder="123 Street, New York, USA" />
                                </div>
                            </div>

                            {/* Role Selection - MỚI */}
                            <div className="input-group">
                                <label>User Role</label>
                                <div className="input-wrapper">
                                    <Shield className="icon-left" size={18} />
                                    <select className="input-field select-field">
                                        <option value="" disabled selected>Select role</option>
                                        <option value="director">DIRECTOR</option>
                                        <option value="user">USER</option>
                                    </select>
                                    <ChevronDown className="icon-right-select" size={18} />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Email Address</label>
                                <div className="input-wrapper">
                                    <Mail className="icon-left" size={18} />
                                    <input type="email" className="input-field" placeholder="j.doe@company.com" />
                                </div>
                            </div>

                            <div className="input-group">
                                <label>Password</label>
                                <div className="input-wrapper">
                                    <Lock className="icon-left" size={18} />
                                    <input
                                        type={showPass ? "text" : "password"}
                                        className="input-field"
                                        placeholder="Create a strong password"
                                    />
                                    <div className="icon-right" onClick={() => setShowPass(!showPass)}>
                                        {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </div>
                                </div>
                            </div>

                            <button type="button" className="btn-submit">Create Account</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SignUpPage;