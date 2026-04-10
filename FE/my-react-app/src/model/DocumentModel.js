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

    /**
     * Map a single API response object to DocumentModel
     */
    static fromApiResponse(data) {
        return new DocumentModel(data);
    }

    /**
     * Map an array of API response objects to an array of DocumentModels
     */
    static fromApiResponseList(dataList) {
        if (!Array.isArray(dataList)) return [];
        return dataList.map(item => this.fromApiResponse(item));
    }
}
