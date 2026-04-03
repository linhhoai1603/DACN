import { useMemo, useRef, useState } from 'react';
import './UploadDashboard.css';

function UploadDashboard() {
  const fileInputRef = useRef(null);
  const [isDragging, setIsDragging] = useState(false);
  const [queueItems, setQueueItems] = useState([
    {
      id: 'seed-1',
      iconClass: 'pdf',
      name: 'Annual_Report_2024_Final.pdf',
      sizeLabel: '12.4 MB',
      status: 'Uploading...',
      progress: 58,
    },
    {
      id: 'seed-2',
      iconClass: 'doc',
      name: 'Project_Brief_Editorial.docx',
      sizeLabel: '2.1 MB',
      status: 'Waiting',
      progress: 0,
    },
    {
      id: 'seed-3',
      iconClass: 'xls',
      name: 'Quarterly_Budget_Sheets.xlsx',
      sizeLabel: '4.8 MB',
      status: 'Waiting',
      progress: 0,
    },
  ]);

  const getIconClass = (fileName) => {
    const ext = fileName.split('.').pop()?.toLowerCase();
    if (ext === 'pdf') {
      return 'pdf';
    }
    if (ext === 'doc' || ext === 'docx') {
      return 'doc';
    }
    if (ext === 'xls' || ext === 'xlsx' || ext === 'csv') {
      return 'xls';
    }
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
    if (!files || files.length === 0) {
      return;
    }

    const mappedFiles = Array.from(files).map((file, index) => ({
      id: `${file.name}-${file.lastModified}-${index}`,
      iconClass: getIconClass(file.name),
      name: file.name,
      sizeLabel: formatBytes(file.size),
      status: 'Waiting',
      progress: 0,
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
    if (event.currentTarget.contains(event.relatedTarget)) {
      return;
    }
    setIsDragging(false);
  };

  const handleRemoveItem = (itemId) => {
    setQueueItems((prev) => prev.filter((item) => item.id !== itemId));
  };

  const handleClearQueue = () => {
    setQueueItems([]);
  };

  return (
    <div className="dashboard-root">
      <aside className="sidebar">
        <div>
          <h1 className="brand">DocuManage</h1>

          <section className="workspace-card">
            <span className="workspace-label">WORKSPACE</span>
            <div className="workspace-row">
              <div className="avatar">EO</div>
              <div>
                <strong>Editorial Office</strong>
                <p>Premium Tier</p>
              </div>
            </div>
          </section>

          <nav className="menu">
            <button type="button" className="menu-item">Home</button>
            <button type="button" className="menu-item active">My Files</button>
            <button type="button" className="menu-item">Shared</button>
            <button type="button" className="menu-item">Starred</button>
            <button type="button" className="menu-item">Trash</button>
          </nav>
        </div>

        <div className="sidebar-footer">
          <button type="button" className="menu-item">Security</button>
          <button type="button" className="menu-item">Log Out</button>
        </div>
      </aside>

      <main className="main-panel">
        <header className="topbar">
          <div className="top-nav">
            <button type="button" className="tab">Dashboard</button>
            <button type="button" className="tab active">Documents</button>
            <button type="button" className="tab">Archives</button>
          </div>

          <div className="top-actions">
            <div className="search-box">
              <span>⌕</span>
              <input type="text" placeholder="Search files..." />
            </div>
            <button type="button" className="icon-btn">🔔</button>
            <button type="button" className="icon-btn">?</button>
            <button type="button" className="upload-btn">+ Upload New</button>
            <button type="button" className="avatar-btn">👩🏽</button>
          </div>
        </header>

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
              <button type="button" className="primary-btn" onClick={handleBrowseClick}>
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
                  <button type="button" className="ghost-btn" onClick={handleClearQueue}>Clear</button>
                  <button type="button" className="primary-btn">Upload All</button>
                </div>
              </div>
            </article>
          </div>
        </section>
      </main>
    </div>
  );
}

export default UploadDashboard;
