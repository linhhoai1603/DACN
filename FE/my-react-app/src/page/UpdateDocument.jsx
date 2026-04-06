import { useRef, useState } from 'react';
import {
    FileText, Share2, Clock, Settings, Plus,
    Upload, ChevronLeft, Info, CheckCircle
} from 'lucide-react';
import './UpdateDocument.css';

const API_BASE = 'http://localhost:8080';

const UpdateDocument = ({ onNavigate, doc }) => {
    const fileInputRef = useRef(null);
    const [isDragging, setIsDragging] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [summary, setSummary] = useState('');
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState(null);

    const handleFileSelect = (file) => {
        if (!file) return;
        setSelectedFile(file);
        setResult(null);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragging(false);
        handleFileSelect(e.dataTransfer.files?.[0]);
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = (e) => {
        if (!e.currentTarget.contains(e.relatedTarget)) setIsDragging(false);
    };

    const handleBrowse = (e) => {
        e.stopPropagation();
        fileInputRef.current?.click();
    };

    const handleInputChange = (e) => {
        handleFileSelect(e.target.files?.[0]);
        e.target.value = '';
    };

    const handleUpload = async () => {
        if (!selectedFile) return;
        setUploading(true);
        setResult(null);

        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('file', selectedFile);
        if (summary.trim()) formData.append('commitMessage', summary.trim());

        // Nếu có doc.id → PUT update version; không thì không nên gọi hàm này
        const url = `${API_BASE}/files/${doc.id}/update`;

        try {
            const res = await fetch(url, {
                method: 'PUT',
                headers: token ? { Authorization: `Bearer ${token}` } : {},
                body: formData,
            });
            if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || res.statusText);
            }
            const data = await res.json();
            setResult({ success: true, data });
            setSelectedFile(null);
            setSummary('');
        } catch (err) {
            setResult({ success: false, error: err.message });
        } finally {
            setUploading(false);
        }
    };

    const formatBytes = (bytes) => {
        if (!bytes) return '—';
        const mb = bytes / (1024 * 1024);
        return mb < 1 ? `${(bytes / 1024).toFixed(1)} KB` : `${mb.toFixed(2)} MB`;
    };

    const formatDate = (isoStr) => {
        if (!isoStr) return '—';
        return new Date(isoStr).toLocaleString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit',
        });
    };

    // Info card shows selected file if available, otherwise the doc passed from VersionControl
    const infoName = selectedFile ? selectedFile.name : (doc?.fileName || 'No file selected');
    const infoSize = selectedFile ? formatBytes(selectedFile.size) : formatBytes(doc?.fileSize);
    const infoFormat = selectedFile
        ? selectedFile.name.split('.').pop().toUpperCase()
        : (doc?.fileType?.toUpperCase() || doc?.fileName?.split('.').pop().toUpperCase() || '—');
    const infoVersion = doc?.version || '—';
    const infoModified = doc?.uploadedAt ? formatDate(doc.uploadedAt) : '—';
    const infoUploader = doc?.uploadedBy || '—';

    return (
        <div className="dashboard-container">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-brand"><h2>DocuManage</h2></div>

                <button className="btn-upload" onClick={() => onNavigate('/upload')}>
                    <Plus size={20} /> Upload Document
                </button>

                <nav className="sidebar-nav">
                    <div className="nav-item" onClick={() => onNavigate('/version-control')}>
                        <FileText size={20} /> Documents
                    </div>
                    <div className="nav-item"><Share2 size={20} /> Shared</div>
                    <div className="nav-item"><Clock size={20} /> Recent</div>
                    <div className="nav-item active"><Settings size={20} /> Update Version</div>
                </nav>

                <div className="sidebar-user">
                    <img src="https://via.placeholder.com/40" alt="avatar" />
                    <div className="user-info">
                        <strong>Admin User</strong>
                        <span>admin@ledgerpro.com</span>
                    </div>
                </div>
            </aside>

            <main className="ud-main">
                {/* Breadcrumb */}
                <div className="ud-breadcrumb">
                    <button className="ud-back" onClick={() => onNavigate('/version-control')}>
                        <ChevronLeft size={16} /> Documents
                    </button>
                    <span className="ud-sep">›</span>
                    {doc && (
                        <>
                            <span className="ud-sep">›</span>
                            <span>{doc.fileName}</span>
                        </>
                    )}
                    <span className="ud-sep">›</span>
                    <span className="ud-crumb-active">Update</span>
                </div>

                <div className="ud-body">
                    {/* Left */}
                    <div className="ud-left">
                        <h1 className="ud-title">Update Document Version</h1>
                        <p className="ud-subtitle">
                            Maintain document integrity by uploading the latest revision.
                            Previous versions will be archived automatically.
                        </p>

                        {/* Drop zone */}
                        <div
                            className={`ud-dropzone${isDragging ? ' dragging' : ''}${selectedFile ? ' has-file' : ''}`}
                            onDrop={handleDrop}
                            onDragOver={handleDragOver}
                            onDragLeave={handleDragLeave}
                        >
                            <input
                                ref={fileInputRef}
                                type="file"
                                className="ud-hidden-input"
                                accept=".pdf,.doc,.docx,.xls,.xlsx,.csv"
                                onChange={handleInputChange}
                            />

                            {selectedFile ? (
                                <div className="ud-file-preview">
                                    <FileText size={36} className="ud-file-icon" />
                                    <div>
                                        <strong>{selectedFile.name}</strong>
                                        <p>{formatBytes(selectedFile.size)}</p>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <div className="ud-drop-icon"><Upload size={28} /></div>
                                    <p className="ud-drop-label">Drop new version here or click to browse</p>
                                    <p className="ud-drop-hint">Supported formats: PDF, DOCX, XLSX (Max 50MB)</p>
                                </>
                            )}
                        </div>

                        <button className="ud-browse-btn" type="button" onClick={handleBrowse}>
                            <Upload size={16} /> Choose File
                        </button>

                        {/* Summary */}
                        <div className="ud-summary-section">
                            <label className="ud-label">Update Summary</label>
                            <textarea
                                className="ud-textarea"
                                rows={4}
                                placeholder="Briefly describe what's new in this version (e.g., 'Added Q4 revenue projections')..."
                                value={summary}
                                onChange={(e) => setSummary(e.target.value)}
                            />
                        </div>

                        {/* Feedback */}
                        {result && (
                            <div className={`ud-feedback ${result.success ? 'success' : 'error'}`}>
                                {result.success ? (
                                    <><CheckCircle size={16} /> Uploaded successfully — version <strong>v{result.data.version}</strong></>
                                ) : (
                                    <>Upload failed: {result.error}</>
                                )}
                            </div>
                        )}

                        {/* Actions */}
                        <div className="ud-actions">
                            <button
                                className="ud-btn-primary"
                                onClick={handleUpload}
                                disabled={!selectedFile || uploading}
                            >
                                <Upload size={16} />
                                {uploading ? 'Uploading...' : 'Update File'}
                            </button>
                            <button
                                className="ud-btn-cancel"
                                onClick={() => { setSelectedFile(null); setSummary(''); setResult(null); }}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>

                    {/* Right: info card */}
                    <div className="ud-right">
                        <div className="ud-info-card">
                            <div className="ud-info-header">
                                <Info size={16} /> Current Version
                            </div>

                            <div className="ud-info-row">
                                <div className="ud-info-file-icon"><FileText size={20} /></div>
                                <div>
                                    <span className="ud-info-meta-label">FILE NAME</span>
                                    <strong className="ud-info-filename">{infoName}</strong>
                                </div>
                            </div>

                            <div className="ud-info-grid">
                                <div>
                                    <span className="ud-info-meta-label">SIZE</span>
                                    <span className="ud-info-value">{infoSize}</span>
                                </div>
                                <div>
                                    <span className="ud-info-meta-label">FORMAT</span>
                                    <span className="ud-info-value">{infoFormat} ({infoVersion})</span>
                                </div>
                            </div>

                            <div className="ud-info-grid" style={{ marginBottom: '18px' }}>
                                <div>
                                    <span className="ud-info-meta-label">LAST MODIFIED</span>
                                    <span className="ud-info-value">{infoModified}</span>
                                </div>
                                <div>
                                    <span className="ud-info-meta-label">BY</span>
                                    <span className="ud-info-value">{infoUploader}</span>
                                </div>
                            </div>

                            <div className="ud-info-status">
                                <span className="ud-status-dot" />
                                Verified &amp; Secure
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default UpdateDocument;
