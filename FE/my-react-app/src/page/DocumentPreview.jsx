import { useEffect, useState, useCallback } from 'react';
import { ChevronLeft, Download, ZoomIn, ZoomOut, RotateCcw, Maximize2 } from 'lucide-react';
import './DocumentPreview.css';

const API_BASE = 'http://localhost:8080';

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
  const [iframeLoading, setIframeLoading] = useState(true);

  // Fetch document metadata from backend
  useEffect(() => {
    if (!docId) {
      setError('No document ID provided.');
      setLoading(false);
      return;
    }

    setLoading(true);
    fetch(`${API_BASE}/files/${docId}`)
      .then((res) => {
        if (!res.ok) throw new Error(`Document not found (${res.status})`);
        return res.json();
      })
      .then((data) => {
        setDoc(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [docId]);

  const handleZoomIn = useCallback(() => {
    setZoom((z) => Math.min(z + ZOOM_STEP, ZOOM_MAX));
  }, []);

  const handleZoomOut = useCallback(() => {
    setZoom((z) => Math.max(z - ZOOM_STEP, ZOOM_MIN));
  }, []);

  const handleZoomReset = useCallback(() => setZoom(1.0), []);

  const handleDownload = useCallback(() => {
    if (!doc) return;
    window.open(`${API_BASE}/files/${doc.id}/download`, '_blank');
  }, [doc]);

  const handleFullscreen = useCallback(() => {
    const el = document.querySelector('.viewer-container');
    if (el && el.requestFullscreen) el.requestFullscreen();
  }, []);

  const formatBytes = (bytes) => {
    if (!bytes) return '—';
    const mb = bytes / (1024 * 1024);
    return mb < 1 ? `${(bytes / 1024).toFixed(1)} KB` : `${mb.toFixed(1)} MB`;
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return '—';
    return new Date(isoStr).toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  // Determine viewer URL
  const getViewerUrl = () => {
    if (!doc) return null;
    const ext = doc.fileType?.toLowerCase();
    const streamUrl = `${API_BASE}/files/${doc.id}/stream`;
    if (ext === 'pdf') {
      return streamUrl;
    }
    // Office files: Google Docs Viewer trỏ đến stream endpoint
    return `https://docs.google.com/viewer?embedded=true&url=${encodeURIComponent(streamUrl)}`;
  };

  const isOfficeFile = doc && ['doc', 'docx', 'xls', 'xlsx'].includes(doc.fileType?.toLowerCase());

  return (
    <div className="preview-root">
      {/* ── Sidebar ── */}
      <aside className="preview-sidebar">
        <h1 className="preview-brand">DocuManage</h1>

        {doc && (
          <div className="preview-file-info">
            <h3>Document Info</h3>
            <p className="preview-file-name">{doc.fileName}</p>
            <div className="preview-meta-row">
              <span>
                <span className={`file-type-badge ${doc.fileType}`}>
                  {doc.fileType?.toUpperCase()}
                </span>
                &nbsp;{doc.version}
              </span>
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

        <div className="preview-sidebar-footer">
          DocuManage Preview v1.0
        </div>
      </aside>

      {/* ── Main viewer ── */}
      <main className="preview-main">
        {/* Toolbar */}
        <div className="preview-toolbar">
          <div className="toolbar-left">
            <span className="toolbar-title">{doc?.fileName || 'Loading...'}</span>
            {doc && (
              <span className="toolbar-badge">{doc.version}</span>
            )}
          </div>

          {/* Zoom controls — only for PDF */}
          {doc && !isOfficeFile && (
            <div className="toolbar-center">
              <button className="zoom-btn" onClick={handleZoomOut} title="Zoom out">−</button>
              <span className="zoom-label">{Math.round(zoom * 100)}%</span>
              <button className="zoom-btn" onClick={handleZoomIn} title="Zoom in">+</button>
              <button className="zoom-btn" onClick={handleZoomReset} title="Reset zoom">
                <RotateCcw size={14} />
              </button>
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

        {/* Viewer area */}
        <div className="viewer-container">
          {loading && (
            <div className="viewer-loading">
              <div className="spinner" />
              <span>Loading document...</span>
            </div>
          )}

          {!loading && error && (
            <div className="viewer-error">
              <span>⚠ {error}</span>
              <button onClick={() => onNavigate('/version-control')}>← Back to list</button>
            </div>
          )}

          {!loading && !error && doc && (
            <>
              {iframeLoading && (
                <div className="viewer-loading" style={{ position: 'absolute', zIndex: 10 }}>
                  <div className="spinner" />
                  <span>Rendering document...</span>
                </div>
              )}
              <iframe
                key={getViewerUrl()}
                src={getViewerUrl()}
                className="viewer-iframe"
                title={doc.fileName}
                style={{
                  transform: isOfficeFile ? 'none' : `scale(${zoom})`,
                  transformOrigin: 'top center',
                  height: isOfficeFile ? '100%' : `${100 / zoom}%`,
                  width: isOfficeFile ? '100%' : `${100 / zoom}%`,
                }}
                onLoad={() => setIframeLoading(false)}
                sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
              />
              {isOfficeFile && (
                <div className="office-notice">
                  Powered by Google Docs Viewer — requires internet connection
                </div>
              )}
            </>
          )}

          {!loading && !error && !doc && (
            <div className="viewer-empty">
              <span>No document selected.</span>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default DocumentPreview;
