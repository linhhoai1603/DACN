import config from '../config/api';
import { useEffect, useState, useCallback, useRef } from 'react';
import { ChevronLeft, Download, RotateCcw, Maximize2 } from 'lucide-react';
import './DocumentPreview.css';

const API_BASE = config.API_BASE_URL;
const ZOOM_STEP = 0.15;
const ZOOM_MIN = 0.4;
const ZOOM_MAX = 3.0;

function DocumentPreview({ onNavigate }) {
  const params = new URLSearchParams(window.location.search);
  const docId = params.get('id');

  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [zoom, setZoom] = useState(1.0);
  const [blobUrl, setBlobUrl] = useState(null);
  const [blobLoading, setBlobLoading] = useState(false);
  const blobRef = useRef(null);

  // Fetch metadata
  useEffect(() => {
    if (!docId) { setError('No document ID provided.'); setLoading(false); return; }
    fetch(`${API_BASE}/files/${docId}`)
      .then(res => { if (!res.ok) throw new Error(`Document not found (${res.status})`); return res.json(); })
      .then(data => { setDoc(data); setLoading(false); })
      .catch(err => { setError(err.message); setLoading(false); });
  }, [docId]);

  // Fetch blob khi có doc
  useEffect(() => {
    if (!doc) return;
    const ext = doc.fileType?.toLowerCase();
    // Office files dùng Google Docs Viewer — không cần blob
    if (['doc', 'docx', 'xls', 'xlsx'].includes(ext)) return;

    setBlobLoading(true);
    fetch(`${API_BASE}/files/${doc.id}/stream`)
      .then(res => { if (!res.ok) throw new Error(`Stream failed (${res.status})`); return res.blob(); })
      .then(blob => {
        const url = URL.createObjectURL(blob);
        blobRef.current = url;
        setBlobUrl(url);
        setBlobLoading(false);
      })
      .catch(err => { setError(err.message); setBlobLoading(false); });

    return () => { if (blobRef.current) URL.revokeObjectURL(blobRef.current); };
  }, [doc]);

  const handleZoomIn = useCallback(() => setZoom(z => Math.min(z + ZOOM_STEP, ZOOM_MAX)), []);
  const handleZoomOut = useCallback(() => setZoom(z => Math.max(z - ZOOM_STEP, ZOOM_MIN)), []);
  const handleZoomReset = useCallback(() => setZoom(1.0), []);

  const handleDownload = useCallback(() => {
    if (!doc) return;
    const a = document.createElement('a');
    a.href = `${API_BASE}/files/${doc.id}/download`;
    a.download = doc.fileName;
    a.click();
  }, [doc]);

  const handleFullscreen = useCallback(() => {
    const el = document.querySelector('.viewer-container');
    if (el?.requestFullscreen) el.requestFullscreen();
  }, []);

  const formatBytes = (bytes) => {
    if (!bytes) return '—';
    const mb = bytes / (1024 * 1024);
    return mb < 1 ? `${(bytes / 1024).toFixed(1)} KB` : `${mb.toFixed(1)} MB`;
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return '—';
    return new Date(isoStr).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const isOfficeFile = doc && ['doc', 'docx', 'xls', 'xlsx'].includes(doc.fileType?.toLowerCase());
  const googleViewerUrl = doc && isOfficeFile
    ? `https://docs.google.com/viewer?embedded=true&url=${encodeURIComponent(`${API_BASE}/files/${doc.id}/stream`)}`
    : null;

  const isViewerLoading = loading || blobLoading;

  return (
    <div className="preview-root">
      <aside className="preview-sidebar">
        <h1 className="preview-brand">DocuManage</h1>
        {doc && (
          <div className="preview-file-info">
            <h3>Document Info</h3>
            <p className="preview-file-name">{doc.fileName}</p>
            <div className="preview-meta-row">
              <span><span className={`file-type-badge ${doc.fileType}`}>{doc.fileType?.toUpperCase()}</span>&nbsp;{doc.version}</span>
              <span>📁 {formatBytes(doc.fileSize)}</span>
              <span>👤 {doc.uploadedBy}</span>
              <span>🕐 {formatDate(doc.uploadedAt)}</span>
              <span>💬 {doc.commitMessage}</span>
            </div>
          </div>
        )}
        <div className="preview-actions">
          <button className="btn-action btn-download" onClick={handleDownload} disabled={!doc}>
            <Download size={16} /> Download
          </button>
          <button className="btn-action btn-back-nav" onClick={() => onNavigate('/version-control')}>
            <ChevronLeft size={16} /> Back to List
          </button>
        </div>
        <div className="preview-sidebar-footer">DocuManage Preview v1.0</div>
      </aside>

      <main className="preview-main">
        <div className="preview-toolbar">
          <div className="toolbar-left">
            <span className="toolbar-title">{doc?.fileName || 'Loading...'}</span>
            {doc && <span className="toolbar-badge">{doc.version}</span>}
          </div>
          {doc && !isOfficeFile && (
            <div className="toolbar-center">
              <button className="zoom-btn" onClick={handleZoomOut}>−</button>
              <span className="zoom-label">{Math.round(zoom * 100)}%</span>
              <button className="zoom-btn" onClick={handleZoomIn}>+</button>
              <button className="zoom-btn" onClick={handleZoomReset}><RotateCcw size={14} /></button>
            </div>
          )}
          <div className="toolbar-right">
            <button className="btn-toolbar btn-toolbar-secondary" onClick={handleFullscreen}>
              <Maximize2 size={14} /> Fullscreen
            </button>
            <button className="btn-toolbar btn-toolbar-download" onClick={handleDownload} disabled={!doc}>
              <Download size={14} /> Download
            </button>
          </div>
        </div>

        <div className="viewer-container">
          {isViewerLoading && (
            <div className="viewer-loading">
              <div className="spinner" />
              <span>{loading ? 'Loading document...' : 'Rendering document...'}</span>
            </div>
          )}

          {!loading && error && (
            <div className="viewer-error">
              <span>⚠ {error}</span>
              <button onClick={() => onNavigate('/version-control')}>← Back to list</button>
            </div>
          )}

          {/* PDF: dùng blob URL — tránh Chrome block iframe http */}
          {!loading && !error && doc && !isOfficeFile && blobUrl && (
            <iframe
              key={blobUrl}
              src={blobUrl}
              className="viewer-iframe"
              title={doc.fileName}
              style={{
                transform: `scale(${zoom})`,
                transformOrigin: 'top center',
                height: `${100 / zoom}%`,
                width: `${100 / zoom}%`,
              }}
            />
          )}

          {/* Office files: Google Docs Viewer */}
          {!loading && !error && doc && isOfficeFile && (
            <>
              <iframe
                key={googleViewerUrl}
                src={googleViewerUrl}
                className="viewer-iframe"
                title={doc.fileName}
                style={{ width: '100%', height: '100%' }}
              />
              <div className="office-notice">Powered by Google Docs Viewer — requires internet connection</div>
            </>
          )}

          {!loading && !error && !doc && (
            <div className="viewer-empty"><span>No document selected.</span></div>
          )}
        </div>
      </main>
    </div>
  );
}

export default DocumentPreview;
