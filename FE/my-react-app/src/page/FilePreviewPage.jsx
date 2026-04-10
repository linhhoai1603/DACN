import { useEffect, useState } from 'react';
import FilePreview from '../component/FilePreview';
import config from '../config/api';
import '../component/FilePreview.css';
import './FilePreviewPage.css';

/**
 * Standalone full-screen preview page.
 * Opened via window.open('/file-preview?id=...&name=...&type=...&url=...')
 */
const FilePreviewPage = () => {
    const [doc, setDoc] = useState(null);

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const id = params.get('id');
        const fileName = params.get('name');
        const fileType = params.get('type');
        const url = params.get('url');

        if (id && fileName) {
            setDoc({ id, fileName, fileType, url });
            document.title = fileName;
        }
    }, []);

    if (!doc) {
        return <div className="fpp-loading">Loading preview…</div>;
    }

    return (
        <div className="fpp-root">
            <div className="fpp-header">
                <span className="fpp-title">{doc.fileName}</span>
                <a
                    href={`${config.API_BASE_URL}/files/${doc.id}/download`}
                    download={doc.fileName}
                    className="fpp-btn-download"
                >
                    Download
                </a>
            </div>
            <div className="fpp-body">
                <FilePreview doc={doc} />
            </div>
        </div>
    );
};

export default FilePreviewPage;
