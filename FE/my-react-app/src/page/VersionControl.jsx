import React from 'react';
import {
    FileText, Share2, Clock, Settings, Plus, Search,
    Bell, HelpCircle, Eye, ChevronLeft, ChevronRight, ListFilter
} from 'lucide-react';
import './VersionControl.css';

const VersionControl = ({ onNavigate }) => {
    const documents = [
        { name: 'Q3_Financial_Review.pdf', size: '2.4 MB', type: 'PDF Document', status: 'COMPLETED', user: 'Alex Riviera', date: 'Oct 24, 2023' },
        { name: 'Contract_Draft_V4.docx', size: '840 KB', type: 'Word Document', status: 'PENDING', user: 'Sarah Jenkins', date: 'Oct 23, 2023' },
        { name: 'Invoice_Archive_2022.zip', size: '154.2 MB', type: 'Archive', status: 'FAILED', user: 'Mark K.', date: 'Oct 22, 2023' },
        { name: 'Blueprint_Modern_Villa.dwg', size: '12.8 MB', type: 'CAD File', status: 'COMPLETED', user: 'David Chen', date: 'Oct 21, 2023' },
    ];

    return (
        <div className="dashboard-container">
            {/* 1. SIDEBAR */}
            <aside className="sidebar">
                <div className="sidebar-brand">
                    <h2>DocuManage</h2>
                </div>

                <button className="btn-upload">
                    <Plus size={20} /> Upload Document
                </button>

                <nav className="sidebar-nav">
                    <div className="nav-item active"><FileText size={20}/> Documents</div>
                    <div className="nav-item"><Share2 size={20}/> Shared</div>
                    <div className="nav-item"><Clock size={20}/> Recent</div>
                    <div className="nav-item"><Settings size={20}/> Settings</div>
                </nav>

                <div className="sidebar-user">
                    <img src="https://via.placeholder.com/40" alt="avatar" />
                    <div className="user-info">
                        <strong>Admin User</strong>
                        <span>admin@ledgerpro.com</span>
                    </div>
                </div>
            </aside>

            {/* 2. MAIN AREA */}
            <main className="main-area">
                {/* Top Header */}
                <header className="top-nav">
                    <button className="btn-back" onClick={() => onNavigate('/dashboard')}>
                        <ChevronLeft size={20} /> Back to Dashboard
                    </button>
                    <div className="search-bar">
                        <Search size={18} />
                        <input type="text" placeholder="Search Document..." />
                    </div>
                    <div className="top-nav-right">
                        <Bell size={20} />
                        <HelpCircle size={20} />
                        <div className="lang-select">VN <ChevronRight size={14}/></div>
                    </div>
                </header>

                {/* Page Title */}
                <div className="content-header">
                    <div className="title-section">
                        <h1>Document Management System</h1>

                    </div>
                    <div className="action-buttons">
                        <button className="btn-export">Export Report</button>
                        <button className="btn-filter"><ListFilter size={18} />Filter Documents</button>
                    </div>
                </div>

                {/* Table Area */}
                <div className="table-card">
                    <div className="table-header">
                        <h3>Active Files</h3>
                    </div>

                    <table className="doc-table">
                        <thead>
                        <tr>
                            <th>FILENAME</th>
                            <th>STATUS</th>
                            <th>CREATED BY</th>
                            <th>UPLOAD DATE/TIME</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        {documents.map((doc, index) => (
                            <tr
                            key={index}
                            className="clickable-row"
                            onClick={() => onNavigate(`/version-detail?file=${encodeURIComponent(doc.name)}`)}
                        >
                            <td>
                                <div className="file-info">
                                    <div className="file-icon"><FileText size={18}/></div>
                                    <div>
                                        <strong>{doc.name}</strong>
                                        <p>{doc.size} • {doc.type}</p>
                                    </div>
                                </div>
                            </td>
                            <td><span className={`status-badge ${doc.status.toLowerCase()}`}>{doc.status}</span></td>
                            <td>{doc.user}</td>
                            <td>{doc.date}</td>
                            <td><Eye size={18} className="icon-view"/></td>
                        </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    );
};

export default VersionControl;