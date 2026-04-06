import React, { useState, useEffect, useRef } from 'react';
import { Filter, Download, MoreVertical, RefreshCw, Trash2, FolderArchive, FileText } from 'lucide-react';
import DashboardLayout  from "../component/DashboardLayout";
import './DashboardPage.css';

const documentData = [
    { id: 1, name: 'q3_financial_report.pdf', size: '2.4 MB', type: 'PDF', commit: 'Update revenue projections', uploader: 'David Miller', date: 'Oct 24, 2023', time: '14:32', color: '#4f46e5' },
    { id: 2, name: 'branding_guidelines_v2.doc', size: '15.8 MB', type: 'DOCX', commit: 'Revised typography section', uploader: 'Sarah Jenkins', date: 'Oct 23, 2023', time: '09:15', color: '#7c3aed' },
    { id: 3, name: 'user_analytics_raw.csv', size: '1.2 GB', type: 'CSV', commit: 'Initial data dump for October', uploader: 'Mike Kulas', date: 'Oct 22, 2023', time: '18:45', color: '#ea580c' },
    { id: 4, name: 'Legal_Contracts_Archive', size: '42 items', type: 'ZIP', commit: 'End of year legal consolidation', uploader: 'Elena Rodriguez', date: 'Oct 20, 2023', time: '11:02', color: '#16a34a' },
];

function DashboardPage({ onNavigate, onLogout }) {
    const [openMenuId, setOpenMenuId] = useState(null);
    const menuRef = useRef(null);

    useEffect(() => {
        function handleClickOutside(event) {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpenMenuId(null);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const toggleMenu = (id) => {
        setOpenMenuId(openMenuId === id ? null : id);
    };

    return (
        <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="dashboard">
            <section className="content">
                <div className="content-header-row">
                    <div>
                        <h2>Documents</h2>
                        <p className="subtitle">Manage your enterprise assets.</p>
                    </div>
                    <div className="action-buttons">
                        <button className="btn-filter">
                            <Filter size={16} /> Filter
                        </button>
                        <button className="btn-export">
                            <Download size={16} /> Export List
                        </button>
                    </div>
                </div>

                <div className="table-container">
                    <table className="ledger-table">
                        <thead>
                        <tr>
                            <th>FILE NAME</th>
                            <th>COMMIT</th>
                            <th>UPLOADER</th>
                            <th>UPLOAD TIME</th>
                            <th>ACTIONS</th>
                        </tr>
                        </thead>
                        <tbody>
                        {documentData.map((file) => (
                            <tr key={file.id}>
                                <td className="file-cell">
                                    <div className="file-type-icon"
                                         style={{backgroundColor: `${file.color}20`, color: file.color}}>
                                        {file.type === 'ZIP' ? <FolderArchive size={20} /> : <FileText size={20} />}
                                    </div>
                                    <div>
                                        <div className="file-name-text">{file.name}</div>
                                        <div className="file-sub-text">{file.size} • {file.type}</div>
                                    </div>
                                </td>
                                <td>
                                    <div className="commit-info">
                                        <span className="dot-status"></span>
                                        {file.commit}
                                    </div>
                                </td>
                                <td>
                                    <div className="uploader-info">
                                        <div className="mini-avatar">{file.uploader.charAt(0)}</div>
                                        {file.uploader}
                                    </div>
                                </td>
                                <td>
                                    <div className="time-info">
                                        <strong>{file.date}</strong>
                                        <span>• {file.time}</span>
                                    </div>
                                </td>
                                <td className="actions-cell">
                                    <div className="action-menu-container" ref={openMenuId === file.id ? menuRef : null}>
                                        <button
                                            className="action-dot-btn"
                                            onClick={() => toggleMenu(file.id)}
                                        >
                                            <MoreVertical size={20} />
                                        </button>

                                        {openMenuId === file.id && (
                                            <div className="dropdown-menu">
                                                <button className="menu-option">
                                                    <RefreshCw size={14} /> Update
                                                </button>
                                                <button className="menu-option delete">
                                                    <Trash2 size={14} /> Delete
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
            </section>
        </DashboardLayout>
    );
}

export default DashboardPage;