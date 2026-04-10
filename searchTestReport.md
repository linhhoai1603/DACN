# Technical Test Report - DocuManage (Search Feature)

> **Ngay kiem tra:** 10/04/2026
> **Phien ban:**
> - DocuManage-Backend: `document-management-backend / DocumentPreviewController, DocumentMetadataRepository`
> - DocuManage-Frontend: `my-react-app / DashboardLayout.jsx, DashboardPage.jsx`
> **Nguoi kiem tra:** AI QA Engineer
> **Ky hieu muc do:**
> 🔴 Critical - Gay crash, mat du lieu, lo hong bao mat nghiem trong
> 🟠 High - Chuc nang chinh bi hong hoac rui ro cao
> 🟡 Medium - Logic sai nhung co workaround, anh huong UX
> 🟢 Low - Code smell, best practice, cai thien chat luong
> ℹ️ Info - Ghi chu, suggestion, khong phai loi

---

## 0. Checklist hoan thanh do

- [ ] [BUG-001] Search chi hoat dong tren trang Dashboard, khong hoat dong tren cac trang khac
- [ ] [BUG-002] Khong co gioi han do dai query - co the gui chuoi bat ky
- [ ] [BUG-003] Ket qua search cung cap co dinh 10 ban ghi, khong co pagination
- [ ] [BUG-004] Khong co debounce - moi lan xoa ky tu trang se reset ket qua ngay lap tuc
- [ ] [BUG-005] Khong xu ly loi khi API search that bai (loi am tham, khong thong bao user)
- [ ] [BUG-006] State `searchResults` va `isSearching` khai bao trong DashboardLayout nhung khong dung
- [ ] [BUG-007] Import `React` va `Home` thua trong DashboardLayout.jsx
- [ ] [BUG-008] Khong co trang thai loading hien thi khi dang tim kiem

---

## 1. Executive Summary

| Muc do      | So luong |
| ----------- | -------- |
| 🔴 Critical | 0        |
| 🟠 High     | 2        |
| 🟡 Medium   | 3        |
| 🟢 Low      | 3        |
| **Tong**    | **8**    |

**Diem manh noi bat:**
- Search da duoc implement day du end-to-end: FE goi API, BE xu ly, DB query native SQL voi LIKE case-insensitive.
- Query JOIN voi `document_versions` dam bao chi tra ve phien ban moi nhat cua moi file.
- Ho tro ca Enter key va click icon de trigger search.
- Khi xoa trang o input, ket qua search tu dong reset ve danh sach chinh.

**Rui ro lon nhat can xu ly:**
- Search chi hoat dong tren trang Dashboard - cac trang khac (VersionControl, UploadDashboard) nhan `onSearchResults=undefined`, ket qua search bi mat.
- Khong co gioi han do dai query, co the bi loi hoac bi khai thac.

---

## 2. Danh sach Loi & Diem Yeu

### [BUG-001] Search chi hoat dong tren trang Dashboard

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟠 High |
| **Category** | Functionality |
| **Location** | `FE/my-react-app/src/App.js`, `FE/my-react-app/src/component/DashboardLayout.jsx` |

**Description:**
`onSearchResults` callback chi duoc truyen vao `DashboardPage`. Cac trang khac nhu `VersionControl`, `UploadDashboard` khong nhan prop nay, nen khi user search o cac trang do, ket qua bi bo qua hoan toan.

**Steps to Reproduce:**
1. Dang nhap, vao trang Version Control.
2. Nhap tu khoa vao o search, nhan Enter.
3. **Actual Result:** Khong co gi xay ra, danh sach khong thay doi.
4. **Expected Result:** Hien thi ket qua search hoac chuyen huong sang Dashboard voi ket qua.

**Impact:** User khong the search khi dang o trang khac ngoai Dashboard.

