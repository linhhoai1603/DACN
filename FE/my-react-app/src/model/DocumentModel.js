export class DocumentModel {
    /**
     * @param {Object} data API response mapping
     */
    constructor(data = {}) {
        this.id = data.id || null;
        this.fileName = data.fileName || 'Unknown File';
        this.publicId = data.publicId || '';
        this.url = data.url || '#';
        this.fileSize = data.fileSize || 0;
        this.uploadedBy = data.uploadedBy || 'Unknown User';
        this.uploadedAt = data.uploadedAt || null;
        this.commitMessage = data.commitMessage || 'No message';
        this.version = data.version || 'v1';
        this.fileType = data.fileType || '';
        this.previewUrl = data.previewUrl || '';
    }
}
