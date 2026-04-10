import React, { useEffect, useState } from 'react';
import { ChevronLeft, Download, Share2, User, AlertCircle, Loader2 } from 'lucide-react';
import './VersionControl.css';
import DashboardLayout from "../component/DashboardLayout";

const API_BASE = 'http://localhost:8080';

const VersionDetail = ({ onNavigate, onLogout }) => {
  const params = new URLSearchParams(window.location.search);
  const documentId = params.get('id');
  const fileName = params.get('file') || 'Unknown_Document';

  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!documentId) {
      setError('Không tìm thấy document ID.');
      setLoading(false);
      return;
    }

    const token = localStorage.getItem('token');
    fetch(`${API_BASE}/files/${documentId}/versions`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(data => {
        setVersions(data);
        setLoading(false);
      })
      .catch(err => {
        setError('Không thể tải version history: ' + err.message);
        setLoading(false);
      });
  }, [documentId]);

  const latestVersion = versions.find(v => v.isLatest);

  const formatDate = (isoString) => {
    if (!isoString) return '';
    const d = new Date(isoString);
    return d.toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  return (
    <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="version-control">

      <div style={{ padding: '20px 30px 0' }}>
        <button
          className="btn-export"
          onClick={() => onNavigate('/version-control')}
          style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '0', border: 'none' }}
        >
          <ChevronLeft size={18} /> Back to Versions
        </button>
      </div>

      <header className="content-header" style={{ padding: '15px 30px 5px' }}>
        <div className="title-section">
          <h1 style={{ marginBottom: '8px' }}>{fileName}</h1>
          <p>
            {latestVersion
              ? `Latest version: ${latestVersion.version} • Updated ${formatDate(latestVersion.uploadedAt)}`
              : 'Loading...'}
          </p>
        </div>
        <div className="action-buttons">
          <button className="btn-export" onClick={() => alert('Share action')}>
            <Share2 size={16} style={{ marginRight: '6px', verticalAlign: 'text-bottom' }} /> Share
          </button>
          {latestVersion && (
            <a className="btn-filter" href={latestVersion.fileUrl} target="_blank" rel="noreferrer"
               style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', textDecoration: 'none' }}>
              <Download size={16} /> Download
            </a>
          )}
        </div>
      </header>

      <section className="content-header" style={{ padding: '20px 30px 5px' }}>
        <div className="title-section">
          <h2 style={{ fontSize: '20px', margin: '0 0 8px 0', color: '#1e293b' }}>Version History</h2>
          <p>Tracks every file update with author, timestamp, and notes.</p>
        </div>
      </section>

      <section style={{ padding: '15px 30px 30px' }}>
        {loading && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#64748b' }}>
            <Loader2 size={18} className="spin" /> Đang tải...
          </div>
        )}

        {error && (
          <div style={{ color: '#dc2626', display: 'flex', alignItems: 'center', gap: 6 }}>
            <AlertCircle size={16} /> {error}
          </div>
        )}

        {!loading && !error && versions.length === 0 && (
          <p style={{ color: '#64748b' }}>Chưa có version nào.</p>
        )}

        {versions.map((entry) => (
          <div
            key={entry.id}
            className="version-history-card"
            style={{
              background: '#fff',
              borderRadius: 12,
              border: '1px solid #e2e8f0',
              padding: 18,
              marginBottom: 14,
              boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: 18, color: '#1e293b' }}>{entry.fileName}</h3>
                <small style={{ color: '#64748b' }}>{entry.version} • {formatDate(entry.uploadedAt)}</small>
              </div>
              <span
                style={{
                  background: entry.isLatest ? '#dbeafe' : '#dcfce7',
                  color: entry.isLatest ? '#1d4ed8' : '#166534',
                  padding: '6px 12px',
                  borderRadius: 999,
                  fontSize: 12,
                  fontWeight: 700,
                  textTransform: 'uppercase',
                }}
              >
                {entry.isLatest ? 'Latest' : `v${entry.versionNumber}`}
              </span>
            </div>
            <p style={{ margin: '8px 0 2px', color: '#334155' }}>
              {entry.commitMessage || '(no commit message)'}
            </p>
            <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8, color: '#475569', fontSize: '14px' }}>
              <User size={16} /> {entry.uploadedBy}
            </div>
          </div>
        ))}
      </section>

    </DashboardLayout>
  );
};

export default VersionDetail;