**Buggy Code:**
```jsx
// App.js - chi DashboardPage duoc truyen onSearchResults
if (currentRoute === DASHBOARD) {
    return <DashboardPage onNavigate={navigateTo} onLogout={handleLogout} />;
    // Thieu: onSearchResults
}
if (currentRoute === VERSION_CONTROL_ROUTE) {
    return <VersionControl onNavigate={navigateTo} onLogout={handleLogout} />;
    // Khong co onSearchResults
}
```

**Proposed Fix:**
```jsx
// App.js
const [searchResults, setSearchResults] = useState(null);

if (currentRoute === DASHBOARD) {
    return <DashboardPage onNavigate={navigateTo} onLogout={handleLogout} searchResults={searchResults} />;
}
// Hoac don gian hon: khi search o bat ky trang nao, navigate ve Dashboard
// DashboardLayout.jsx - sau khi lay ket qua:
if (onSearchResults) {
    onSearchResults(mapped);
} else {
    navigateTo('/dashboard'); // fallback
}
```

---

### [BUG-002] Khong co gioi han do dai query search

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟠 High |
| **Category** | Security / Robustness |
| **Location** | `BE/.../controller/DocumentPreviewController.java:L47`, `BE/.../repository/DocumentMetadataRepository.java` |

**Description:**
Endpoint `GET /files/search?keyword=` khong validate do dai cua `keyword`. User co the gui chuoi rat dai, gay ra query LIKE ton kem hoac loi DB.

**Steps to Reproduce:**
1. Gui request: `GET /files/search?keyword=aaaa...` (chuoi 10000 ky tu).
2. **Actual Result:** BE xu ly query, DB thuc hien LIKE tren chuoi dai.
3. **Expected Result:** Tra ve 400 Bad Request neu keyword vuot qua gioi han.

**Impact:** Co the bi khai thac de lam cham DB, anh huong performance toan he thong.

**Buggy Code:**
```java
@GetMapping(value = "/search", params = {"keyword"})
public ResponseEntity<List<DocumentMetadataResponse>> searchDocument(@RequestParam String keyword) {
    // Khong co validation do dai
    return ResponseEntity.ok(documentService.searchDocuments(keyword));
}
```

**Proposed Fix:**
```java
@GetMapping(value = "/search", params = {"keyword"})
public ResponseEntity<?> searchDocument(@RequestParam @Size(max = 100) String keyword) {
    if (keyword.isBlank()) return ResponseEntity.badRequest().body("Keyword must not be blank");
    return ResponseEntity.ok(documentService.searchDocuments(keyword));
}
```

---

### [BUG-003] Ket qua search gioi han cung co dinh 10 ban ghi, khong co pagination

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟡 Medium |
| **Category** | Functionality / UX |
| **Location** | `BE/.../repository/DocumentMetadataRepository.java:L57` |

**Description:**
Query search hardcode `LIMIT 10`. Neu co nhieu hon 10 file khop voi tu khoa, user khong the xem them.

**Steps to Reproduce:**
1. Upload hon 10 file co ten chua chu "test".
2. Search tu khoa "test".
3. **Actual Result:** Chi hien thi 10 ket qua dau tien.
4. **Expected Result:** Hien thi tat ca ket qua hoac co pagination.

**Buggy Code:**
```sql
WHERE dv.is_latest = true
  AND LOWER(dm.file_name) LIKE CONCAT('%', LOWER(:keyword), '%')
ORDER BY dv.uploaded_at DESC
LIMIT 10  -- hardcoded
```

**Proposed Fix:**
```java
// Them param page/size vao search endpoint
@GetMapping(value = "/search", params = {"keyword"})
public ResponseEntity<List<DocumentMetadataResponse>> searchDocument(
    @RequestParam String keyword,
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "0") int offset
) { ... }
```

---

### [BUG-004] Khong xu ly loi khi API search that bai

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟡 Medium |
| **Category** | UX / Error Handling |
| **Location** | `FE/my-react-app/src/component/DashboardLayout.jsx:L44` |

