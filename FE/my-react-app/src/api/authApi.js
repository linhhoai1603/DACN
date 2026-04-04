import httpClient, { unwrapApiResponse } from './httpClient';

export const registerApi = async (payload) => {
	const response = await httpClient.post('/api/auth/register', payload);
	return unwrapApiResponse(response);
};

export const loginApi = async (payload) => {
	const response = await httpClient.post('/api/auth/login', payload);
	return unwrapApiResponse(response);
};
