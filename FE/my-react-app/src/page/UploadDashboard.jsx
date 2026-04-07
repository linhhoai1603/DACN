import { useMemo, useRef, useState } from 'react';
import './UploadDashboard.css';
import DashboardLayout from "../component/DashboardLayout";

const API_BASE = 'http://localhost:8080';

function UploadDashboard({ onLogout, onNavigate }) {
    const fileInputRef = useRef(null);
    const [isDragging, setIsDragging] = useState(false);
    const [queueItems, setQueueItems] = useState([]);

    const getIconClass = (fileName) => {
        const ext = fileName.split('.').pop()?.toLowerCase();
        if (ext === 'pdf') return 'pdf';
        if (ext === 'doc' || ext === 'docx') return 'doc';
        if (ext === 'xls' || ext === 'xlsx' || ext === 'csv') return 'xls';
        return 'doc';
    };

    const formatBytes = (bytes) => {
        if (!bytes || Number.isNaN(bytes)) {
            return '0 MB';
        }
        const mb = bytes / (1024 * 1024);
        if (mb < 0.1) {
            return `${mb.toFixed(2)} MB`;
        }
        return `${mb.toFixed(1)} MB`;
    };

    const parseSizeToMB = (sizeLabel) => {
        const value = Number.parseFloat(sizeLabel);
        return Number.isNaN(value) ? 0 : value;
    };

    const totalSizeMB = useMemo(() => {
        const total = queueItems.reduce((sum, item) => sum + parseSizeToMB(item.sizeLabel), 0);
        return total.toFixed(1);
    }, [queueItems]);

    const pushFilesToQueue = (files) => {
        if (!files || files.length === 0) return;

        const mappedFiles = Array.from(files).map((file, index) => ({
            id: `${file.name}-${file.lastModified}-${index}`,
            iconClass: getIconClass(file.name),
            name: file.name,
            sizeLabel: formatBytes(file.size),
            status: 'Waiting',
            progress: 0,
            file,
        }));

        setQueueItems((prev) => [...prev, ...mappedFiles]);
    };

    const handleBrowseClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileInputChange = (event) => {
        pushFilesToQueue(event.target.files);
        event.target.value = '';
    };

    const handleDrop = (event) => {
        event.preventDefault();
        setIsDragging(false);
        pushFilesToQueue(event.dataTransfer.files);
    };

    const handleDragOver = (event) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'copy';
        setIsDragging(true);
    };

    const handleDragLeave = (event) => {
        if (event.currentTarget.contains(event.relatedTarget)) return;
        setIsDragging(false);
    };

    const handleRemoveItem = (itemId) => {
        setQueueItems((prev) => prev.filter((item) => item.id !== itemId));
    };

    const handleClearQueue = () => {
        setQueueItems([]);
    };

    const updateItem = (id, patch) => {
        setQueueItems((prev) =>
            prev.map((item) => (item.id === id ? { ...item, ...patch } : item))
        );
    };

    const handleUploadAll = async () => {
        const pending = queueItems.filter((item) => item.status === 'Waiting' && item.file);
        if (pending.length === 0) return;

        for (const item of pending) {
            updateItem(item.id, { status: 'Uploading...', progress: 10 });

            const formData = new FormData();
            formData.append('file', item.file);
            formData.append('commitMessage', 'init file');

            try {
                const token = localStorage.getItem('token');
                const res = await fetch(`${API_BASE}/files/upload`, {
                    method: 'POST',
                    headers: token ? { Authorization: `Bearer ${token}` } : {},
                    body: formData,
                });

                if (!res.ok) {
                    const msg = await res.text();
                    throw new Error(msg || res.statusText);
                }

                updateItem(item.id, { status: 'Done', progress: 100 });
            } catch (err) {
                updateItem(item.id, { status: `Failed: ${err.message}`, progress: 0 });
            }
        }
    };

    return (
        <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="documents">
            <section className="content">
                <h2>Curation Hub</h2>
                <p className="subtitle">
                    Add new assets to your digital office. Supports high-resolution PDFs, Word manuscripts,
                    and complex Excel spreadsheets.
                </p>

                <div className="upload-grid">
                    <article
                        className={`dropzone-card ${isDragging ? 'drag-active' : ''}`}
                        onDrop={handleDrop}
                        onDragOver={handleDragOver}
                        onDragLeave={handleDragLeave}
                    >
                        <div className="upload-icon">☁</div>
                        <h3>Drop your manuscripts here</h3>
                        <p>Click to browse or drag your files into this space.</p>
                        <p className="muted">Supports PDF, DOCX, and XLSX formats.</p>
                        <div className="type-pills">
                            <span>PDF</span>
                            <span>DOCX</span>
                            <span>XLSX</span>
                        </div>
                        <input
                            ref={fileInputRef}
                            type="file"
                            className="hidden-file-input"
                            onChange={handleFileInputChange}
                            multiple
                            accept=".pdf,.doc,.docx,.xls,.xlsx,.csv"
                        />
                        <button type="button" className="btn-upload-primary" onClick={handleBrowseClick}>
                            Select Files
                        </button>
                    </article>

                    <article className="queue-card">
                        <div className="queue-head">
                            <h3>Upload Queue</h3>
                            <span className="badge">{queueItems.length} FILES</span>
                        </div>

                        {queueItems.length === 0 && (
                            <div className="queue-empty">No files yet. Drag files into the drop zone.</div>
                        )}

                        {queueItems.map((item) => (
                            <div className="queue-item" key={item.id}>
                                <span className={`file-icon ${item.iconClass}`}>{item.iconClass.toUpperCase()}</span>
                                <div className="queue-meta">
                                    <strong>{item.name}</strong>
                                    <p>{item.sizeLabel} • {item.status}</p>
                                    {item.progress > 0 && (
                                        <div className="progress-track">
                                            <span style={{ width: `${item.progress}%` }} />
                                        </div>
                                    )}
                                </div>
                                <button type="button" className="remove-btn" onClick={() => handleRemoveItem(item.id)}>
                                    x
                                </button>
                            </div>
                        ))}

                        <div className="queue-footer">
                            <p>
                                Total Size: <strong>{totalSizeMB} MB</strong>
                            </p>
                            <div className="queue-actions">
                                <button type="button" className="btn-ghost" onClick={handleClearQueue}>Clear</button>
                                <button type="button" className="btn-upload-primary" onClick={handleUploadAll}>
                                    Upload All
                                </button>
                            </div>
                        </div>
                    </article>
                </div>
            </section>
        </DashboardLayout>
    );
}

export default UploadDashboard;