**Description:**
Khi API search tra ve loi (401, 500, network error), FE chi `console.error` ma khong hien thi thong bao gi cho user. User khong biet search that bai hay khong co ket qua.

**Steps to Reproduce:**
1. Tat BE server.
2. Nhap tu khoa va nhan Enter.
3. **Actual Result:** Khong co gi hien thi, o search van binh thuong.
4. **Expected Result:** Hien thi thong bao loi "Khong the ket noi den server" hoac tuong tu.

**Buggy Code:**
```js
} catch (error) {
    console.error("Search API error:", error); // Chi log, khong notify user
} finally {
    setIsSearching(false);
}
```

**Proposed Fix:**
```js
} catch (error) {
    console.error("Search API error:", error);
    if (onSearchResults) onSearchResults([]); // Reset ve empty
    // Hien thi toast hoac error state
}
```

---

### [BUG-005] Khong co trang thai loading khi dang tim kiem

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟡 Medium |
| **Category** | UX |
| **Location** | `FE/my-react-app/src/component/DashboardLayout.jsx` |

**Description:**
State `isSearching` duoc set nhung khong duoc su dung de hien thi bat ky indicator nao. User khong biet he thong dang xu ly hay bi treo.

**Steps to Reproduce:**
1. Nhap tu khoa va nhan Enter.
2. **Actual Result:** Khong co spinner hay feedback gi trong luc cho.
3. **Expected Result:** Icon search doi thanh spinner hoac input bi disable trong luc goi API.

**Buggy Code:**
```jsx
const [isSearching, setIsSearching] = useState(false); // Khai bao nhung khong dung trong JSX
```

**Proposed Fix:**
```jsx
<Search
    size={16}
    color={isSearching ? "#94a3b8" : "#64748b"}
    style={{ cursor: isSearching ? 'wait' : 'pointer' }}
    onClick={!isSearching ? handleSearch : undefined}
/>
```

---

### [BUG-006] State `searchResults` va `isSearching` khai bao thua trong DashboardLayout

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟢 Low |
| **Category** | Code Quality |
| **Location** | `FE/my-react-app/src/component/DashboardLayout.jsx:L19-20` |

**Description:**
`searchResults` va `isSearching` duoc khai bao trong `DashboardLayout` nhung ket qua search duoc truyen ra ngoai qua `onSearchResults` callback. State noi bo nay khong duoc render, gay nham lan ve noi luu tru state.

**Buggy Code:**
```js
const [searchResults, setSearchResults] = useState([]); // Khong dung
const [isSearching, setIsSearching] = useState(false);  // Chi set, khong render
```

**Proposed Fix:**
Xoa `searchResults` state, giu lai `isSearching` va su dung no de hien thi loading UI.

---

### [BUG-007] Import thua `React` va `Home` trong DashboardLayout.jsx

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟢 Low |
| **Category** | Code Quality |
| **Location** | `FE/my-react-app/src/component/DashboardLayout.jsx:L1,L9` |

**Description:**
`React` (khong can thiet voi React 17+) va `Home` (da xoa khoi menu) van con trong import.

**Buggy Code:**
```js
import React, { useState } from 'react'; // React thua
import { ..., Home, ... } from 'lucide-react'; // Home thua
```

**Proposed Fix:**
```js
import { useState } from 'react';
import { Search, Bell, HelpCircle, User, Users, Trash, FileText, LogOut, Hexagon } from 'lucide-react';
```

---

### [BUG-008] Search chi tim kiem theo ten file, khong tim theo nguoi upload hoac ngay

| Thuoc tinh | Chi tiet |
|---|---|
| **Severity** | 🟢 Low |
| **Category** | Functionality / UX |
| **Location** | `BE/.../repository/DocumentMetadataRepository.java:L43-58` |

**Description:**
Query search chi LIKE tren `dm.file_name`. User khong the tim kiem theo `uploaded_by` hoac khoang thoi gian upload.

