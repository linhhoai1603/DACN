import React, { useState } from 'react';
import { Shield, CloudSync, Lock, User, Phone, Eye, EyeOff, Mail, MapPin, ChevronDown } from 'lucide-react';
import './Loginpage.css';

const API_BASE = 'http://localhost:8080';

const SignUpPage = ({ onNavigate }) => {
    const [form, setForm] = useState({
        fullName: '',
        address: '',
        role: '',
        email: '',
        phone: '',
        password: '',
    });
    const [showPass, setShowPass] = useState(false);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const handleChange = (e) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setErrors((prev) => ({ ...prev, [e.target.name]: undefined }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setErrors({});

        try {
            const res = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form),
            });

            const data = await res.json();

            if (!res.ok) {
                // data là map field -> message từ BE
                setErrors(typeof data === 'object' ? data : { general: 'Đăng ký thất bại' });
                return;
            }

            // Thành công
            setSuccess(true);
            setTimeout(() => onNavigate(), 3000);
        } catch {
            setErrors({ general: 'Không thể kết nối đến server. Vui lòng thử lại.' });
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

                        {success && (
                            <div className="success-banner">
                                Tạo tài khoản thành công! Đang chuyển đến trang đăng nhập...
                            </div>
                        )}

                        {errors.general && <div className="error-banner">{errors.general}</div>}

                        <form className="login-form" onSubmit={handleSubmit}>
                            {/* Full Name */}
                            <div className="input-group">
                                <label>Full Name</label>
                                <div className="input-wrapper">
                                    <User className="icon-left" size={18} />
                                    <input
                                        type="text"
                                        name="fullName"
                                        className="input-field"
                                        placeholder="Nguyễn Văn A"
                                        value={form.fullName}
                                        onChange={handleChange}
                                    />
                                </div>
                                {errors.fullName && <span className="field-error">{errors.fullName}</span>}
                            </div>

                            {/* Address */}
                            <div className="input-group">
                                <label>Address</label>
                                <div className="input-wrapper">
                                    <MapPin className="icon-left" size={18} />
                                    <input
                                        type="text"
                                        name="address"
                                        className="input-field"
                                        placeholder="123 Đường ABC, TP.HCM"
                                        value={form.address}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            {/* Role */}
                            <div className="input-group">
                                <label>User Role</label>
                                <div className="input-wrapper">
                                    <Shield className="icon-left" size={18} />
                                    <select
                                        name="role"
                                        className="input-field select-field"
                                        value={form.role}
                                        onChange={handleChange}
                                    >
                                        <option value="" disabled>Select role</option>
                                        <option value="DIRECTOR">DIRECTOR</option>
                                        <option value="USER">USER</option>
                                    </select>
                                    <ChevronDown className="icon-right-select" size={18} />
                                </div>
                                {errors.role && <span className="field-error">{errors.role}</span>}
                            </div>

                            {/* Email */}
                            <div className="input-group">
                                <label>Email Address</label>
                                <div className="input-wrapper">
                                    <Mail className="icon-left" size={18} />
                                    <input
                                        type="email"
                                        name="email"
                                        className="input-field"
                                        placeholder="example@company.com"
                                        value={form.email}
                                        onChange={handleChange}
                                    />
                                </div>
                                {errors.email && <span className="field-error">{errors.email}</span>}
                            </div>

                            {/* Phone */}
                            <div className="input-group">
                                <label>Phone Number</label>
                                <div className="input-wrapper">
                                    <Phone className="icon-left" size={18} />
                                    <input
                                        type="text"
                                        name="phone"
                                        className="input-field"
                                        placeholder="0912345678"
                                        value={form.phone}
                                        onChange={handleChange}
                                    />
                                </div>
                                {errors.phone && <span className="field-error">{errors.phone}</span>}
                            </div>

                            {/* Password */}
                            <div className="input-group">
                                <label>Password</label>
                                <div className="input-wrapper">
                                    <Lock className="icon-left" size={18} />
                                    <input
                                        type={showPass ? 'text' : 'password'}
                                        name="password"
                                        className="input-field"
                                        placeholder="Tối thiểu 6 ký tự"
                                        value={form.password}
                                        onChange={handleChange}
                                    />
                                    <div className="icon-right" onClick={() => setShowPass(!showPass)}>
                                        {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </div>
                                </div>
                                {errors.password && <span className="field-error">{errors.password}</span>}
                            </div>

                            <button type="submit" className="btn-submit" disabled={loading || success}>
                                {loading ? 'Đang tạo tài khoản...' : 'Create Account'}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SignUpPage;
