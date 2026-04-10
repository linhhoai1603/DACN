import { useEffect, useRef, useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import * as mammoth from 'mammoth';
import * as XLSX from 'xlsx';
import config from '../config/api';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// Use local worker bundled with react-pdf
pdfjs.GlobalWorkerOptions.workerSrc = new URL(
    'pdfjs-dist/build/pdf.worker.min.mjs',
    import.meta.url,
).toString();

const API_BASE = config.API_BASE_URL;

/* ── PDF Viewer ── */
const PdfViewer = ({ docId }) => {
    const [numPages, setNumPages] = useState(null);
    const url = `${API_BASE}/files/${docId}/stream`;
    return (
        <div className="fp-pdf-scroll">
            <Document
                file={url}
                onLoadSuccess={({ numPages }) => setNumPages(numPages)}
                loading={<div className="fp-loading">Loading PDF…</div>}
                error={<div className="fp-error">Failed to load PDF.</div>}
            >
                {Array.from({ length: numPages || 0 }, (_, i) => (
                    <Page
                        key={i + 1}
                        pageNumber={i + 1}
                        width={820}
                        renderTextLayer={true}
                        renderAnnotationLayer={true}
                    />
                ))}
            </Document>
        </div>
    );
};

/* ── DOCX Viewer ── */
const DocxViewer = ({ docId }) => {
    const [html, setHtml] = useState(null);
    const [err, setErr] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        fetch(`${API_BASE}/files/${docId}/stream`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
            .then((r) => r.arrayBuffer())
            .then((buf) => mammoth.convertToHtml({ arrayBuffer: buf }))
            .then((result) => setHtml(result.value))
            .catch(() => setErr('Failed to load document.'));
    }, [docId]);

    if (err) return <div className="fp-error">{err}</div>;
    if (!html) return <div className="fp-loading">Loading document…</div>;
    return (
        <div
            className="fp-docx-body"
            dangerouslySetInnerHTML={{ __html: html }}
        />
    );
};

/* ── XLSX Viewer ── */
const XlsxViewer = ({ docId }) => {
    const [sheets, setSheets] = useState(null); // { name, html }[]
    const [active, setActive] = useState(0);
    const [err, setErr] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        fetch(`${API_BASE}/files/${docId}/stream`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
            .then((r) => r.arrayBuffer())
            .then((buf) => {
                const wb = XLSX.read(buf, { type: 'array' });
                const parsed = wb.SheetNames.map((name) => ({
                    name,
                    html: XLSX.utils.sheet_to_html(wb.Sheets[name]),
                }));
                setSheets(parsed);
            })
            .catch(() => setErr('Failed to load spreadsheet.'));
    }, [docId]);

    if (err) return <div className="fp-error">{err}</div>;
    if (!sheets) return <div className="fp-loading">Loading spreadsheet…</div>;
    return (
        <div className="fp-xlsx-wrapper">
            {sheets.length > 1 && (
                <div className="fp-sheet-tabs">
                    {sheets.map((s, i) => (
                        <button
                            key={s.name}
                            className={`fp-sheet-tab${i === active ? ' active' : ''}`}
                            onClick={() => setActive(i)}
                        >
                            {s.name}
                        </button>
                    ))}
                </div>
            )}
            <div
                className="fp-xlsx-body"
                dangerouslySetInnerHTML={{ __html: sheets[active].html }}
            />
        </div>
    );
};

/* ── Main FilePreview ── */
const FilePreview = ({ doc }) => {
    const ext = doc.fileName?.split('.').pop()?.toLowerCase();

    if (ext === 'pdf') return <PdfViewer docId={doc.id} />;
    if (ext === 'docx') return <DocxViewer docId={doc.id} />;
    if (['xls', 'xlsx'].includes(ext)) return <XlsxViewer docId={doc.id} />;
    if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg'].includes(ext)) {
        return <img src={doc.url} alt={doc.fileName} className="fp-image" />;
    }
    // .doc, .ppt, .pptx and other unsupported — fallback to Google Docs viewer
    if (['doc', 'ppt', 'pptx'].includes(ext)) {
        const src = `https://docs.google.com/viewer?url=${encodeURIComponent(doc.url)}&embedded=true`;
        return <iframe src={src} title={doc.fileName} className="fp-iframe" />;
    }
    return (
        <div className="fp-unsupported">
            <p>Preview not available for this file type.</p>
        </div>
    );
};

export default FilePreview;