**Proposed Fix:**
```sql
WHERE dv.is_latest = true
  AND (
    LOWER(dm.file_name) LIKE CONCAT('%', LOWER(:keyword), '%')
    OR LOWER(dm.uploaded_by) LIKE CONCAT('%', LOWER(:keyword), '%')
  )
```

---

## 3. Test Cases

| TC# | Mo ta | Input | Expected | Actual | Ket qua |
|-----|-------|-------|----------|--------|---------|
| TC01 | Search tu khoa hop le | keyword="report" | Tra ve danh sach file chua "report" | ✅ Hoat dong | PASS |
| TC02 | Search khong phan biet hoa thuong | keyword="REPORT" | Tra ve ket qua giong TC01 | ✅ LIKE LOWER() | PASS |
| TC03 | Search khong co ket qua | keyword="xyzxyz123" | Hien thi "Khong co ket qua tim kiem nao phu hop" | ✅ Hien thi dung | PASS |
| TC04 | Xoa trang o input | keyword="" | Reset ve danh sach chinh | ✅ Hoat dong | PASS |
| TC05 | Nhan Enter de search | Enter key | Goi API search | ✅ Hoat dong | PASS |
| TC06 | Click icon search | Click icon | Goi API search | ✅ Hoat dong | PASS |
| TC07 | Search tren trang VersionControl | keyword="abc" | Hien thi ket qua | ❌ Khong co gi xay ra | FAIL |
| TC08 | Search voi chuoi rong (chi spaces) | keyword="   " | Khong goi API | ✅ trim() check | PASS |
| TC09 | Search khi BE offline | BE tat | Hien thi thong bao loi | ❌ Am tham that bai | FAIL |
| TC10 | Search voi chuoi rat dai | keyword=10000 chars | 400 Bad Request | ❌ BE xu ly binh thuong | FAIL |
| TC11 | Ket qua search hien thi dung cot FILE NAME | keyword hop le | Cot FILE NAME hien thi ten file dung | ✅ file.fileName render chinh xac | PASS |
| TC12 | Ket qua search hien thi dung cot UPLOADER | keyword hop le | Cot UPLOADER hien thi ten nguoi upload | ✅ file.uploadedBy render chinh xac | PASS |
| TC13 | Ket qua search hien thi dung cot UPLOAD TIME | keyword hop le | Cot UPLOAD TIME hien thi ngay gio dung dinh dang | ✅ formatDate + formatTime hoat dong | PASS |
| TC14 | Ket qua search hien thi dung cot COMMIT | keyword hop le | Cot COMMIT hien thi commit message | ✅ file.commitMessage render chinh xac | PASS |
| TC15 | Pagination an khi dang hien thi ket qua search | Search co ket qua | Thanh pagination khong hien thi | ✅ !searchResults check | PASS |
| TC16 | Pagination hien lai sau khi xoa keyword | Xoa keyword | Thanh pagination hien thi lai | ✅ searchResults reset ve null | PASS |
| TC17 | Click vao file trong ket qua search mo preview | Click row | Modal preview hien thi | ✅ setPreviewDoc hoat dong | PASS |
| TC18 | Click Update trong ket qua search | Click Update | Chuyen sang trang update-document | ✅ onNavigate('/update-document') | PASS |
| TC19 | Icon file doi mau theo loai file trong ket qua search | File PDF/DOCX/XLSX | Icon hien thi mau tuong ung | ✅ getFileColor() hoat dong | PASS |
| TC20 | Search tra ve toi da 10 ket qua | Co >10 file khop | Chi hien thi 10 ket qua | ✅ LIMIT 10 trong SQL | PASS |
| TC21 | Ket qua search chi lay phien ban moi nhat cua moi file | File co nhieu version | Chi hien thi version moi nhat | ✅ JOIN + is_latest=true | PASS |
| TC22 | Ket qua search sap xep theo ngay upload giam dan | keyword hop le | File moi nhat hien thi truoc | ✅ ORDER BY uploaded_at DESC | PASS |
