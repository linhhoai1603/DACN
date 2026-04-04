import httpClient, { unwrapApiResponse } from './httpClient';

export const createDocumentApi = async (payload) => {
  const response = await httpClient.post('/api/documents', payload);
  return unwrapApiResponse(response);
};

export const getDocumentsApi = async () => {
  const response = await httpClient.get('/api/documents');
  return unwrapApiResponse(response);
};

export const getDocumentDetailApi = async (documentId) => {
  const response = await httpClient.get(`/api/documents/${documentId}`);
  return unwrapApiResponse(response);
};

export const uploadVersionApi = async ({ documentId, file, commitMessage }) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('commitMessage', commitMessage);

  const response = await httpClient.post(`/api/documents/${documentId}/versions`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return unwrapApiResponse(response);
};
