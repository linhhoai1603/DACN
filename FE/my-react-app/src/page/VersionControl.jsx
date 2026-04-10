import config from '../config/api';
import { useEffect, useRef, useState } from 'react';
import { FileText, ListFilter, MoreHorizontal, X } from 'lucide-react';
import './VersionControl.css';
import DashboardLayout from "../component/DashboardLayout";
import FilePreview from "../component/FilePreview";
import "../component/FilePreview.css";

const API_BASE = config.API_BASE_URL;

const VersionControl = ({ onNavigate, onLogout }) => {
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [openMenuId, setOpenMenuId] = useState(null);
    const [sortOrder, setSortOrder] = useState('desc'); // 'desc' = mới nhất trước
    const [previewDoc, setPreviewDoc] = useState(null); // doc being previewed
    const menuRef = useRef(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        fetch(`${API_BASE}/files`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
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

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                setOpenMenuId(null);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
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

    const sortedDocuments = [...documents].sort((a, b) => {
        const dateA = new Date(a.uploadedAt || 0);
        const dateB = new Date(b.uploadedAt || 0);
        return sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
    });

    const toggleSort = () => setSortOrder((prev) => (prev === 'desc' ? 'asc' : 'desc'));

    const handleToggleMenu = (e, docId) => {
        e.stopPropagation(); // Ngăn sự kiện click lan ra thẻ <tr>
        setOpenMenuId((prev) => (prev === docId ? null : docId));
    };

    const handleView = (e, doc) => {
        e.stopPropagation();
        setOpenMenuId(null);
        setPreviewDoc(doc);
    };

    const handleUpdate = (e, doc) => {
        e.stopPropagation();
        setOpenMenuId(null);
        onNavigate('/update-document', doc);
    };

    return (
        <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="version-control">
            <div className="content-header">
                <div className="title-section">
                    <h1>Document Management System</h1>
                </div>
                <div className="action-buttons">
                    <button className="btn-export">Export Report</button>
                    <button className="btn-filter">
                        <ListFilter size={18} /> Filter Documents
                    </button>
                </div>
            </div>

            <div className="table-card">
                <div className="table-header"><h3>Active Files</h3></div>

                {loading && (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
                        Loading documents...
                    </div>
                )}
                {error && (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#ef4444' }}>
                        {error}
                    </div>
                )}
                {!loading && !error && documents.length === 0 && (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
                        No documents uploaded yet.
                    </div>
                )}

                {!loading && !error && documents.length > 0 && (
                    <div className="table-scroll-wrapper">
                    <table className="doc-table">
                        <thead>
                        <tr>
                            <th>FILENAME</th>
                            <th>VERSION</th>
                            <th>UPLOADED BY</th>
                            <th onClick={toggleSort} style={{ cursor: 'pointer', userSelect: 'none' }}>
                                UPLOAD DATE/TIME {sortOrder === 'desc' ? '↓' : '↑'}
                            </th>
                            <th>SIZE</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody ref={menuRef}>
                        {sortedDocuments.map((doc) => (
                            <tr
                                key={doc.id}
                                className="clickable-row"
                                onClick={() => onNavigate(`${VERSION_DETAIL_ROUTE}?id=${doc.id}&file=${encodeURIComponent(doc.fileName)}`)}
                            >
                                <td>
                                    <div className="file-info">
                                        <div className="file-icon"><FileText size={18} /></div>
                                        <div>
                                            <strong>{doc.fileName}</strong>
                                            <p>{doc.fileType?.toUpperCase()}</p>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <span className="status-badge completed">{doc.version}</span>
                                </td>
                                <td>{doc.uploadedBy}</td>
                                <td>{formatDate(doc.uploadedAt)}</td>
                                <td>{formatBytes(doc.fileSize)}</td>
                                <td className="action-cell">
                                    <div className="row-menu-wrapper">
                                        <button
                                            className="btn-more"
                                            title="Actions"
                                            onClick={(e) => handleToggleMenu(e, doc.id)}
                                        >
                                            <MoreHorizontal size={18} />
                                        </button>
                                        {openMenuId === doc.id && (
                                            <div className="row-dropdown">
                                                <button onClick={(e) => handleView(e, doc)}>
                                                    View
                                                </button>
                                                <button onClick={(e) => handleUpdate(e, doc)}>
                                                    Update
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                    </div>
                )}
            </div>
        </DashboardLayout>

        {/* Preview Modal */}
        {previewDoc && (() => {
            return (
                <div className="preview-overlay" onClick={() => setPreviewDoc(null)}>
                    <div className="preview-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="preview-modal-header">
                            <span className="preview-modal-title">{previewDoc.fileName}</span>
                            <div className="preview-modal-actions">
                                <a
                                    href={`${API_BASE}/files/${previewDoc.id}/download`}
                                    download={previewDoc.fileName}
                                    className="btn-download-modal"
                                >
                                    Download
                                </a>
                                <button className="btn-close-modal" onClick={() => setPreviewDoc(null)}>
                                    <X size={20} />
                                </button>
                            </div>
                        </div>
                        <div className="preview-modal-body">
                            <FilePreview doc={previewDoc} />
                        </div>
                    </div>
                </div>
            );
        })()}
        </>
    );
};

export default VersionControl;