const config = {
    API_BASE_URL: process.env.NODE_ENV === 'production' 
        ? 'http://54.84.136.166:8080' 
        : 'http://localhost:8080'
};

export default config;
