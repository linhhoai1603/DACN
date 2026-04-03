import React from 'react';
import { ChevronLeft, Download, Share2, Clock, FileText, User, AlertCircle } from 'lucide-react';
import './VersionControl.css';

const VersionDetail = ({ onNavigate }) => {
  const params = new URLSearchParams(window.location.search);
  const fileName = params.get('file') || 'Unknown_Document.pdf';

  const versionHistory = [
    {
      version: 'v1.3',
      author: 'Alex Johnson',
      role: 'Head of Finance',
      changedAt: 'Oct 24, 2024 • 14:22',
      comment: 'Updated financial highlights for Q3 and adjusted ESG section.',
      status: 'current',
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
    <div className="dashboard-container">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <h2>DocuManage</h2>
        </div>

        <button className="btn-back" onClick={() => onNavigate('/version-control')}>
          <ChevronLeft size={18} /> Back to Versions
        </button>

        <nav className="sidebar-nav">
          <div className="nav-item active"><FileText size={20} /> Documents</div>
          <div className="nav-item"><Clock size={20} /> History</div>
          <div className="nav-item"><Share2 size={20} /> Share</div>
          <div className="nav-item"><User size={20} /> Team</div>
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
          <div className="title-meta">
            <h1>{fileName}</h1>
            <p>Latest version: {versionHistory[0].version} • Updated 2 hours ago</p>
          </div>
          <div className="top-action-group">
            <button className="btn-export" onClick={() => alert('Share action')}>
              <Share2 size={16} /> Share
            </button>
            <button className="btn-export" onClick={() => alert('Download action')}>
              <Download size={16} /> Download
            </button>
          </div>
        </header>

        <section className="content-header" style={{ padding: '20px 30px 5px' }}>
          <h2>Version History</h2>
          <p>Tracks every file update with author, timestamp, and notes.</p>
        </section>

        <section style={{ padding: '0 30px 30px' }}>
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
                  <h3 style={{ margin: 0, fontSize: 18 }}>{fileName}</h3>
                  <small style={{ color: '#64748b' }}>{entry.version} • {entry.changedAt}</small>
                </div>
                <span
                  style={{
                    background: entry.status === 'current' ? '#dbeafe' : '#dcfce7',
                    color: entry.status === 'current' ? '#1d4ed8' : '#166534',
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
              <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8, color: '#475569' }}>
                <User size={16} /> {entry.author} • {entry.role}
              </div>
            </div>
          ))}
          <div style={{ marginTop: 16, color: '#64748b', fontSize: 13 }}>
            <AlertCircle size={16} style={{ verticalAlign: 'middle', marginRight: 6 }} />
            Lưu ý: bạn có thể quay lại các phiên bản trước bằng cách chọn biểu tượng version (không có trong demo tĩnh).
          </div>
        </section>
      </main>
    </div>
  );
};

export default VersionDetail;
