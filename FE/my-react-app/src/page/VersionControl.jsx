import { useEffect, useState } from 'react';
import {
    FileText, Share2, Clock, Settings, Plus, Search,
    Bell, HelpCircle, Eye, ChevronLeft, ChevronRight, ListFilter
} from 'lucide-react';
import './VersionControl.css';
import { VERSION_DETAIL_ROUTE } from '../App.js';

const API_BASE = 'http://localhost:8080';

const VersionControl = ({ onNavigate }) => {
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch(`${API_BASE}/files`)
            .then((res) => {
                if (!res.ok) throw new Error(`Failed to load documents (${res.status})`);
                return res.json();
            })
            .then((data) => {
                setDocuments(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    const formatDate = (isoStr) => {
        if (!isoStr) return '—';
        return new Date(isoStr).toLocaleString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit',
        });
    };

    const formatBytes = (bytes) => {
        if (!bytes) return '—';
        const mb = bytes / (1024 * 1024);
        return mb < 1 ? `${(bytes / 1024).toFixed(1)} KB` : `${mb.toFixed(1)} MB`;
    };

    return (
        <div className="dashboard-container">
            <aside className="sidebar">
                <div className="sidebar-brand">
                    <h2>DocuManage</h2>
                </div>

                <button className="btn-upload" onClick={() => onNavigate('/upload')}>
                    <Plus size={20} /> Upload Document
                </button>

                <nav className="sidebar-nav">
                    <div className="nav-item active"><FileText size={20} /> Documents</div>
                    <div className="nav-item"><Share2 size={20} /> Shared</div>
                    <div className="nav-item"><Clock size={20} /> Recent</div>
                    <div className="nav-item"><Settings size={20} /> Settings</div>
                </nav>

                <div className="sidebar-user">
                    <img src="https://via.placeholder.com/40" alt="avatar" />
                    <div className="user-info">
                        <strong>Admin User</strong>
                        <span>admin@ledgerpro.com</span>
                    </div>
                </div>
            </aside>

            <main className="main-area">
                <header className="top-nav">
                    <button className="btn-back" onClick={() => onNavigate('/upload')}>
                        <ChevronLeft size={20} /> Back to Dashboard
                    </button>
                    <div className="search-bar">
                        <Search size={18} />
                        <input type="text" placeholder="Search Document..." />
                    </div>
                    <div className="top-nav-right">
                        <Bell size={20} />
                        <HelpCircle size={20} />
                        <div className="lang-select">VN <ChevronRight size={14} /></div>
                    </div>
                </header>

                <div className="content-header">
                    <div className="title-section">
                        <h1>Document Management System</h1>
                    </div>
                    <div className="action-buttons">
                        <button className="btn-export">Export Report</button>
                        <button className="btn-filter"><ListFilter size={18} />Filter Documents</button>
                    </div>
                </div>

                <div className="table-card">
                    <div className="table-header">
                        <h3>Active Files</h3>
                    </div>

                    {loading && (
                        <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
                            Loading documents...
                        </div>
                    )}

                    {error && (
                        <div style={{ padding: '40px', textAlign: 'center', color: '#ef4444' }}>
                            ⚠ {error}
                        </div>
                    )}

                    {!loading && !error && documents.length === 0 && (
                        <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
                            No documents uploaded yet.
                        </div>
                    )}

                    {!loading && !error && documents.length > 0 && (
                        <table className="doc-table">
                            <thead>
                                <tr>
                                    <th>FILENAME</th>
                                    <th>VERSION</th>
                                    <th>UPLOADED BY</th>
                                    <th>UPLOAD DATE/TIME</th>
                                    <th>SIZE</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {documents.map((doc) => (
                                    <tr
                                        key={doc.id}
                                        className="clickable-row"
                                        onClick={() => onNavigate(`${VERSION_DETAIL_ROUTE}?id=${doc.id}`)}
                                    >
                                        <td>
                                            <div className="file-info">
                                                <div className="file-icon"><FileText size={18} /></div>
                                                <div>
                                                    <strong>{doc.fileName}</strong>
                                                    <p>{doc.fileType?.toUpperCase()} • {doc.commitMessage}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span className="status-badge completed">{doc.version}</span>
                                        </td>
                                        <td>{doc.uploadedBy}</td>
                                        <td>{formatDate(doc.uploadedAt)}</td>
                                        <td>{formatBytes(doc.fileSize)}</td>
                                        <td>
                                            <button
                                                className="btn-preview-icon"
                                                title="Preview document"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onNavigate(`/preview?id=${doc.id}`)
                                                }}
                                            >
                                                <Eye size={18} className="icon-view" />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </main>
        </div>
    );
};

export default VersionControl;
