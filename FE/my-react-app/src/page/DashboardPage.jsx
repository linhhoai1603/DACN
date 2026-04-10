import config from '../config/api';
import React, { useState, useEffect, useRef } from 'react';
import { Filter, Download, MoreVertical, RefreshCw, Trash2, FolderArchive, FileText, X } from 'lucide-react';
import DashboardLayout  from "../component/DashboardLayout";
import { DocumentModel } from "../model/DocumentModel";
import FilePreview from "../component/FilePreview";
import "../component/FilePreview.css";
import './DashboardPage.css';

const API_BASE = 'http://localhost:8080';

const formatBytes = (bytes, decimals = 1) => {
    if (!+bytes) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
};

const formatDate = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

const formatTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
};

const getFileColor = (type) => {
    if (!type) return '#64748b';
    const t = type.toLowerCase();
    if (t.includes('pdf')) return '#4f46e5';
    if (t.includes('doc')) return '#7c3aed';
    if (t.includes('csv') || t.includes('xls')) return '#ea580c';
    if (t.includes('zip') || t.includes('rar')) return '#16a34a';
    return '#64748b'; // default
};

function DashboardPage({ onNavigate, onLogout }) {
    const [documents, setDocuments] = useState([]);
    const [searchResults, setSearchResults] = useState(null);
    const [openMenuId, setOpenMenuId] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [inputPage, setInputPage] = useState(1);
    const [previewDoc, setPreviewDoc] = useState(null);
    const menuRef = useRef(null);

    // Sync inputPage when currentPage changes via buttons
    useEffect(() => {
        setInputPage(currentPage + 1);
    }, [currentPage]);

    // Fetch total document count to calculate total pages
    useEffect(() => {
        if (searchResults !== null) return; // Không gọi API count nếu đang hiển thị search
        fetch(`${config.API_BASE_URL}/files/count`)
            .then(res => res.json())
            .then(data => {
                // Handle different response formats (number, string, or object like { count: 35 })
                const totalCount = typeof data === 'number' ? data : (data.count || parseInt(data, 10));
                const calculatedPages = Math.ceil(totalCount / 10);
                setTotalPages(calculatedPages > 0 ? calculatedPages : 1);
            })
            .catch(err => console.error('Error fetching document count:', err));
    }, [searchResults]);

    useEffect(() => {
        // Nếu ô search có kết quả thì không gọi API get documents
        if (searchResults !== null) return;
        
        // Fetch files from API based on currentPage
        fetch(`${config.API_BASE_URL}/files?page=${currentPage}&index=10`)
            .then(res => res.json())
            .then(data => {
                // Extract array from paginated response or direct array
                const items = Array.isArray(data) ? data : (data.content || []);
                
                // Trích xuất thành plain object thông qua constructor của Model
                const mappedDocuments = items.map(item => ({...new DocumentModel(item)}));
                setDocuments(mappedDocuments);
            })
            .catch(err => console.error('Error fetching documents:', err));
    }, [currentPage, searchResults]);

    useEffect(() => {
        function handleClickOutside(event) {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setOpenMenuId(null);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const toggleMenu = (e, id) => {
        e.stopPropagation();
        setOpenMenuId(openMenuId === id ? null : id);
    };

    const handleView = (e, doc) => {
        e.stopPropagation();
        setOpenMenuId(null);
        setPreviewDoc(doc);
    };

    const handleUpdate = (e, doc) => {
        e.stopPropagation();
        setOpenMenuId(null);
        onNavigate('/update-document', doc);
    };

    const handlePageInputSubmit = () => {
        let pageNum = parseInt(inputPage, 10);
        if (isNaN(pageNum) || pageNum < 1) {
            pageNum = 1;
        } else if (pageNum > totalPages) {
            pageNum = totalPages;
        }
        setCurrentPage(pageNum - 1);
        setInputPage(pageNum); // Reset valid format back
    };

    const handlePageInputKeyDown = (e) => {
        if (e.key === 'Enter') {
            handlePageInputSubmit();
        }
    };

    return (
        <>
        <DashboardLayout onNavigate={onNavigate} onLogout={onLogout} activeTab="dashboard" onSearchResults={setSearchResults}>
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
                        {searchResults && searchResults.length === 0 ? (
                            <tr>
                                <td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>
                                    Không có kết quả tìm kiếm nào phù hợp.
                                </td>
                            </tr>
                        ) : (
                            (searchResults || documents).map((file) => (
                                <tr key={file.id} className="clickable-row" onClick={() => setPreviewDoc(file)}>
                                    <td className="file-cell">
                                        <div className="file-type-icon"
                                             style={{backgroundColor: `${getFileColor(file.fileType)}20`, color: getFileColor(file.fileType)}}>
                                            {file.fileType?.toUpperCase() === 'ZIP' ? <FolderArchive size={20} /> : <FileText size={20} />}
                                        </div>
                                        <div>
                                            <div className="file-name-text">{file.fileName}</div>
                                            <div className="file-sub-text">{formatBytes(file.fileSize)} • {file.fileType ? file.fileType.toUpperCase() : 'FILE'}</div>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="commit-info">
                                            <span className="dot-status"></span>
                                            {file.commitMessage}
                                        </div>
                                    </td>
                                    <td>
                                        <div className="uploader-info">
                                            <div className="mini-avatar">{file.uploadedBy ? file.uploadedBy.charAt(0).toUpperCase() : '?'}</div>
                                            {file.uploadedBy}
                                        </div>
                                    </td>
                                    <td>
                                        <div className="time-info">
                                            <strong>{formatDate(file.uploadedAt)}</strong>
                                            <span>• {formatTime(file.uploadedAt)}</span>
                                        </div>
                                    </td>
                                    <td className="actions-cell">
                                        <div className="action-menu-container" ref={openMenuId === file.id ? menuRef : null}>
                                            <button
                                                className="action-dot-btn"
                                                onClick={(e) => toggleMenu(e, file.id)}
                                            >
                                                <MoreVertical size={20} />
                                            </button>

                                            {openMenuId === file.id && (
                                                <div className="dropdown-menu">
                                                    <button className="menu-option" onClick={(e) => handleView(e, file)}>
                                                        <FileText size={14} /> View
                                                    </button>
                                                    <button className="menu-option" onClick={(e) => handleUpdate(e, file)}>
                                                        <RefreshCw size={14} /> Update
                                                    </button>
                                                    <button className="menu-option delete" onClick={(e) => e.stopPropagation()}>
                                                        <Trash2 size={14} /> Delete
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>

                {!searchResults && (
                    <div className="pagination-wrapper">
                        <button 
                            className="pagination-btn"
                            onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))} 
                            disabled={currentPage === 0}
                        >
                            &lt;
                        </button>
                        <div className="pagination-divider"></div>
                        <span className="pagination-current">{currentPage + 1}</span>
                        <div className="pagination-divider"></div>
                        <button 
                            className="pagination-btn"
                            onClick={() => setCurrentPage(prev => prev + 1)} 
                            disabled={currentPage >= totalPages - 1}
                        >
                            &gt;
                        </button>
                        
                        <div style={{ display: 'flex', alignItems: 'center', marginLeft: '20px', gap: '8px' }}>
                            <label htmlFor="pageInput" style={{ fontSize: '14px', color: '#64748b' }}>Page:</label>
                            <input 
                                id="pageInput"
                                type="number"
                                min="1"
                                step="1"
                                style={{ width: '60px', padding: '4px 8px', borderRadius: '4px', border: '1px solid #cbd5e1', textAlign: 'center' }}
                                value={inputPage}
                                onChange={(e) => {
                                    const val = e.target.value;
                                    if (val === '' || /^[1-9]\d*$/.test(val)) {
                                        setInputPage(val);
                                    }
                                }}
                                onKeyDown={handlePageInputKeyDown}
                            />
                            <button 
                                style={{ padding: '4px 12px', borderRadius: '4px', border: 'none', backgroundColor: '#3b82f6', color: 'white', cursor: 'pointer' }}
                                onClick={handlePageInputSubmit}
                            >
                                Go
                            </button>
                        </div>
                    </div>
                )}
            </section>
        </DashboardLayout>

        {previewDoc && (
            <div className="preview-overlay" onClick={() => setPreviewDoc(null)}>
                <div className="preview-modal" onClick={(e) => e.stopPropagation()}>
                    <div className="preview-modal-header">
                        <span className="preview-modal-title">{previewDoc.fileName}</span>
                        <div className="preview-modal-actions">
                            <a
                                href={`${API_BASE}/files/${previewDoc.id}/download`}
                                download={previewDoc.fileName}
                                className="btn-download-modal"
                            >
                                Download
                            </a>
                            <button className="btn-close-modal" onClick={() => setPreviewDoc(null)}>
                                <X size={20} />
                            </button>
                        </div>
                    </div>
                    <div className="preview-modal-body">
                        <FilePreview doc={previewDoc} />
                    </div>
                </div>
            </div>
        )}
        </>
    );
}

export default DashboardPage;