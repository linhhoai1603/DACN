import React from 'react';
import { ChevronLeft, Download, Share2, User, AlertCircle } from 'lucide-react';
import './VersionControl.css';
import DashboardLayout from "../component/DashboardLayout";

const VersionDetail = ({ onNavigate, onLogout }) => {
  const params = new URLSearchParams(window.location.search);
  const fileName = params.get('file') || 'Unknown_Document.pdf';

  const versionHistory = [
    {
      version: 'v1.3',
      author: 'Alex Johnson',
      role: 'Head of Finance',
      changedAt: 'Oct 24, 2024 • 14:22',
      comment: 'Updated financial highlights for Q3 and adjusted ESG section.',
      status: 'latest',
    },
    {
      version: 'v1.2',
      author: 'Sarah Chen',
      role: 'Creative Director',
      changedAt: 'Oct 22, 2024 • 09:15',
      comment: 'Corrected typos in executive summary, brand assets updated.',
      status: 'completed',
    },
    {
      version: 'v1.1',
      author: 'Michael Brown',
      role: 'Compliance Officer',
      changedAt: 'Oct 18, 2024 • 16:45',
      comment: 'Initial draft for Q4 projections aligned with compliance standards.',
      status: 'completed',
    },
  ];

  return (
      <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="version-control">

        {/* Nút Back được dời vào trong nội dung chính */}
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
            <p>Latest version: {versionHistory[0].version} • Updated 2 hours ago</p>
          </div>
          <div className="action-buttons">
            <button className="btn-export" onClick={() => alert('Share action')}>
              <Share2 size={16} style={{ marginRight: '6px', verticalAlign: 'text-bottom' }} /> Share
            </button>
            <button className="btn-filter" onClick={() => alert('Download action')}>
              <Download size={16} /> Download
            </button>
          </div>
        </header>

        <section className="content-header" style={{ padding: '20px 30px 5px' }}>
          <div className="title-section">
            <h2 style={{ fontSize: '20px', margin: '0 0 8px 0', color: '#1e293b' }}>Version History</h2>
            <p>Tracks every file update with author, timestamp, and notes.</p>
          </div>
        </section>

        <section style={{ padding: '15px 30px 30px' }}>
          {versionHistory.map((entry, idx) => (
              <div
                  key={idx}
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
                    <h3 style={{ margin: 0, fontSize: 18, color: '#1e293b' }}>{fileName}</h3>
                    <small style={{ color: '#64748b' }}>{entry.version} • {entry.changedAt}</small>
                  </div>
                  <span
                      style={{
                        background: entry.status === 'latest' ? '#dbeafe' : '#dcfce7',
                        color: entry.status === 'latest' ? '#1d4ed8' : '#166534',
                        padding: '6px 12px',
                        borderRadius: 999,
                        fontSize: 12,
                        fontWeight: 700,
                        textTransform: 'uppercase',
                      }}
                  >
                  {entry.status}
                </span>
                </div>
                <p style={{ margin: '8px 0 2px', color: '#334155' }}>{entry.comment}</p>
                <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8, color: '#475569', fontSize: '14px' }}>
                  <User size={16} /> {entry.author} • {entry.role}
                </div>
              </div>
          ))}

          <div style={{ marginTop: 24, color: '#64748b', fontSize: 13, display: 'flex', alignItems: 'center', gap: '6px' }}>
            <AlertCircle size={16} />
            Lưu ý: bạn có thể quay lại các phiên bản trước bằng cách chọn biểu tượng version (không có trong demo tĩnh).
          </div>
        </section>

      </DashboardLayout>
  );
};

export default VersionDetail;