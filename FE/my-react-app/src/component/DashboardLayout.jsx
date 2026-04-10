import React, { useState } from 'react';
import {
    Search,
    Bell,
    HelpCircle,
    User,
    Home,
    Users,
    Trash,
    FileText,
    LogOut,
    Hexagon,
} from 'lucide-react';
import './DashboardLayout.css';
import config from '../config/api';
import { DocumentModel } from '../model/DocumentModel';

function DashboardLayout({ children, onNavigate, onLogout, activeTab, onSearchResults }) {
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [isSearching, setIsSearching] = useState(false);

    // Gọi API khi người dùng kích hoạt tìm kiếm
    const handleSearch = async () => {
        if (!searchKeyword.trim()) {
            setSearchResults([]);
            if (onSearchResults) onSearchResults(null);
            return;
        }
        
        setIsSearching(true);
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${config.API_BASE_URL}/files/search?keyword=${encodeURIComponent(searchKeyword)}`, {
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            
            if (res.ok) {
                const data = await res.json();
                const rawList = Array.isArray(data) ? data : (data.content || []);
                const mapped = DocumentModel.fromApiResponseList(rawList);
                setSearchResults(mapped);
                if (onSearchResults) onSearchResults(mapped);
            }
        } catch (error) {
            console.error("Search API error:", error);
        } finally {
            setIsSearching(false);
        }
    };

    return (
        <div className="dashboard-root">
            {/* --- SIDEBAR CHUNG --- */}
            <aside className="sidebar">
                <div>
                    <h1 className="brand">
                        <Hexagon size={24} color="#003d73" fill="#003d73" style={{marginRight: '8px', verticalAlign: 'middle'}}/>
                        DocuManage
                    </h1>

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
                        <button type="button" className={`menu-item ${activeTab === 'home' ? 'active' : ''}`} onClick={() => onNavigate('/home')}>
                            <Home size={18} /> Home
                        </button>
                        <button type="button" className={`menu-item ${activeTab === 'shared' ? 'active' : ''}`}>
                            <Users size={18} /> Shared
                        </button>
                        <button type="button" className={`menu-item ${activeTab === 'trash' ? 'active' : ''}`}>
                            <Trash size={18} /> Trash
                        </button>
                        <button type="button" className={`menu-item ${activeTab === 'version-control' ? 'active' : ''}`} onClick={() => onNavigate('/version-control')}>
                            <FileText size={18} /> Version Control List
                        </button>
                    </nav>
                </div>

                <div className="sidebar-footer">
                    <button type="button" className="menu-item" onClick={onLogout}>
                        <LogOut size={18} /> Log Out
                    </button>
                </div>
            </aside>

            {/* --- MAIN PANEL --- */}
            <main className="main-panel">
                <header className="topbar">
                    <div className="top-nav">
                        <button type="button" className={`tab ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => onNavigate('/dashboard')}>Dashboard</button>
                        <button type="button" className={`tab ${activeTab === 'documents' ? 'active' : ''}`} onClick={() => onNavigate('/upload')}>Documents</button>
                        <button type="button" className="tab">Archives</button>
                    </div>

                    <div className="top-actions">
                        <div className="search-container">
                            <div className="search-box">
                                <Search 
                                    size={16} 
                                    color="#64748b" 
                                    style={{ cursor: 'pointer' }} 
                                    onClick={handleSearch} 
                                />
                                <input 
                                    type="text" 
                                    placeholder="Search files..." 
                                    value={searchKeyword}
                                    onChange={(e) => {
                                        setSearchKeyword(e.target.value);
                                        if (!e.target.value.trim()) {
                                            setSearchResults([]);
                                            if (onSearchResults) onSearchResults(null);
                                        }
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') handleSearch();
                                    }}
                                />
                            </div>
                        </div>

                        <button type="button" className="icon-btn"><Bell size={20} color="#64748b" /></button>
                        <button type="button" className="icon-btn"><HelpCircle size={20} color="#64748b" /></button>
                        <button type="button" className="upload-btn" onClick={() => onNavigate('/upload')}>+ Upload New</button>
                        <button type="button" className="avatar-btn"><User size={20} color="#1e293b" /></button>
                    </div>
                </header>

                {/* TOÀN BỘ NỘI DUNG CÁC TRANG SẼ ĐƯỢC HIỂN THỊ Ở ĐÂY */}
                <div className="main-content">
                    {children}
                </div>

            </main>
        </div>
    );
}

export default DashboardLayout;