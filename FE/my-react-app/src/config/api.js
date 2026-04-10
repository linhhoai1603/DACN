const config = {
    API_BASE_URL: process.env.NODE_ENV === 'production' 
        ? 'http://54.84.136.166:8080' // TODO: Thay thế bằng URL backend thật khi deploy
        : 'http://localhost:8080'
};

export default config;
