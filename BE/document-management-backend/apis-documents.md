# API Documents for Frontend

## 1. Overview

- Base URL (local): `http://localhost:8080`
- Content type:
  - JSON for auth endpoints
  - multipart/form-data for document upload endpoints
- Auth:
  - Public: `/api/auth/login`, `/api/auth/register`
  - JWT required: all other endpoints
  - Header format: `Authorization: Bearer <accessToken>`
- CORS allowed origin: `http://localhost:3000`

---

## 2. Standard API Response

All APIs return wrapped response in this structure:

```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "Any message",
  "data": {}
}
```

Error response format:

```json
{
  "code": 400,
  "status": "ERROR",
  "message": "Any error message",
  "data": null
}
```

Validation error response example:

```json
{
  "code": 400,
  "status": "ERROR",
  "message": "Validation failed",
  "data": {
    "username": "username is required",
    "password": "password is required"
  }
}
```

---

## 3. Authentication APIs

### 3.1 Register

- Method: `POST`
- URL: `/api/auth/register`
- Auth required: `No`
- Content-Type: `application/json`

Request body:

```json
{
  "username": "newuser01",
  "password": "Password@123",
  "email": "newuser01@gov.local",
  "fullName": "Nguyen Van A"
}
```

Rules:

- `username`: required, length 4..100
- `password`: required, min length 8
- `email`: required, valid email format
- `fullName`: required, max length 255

Success response:

```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "Register successful",
  "data": {
    "id": 2,
    "username": "newuser01",
    "email": "newuser01@gov.local",
    "fullName": "Nguyen Van A",
    "isActive": true
  }
}
```

Possible errors:

- 400: `Username already exists`
- 400: `Email already exists`
- 400: validation errors

### 3.2 Login

- Method: `POST`
- URL: `/api/auth/login`
- Auth required: `No`
- Content-Type: `application/json`

Request body:

```json
{
  "username": "admin",
  "password": "Admin@123"
}
```

Success response:

```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt-token>",
    "tokenType": "Bearer",
    "expiresInMs": 86400000
  }
}
```

Possible errors:

- 400: `Invalid username or password`
- 400: validation errors

---

## 4. Document APIs (Git-like versioning)

### 4.1 Create document with initial version

- Method: `POST`
- URL: `/api/documents`
- Auth required: `Yes (Bearer token)`
- Content-Type: `multipart/form-data`

Form-data fields:

1. `payload` (JSON text):

```json
{
  "title": "Decision 2026-01",
  "description": "Initial draft",
  "commitMessage": "Initial version",
  "status": "DRAFT"
}
```

2. `file` (binary file)

Notes:

- `status` accepted values: `DRAFT`, `PUBLISHED`, `ARCHIVED`
- If `commitMessage` is empty or missing, backend uses: `Initial version`

Success response:

```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "Document created with initial version",
  "data": {
    "versionId": 10,
    "documentId": "b49a33f0-793f-4c83-95f3-0a045a8de1b5",
    "versionNumber": 1,
    "fileUrl": "https://ik.imagekit.io/...",
    "fileId": "680f...",
    "fileName": "decision.docx",
    "fileSize": 23122,
    "fileHash": "7d0af9...",
    "commitMessage": "Initial version",
    "createdBy": "admin",
    "createdAt": "2026-04-04T01:23:45.000Z"
  }
}
```

### 4.2 Upload new version for existing document

- Method: `POST`
- URL: `/api/documents/{documentId}/versions`
- Auth required: `Yes (Bearer token)`
- Content-Type: `multipart/form-data`

Path param:

- `documentId`: UUID of existing document

Form-data fields:

- `commitMessage` (text): required
- `file` (binary): required

Success response:

```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "New document version uploaded",
  "data": {
    "versionId": 11,
    "documentId": "b49a33f0-793f-4c83-95f3-0a045a8de1b5",
    "versionNumber": 2,
    "fileUrl": "https://ik.imagekit.io/...",
    "fileId": "680f...",
    "fileName": "decision-v2.docx",
    "fileSize": 24501,
    "fileHash": "98ffca...",
    "commitMessage": "Update article 3",
    "createdBy": "admin",
    "createdAt": "2026-04-04T01:28:45.000Z"
  }
}
```

Possible errors:

- 400: `commitMessage is required`
- 400: `File is required`
- 404: `Document not found: <documentId>`
- 401: Unauthorized (missing/invalid token)
- 403: Forbidden (no permission)

---

## 5. Error Codes Cheat Sheet

- 200: success
- 400: bad request / validation / business rule fail
- 401: unauthorized (token missing/invalid)
- 403: forbidden
- 404: resource not found
- 500: internal server error

---

## 6. Backend Application Flow (for FE understanding)

### 6.1 Auth flow

1. FE calls `POST /api/auth/register` to create account.
2. FE calls `POST /api/auth/login` to get JWT access token.
3. FE stores token (localStorage/session storage).
4. FE sends token in `Authorization` header for protected APIs.
5. BE JWT filter validates token and sets authenticated user in SecurityContext.

### 6.2 Document versioning flow

1. FE creates new document via `POST /api/documents` with `payload + file`.
2. BE uploads file to ImageKit and creates version `v1`.
3. FE uploads changes via `POST /api/documents/{id}/versions` with `commitMessage + file`.
4. BE locks document row, calculates next version number (`v2`, `v3`, ...), computes SHA-256 hash, saves new version.
5. FE receives full version metadata for rendering timeline/history.

### 6.3 Security flow

1. Public routes only: login/register.
2. Any other route without valid JWT returns 401.
3. Authenticated but not allowed user returns 403.

---

## 7. FE Integration Checklist

- Always parse wrapped response: `code`, `status`, `message`, `data`.
- On `status = ERROR`, show `message` and handle by `code`.
- For document create endpoint, send `payload` as JSON part in multipart request.
- For version upload endpoint, send `commitMessage` and `file` parts.
- Always attach Bearer token for `/api/documents/**`.
