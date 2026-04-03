# Login Email Not Found - Bugfix Design

## Overview

Bug xảy ra khi người dùng đăng nhập với email + phoneNumber + password hợp lệ nhưng hệ thống trả về HTTP 404 "Email not found". Nguyên nhân là `User.getUsername()` trả về `phone`, nhưng `UserRepositoryDetailsService.loadUserByUsername()` tra cứu bằng `findByEmail()`. Khi Spring Security gọi lại `loadUserByUsername(user.getUsername())` trong quá trình xác thực, nó truyền vào giá trị `phone` nhưng hàm lại tìm theo `email`, dẫn đến `UsernameNotFoundException`.

Hướng fix: cập nhật `User.getUsername()` để trả về `email` thay vì `phone`, đảm bảo nhất quán với `loadUserByUsername()` đang dùng `findByEmail()`.

## Glossary

- **Bug_Condition (C)**: Điều kiện kích hoạt bug - `User.getUsername()` trả về `phone` trong khi `loadUserByUsername()` tra cứu bằng `findByEmail()`
- **Property (P)**: Hành vi mong muốn - đăng nhập với thông tin hợp lệ phải trả về HTTP 200 kèm JWT token
- **Preservation**: Các hành vi hiện tại không bị ảnh hưởng bởi fix: xử lý email không tồn tại, password sai, phone không khớp, validation lỗi
- **User.getUsername()**: Method trong `model/User.java` implement `UserDetails`, trả về định danh dùng cho Spring Security
- **loadUserByUsername()**: Method trong `service/UserRepositoryDetailsService.java`, được Spring Security gọi để load user theo định danh
- **AuthServiceImpl.login()**: Method trong `service/impl/AuthServiceImpl.java`, xử lý luồng đăng nhập thủ công

## Bug Details

### Bug Condition

Bug xảy ra khi Spring Security gọi `loadUserByUsername(user.getUsername())` trong quá trình xác thực. `User.getUsername()` trả về `phone`, nhưng `loadUserByUsername()` gọi `findByEmail(phone)` - không tìm thấy user, ném `UsernameNotFoundException`.

**Formal Specification:**
```
FUNCTION isBugCondition(X)
  INPUT: X of type LoginRequest
  OUTPUT: boolean

  userFromDb ← userRepository.findByEmail(X.email)
  IF userFromDb IS EMPTY THEN RETURN false  // email không tồn tại, lỗi khác

  identifier ← userFromDb.getUsername()     // trả về phone
  lookupField ← fieldUsedIn(loadUserByUsername)  // là email

  RETURN identifier != lookupField
         // tức là: phone != email → luôn true với code hiện tại
END FUNCTION
```

### Examples

- `POST /auth/login` với `{ email: "user@example.com", phoneNumber: "0901234567", password: "secret123" }` → HTTP 404 "Email not found" (expected: HTTP 200 + JWT)
- Spring Security gọi `loadUserByUsername("0901234567")` → `findByEmail("0901234567")` → không tìm thấy → `UsernameNotFoundException`
- `POST /auth/login` với email không tồn tại → HTTP 404 "Email not found" (behavior này đúng, cần preserve)
- `POST /auth/login` với password sai → HTTP 401 "Password is incorrect" (behavior này đúng, cần preserve)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Khi email không tồn tại trong database, hệ thống vẫn trả về lỗi phù hợp (không tìm thấy tài khoản)
- Khi password sai, hệ thống vẫn trả về HTTP 401 "Password is incorrect"
- Khi phoneNumber không khớp với email, hệ thống vẫn trả về lỗi phù hợp
- Khi request thiếu trường bắt buộc hoặc sai định dạng, hệ thống vẫn trả về HTTP 400 với danh sách lỗi validation
- Khi đăng nhập thành công, JWT token sinh ra vẫn hợp lệ cho các request tiếp theo

**Scope:**
Tất cả input KHÔNG thuộc bug condition (email không tồn tại, password sai, phone không khớp, validation lỗi) phải hoàn toàn không bị ảnh hưởng bởi fix này.

## Hypothesized Root Cause

1. **Mismatch giữa getUsername() và loadUserByUsername()**: `User.getUsername()` trả về `phone` (dòng `return phone;` trong `User.java`), nhưng `loadUserByUsername()` gọi `repository.findByEmail(email)`. Spring Security dùng `getUsername()` làm key để reload user, dẫn đến mismatch.

2. **AuthServiceImpl không dùng Spring Security AuthenticationManager**: `AuthServiceImpl.login()` tự xử lý authentication thủ công (gọi `findByEmail`, so sánh phone, so sánh password) thay vì delegate cho Spring Security `AuthenticationManager`. Điều này bypass luồng chuẩn nhưng vẫn có thể gây lỗi nếu Spring Security cố reload user ở bước khác.

3. **Không nhất quán trong thiết kế định danh**: Hệ thống dùng `email` làm định danh đăng nhập (LoginRequest có `email`, `loadUserByUsername` nhận `email`), nhưng `User.getUsername()` lại trả về `phone`.

## Correctness Properties

Property 1: Bug Condition - Đăng nhập thành công với thông tin hợp lệ

_For any_ LoginRequest X có email tồn tại trong database, phoneNumber khớp với user đó, và password đúng, hàm `login()` sau khi fix SHALL trả về HTTP 200 kèm JWT token hợp lệ.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Hành vi với input không thuộc bug condition

_For any_ LoginRequest X mà KHÔNG thỏa mãn điều kiện đăng nhập thành công (email không tồn tại, password sai, phone không khớp, hoặc validation lỗi), hàm `login()` sau khi fix SHALL trả về đúng response như trước khi fix, không thay đổi bất kỳ hành vi xử lý lỗi nào.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

**File**: `src/main/java/com/example/documentmanagementbackend/model/User.java`

**Method**: `getUsername()`

**Specific Changes**:
1. **Thay đổi return value**: Đổi `return phone;` thành `return email;` để `User.getUsername()` trả về `email`, nhất quán với `loadUserByUsername()` đang dùng `findByEmail()`.

```java
// Before
@Override
public String getUsername() {
    return phone;
}

// After
@Override
public String getUsername() {
    return email;
}
```

**Lý do chọn hướng fix này thay vì sửa `loadUserByUsername()`**:
- `AuthServiceImpl.login()` đã dùng `email` làm định danh chính (`findByEmail`, `LoginRequest.email`)
- `loadUserByUsername()` đang dùng `findByEmail()` - đây là behavior đúng với thiết kế hiện tại
- Chỉ cần align `getUsername()` với email để Spring Security reload user đúng cách

## Testing Strategy

### Validation Approach

Chiến lược kiểm thử theo hai giai đoạn: trước tiên chạy test trên code CHƯA FIX để xác nhận bug tồn tại (exploratory), sau đó verify fix hoạt động đúng và không gây regression.

### Exploratory Bug Condition Checking

**Goal**: Xác nhận bug tồn tại trên code chưa fix, confirm root cause analysis.

**Test Plan**: Viết unit test gọi `AuthServiceImpl.login()` với LoginRequest hợp lệ, assert response là HTTP 200. Chạy trên code CHƯA FIX để quan sát failure.

**Test Cases**:
1. **Valid Login Test**: Gọi `login()` với email/phone/password hợp lệ → expect HTTP 200 (sẽ fail trên code chưa fix, trả về 404)
2. **getUsername() Check**: Assert `user.getUsername()` trả về email → sẽ fail vì trả về phone
3. **loadUserByUsername() with phone**: Gọi `loadUserByUsername(phone)` → expect tìm thấy user (sẽ fail vì dùng findByEmail)

**Expected Counterexamples**:
- `login()` trả về HTTP 404 "Email not found" thay vì HTTP 200 + JWT
- `user.getUsername()` trả về `"0901234567"` (phone) thay vì `"user@example.com"` (email)

### Fix Checking

**Goal**: Verify rằng với mọi input thỏa bug condition, hàm sau fix trả về đúng behavior.

**Pseudocode:**
```
FOR ALL X WHERE isBugCondition(X) DO
  result ← login_fixed(X)
  ASSERT result.status = 200
  ASSERT result.body IS valid JWT token
END FOR
```

### Preservation Checking

**Goal**: Verify rằng với mọi input KHÔNG thuộc bug condition, hàm sau fix trả về đúng như trước.

**Pseudocode:**
```
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT login_original(X) = login_fixed(X)
END FOR
```

**Testing Approach**: Property-based testing được khuyến nghị vì:
- Tự động sinh nhiều test case trên toàn bộ input domain
- Bắt được edge case mà unit test thủ công có thể bỏ sót
- Đảm bảo mạnh mẽ rằng behavior không thay đổi với mọi non-buggy input

**Test Cases**:
1. **Email Not Found Preservation**: Verify `login()` với email không tồn tại vẫn trả về lỗi phù hợp sau fix
2. **Wrong Password Preservation**: Verify `login()` với password sai vẫn trả về HTTP 401 sau fix
3. **Phone Mismatch Preservation**: Verify `login()` với phone không khớp vẫn trả về lỗi phù hợp sau fix
4. **Validation Error Preservation**: Verify `login()` với request thiếu trường vẫn trả về HTTP 400 sau fix

### Unit Tests

- Test `User.getUsername()` trả về `email` sau fix
- Test `loadUserByUsername(email)` tìm thấy user đúng
- Test `AuthServiceImpl.login()` với thông tin hợp lệ trả về HTTP 200 + JWT
- Test edge case: email tồn tại nhưng phone không khớp

### Property-Based Tests

- Sinh ngẫu nhiên các LoginRequest với email không tồn tại, verify luôn trả về lỗi (không phải 200)
- Sinh ngẫu nhiên các LoginRequest với password sai, verify luôn trả về HTTP 401
- Sinh ngẫu nhiên các User hợp lệ, verify `getUsername()` luôn trả về email (không phải phone)

### Integration Tests

- Test full login flow: POST `/auth/login` với thông tin hợp lệ → HTTP 200 + JWT
- Test JWT token sinh ra có thể dùng để gọi protected endpoint
- Test switching giữa các scenario lỗi khác nhau không ảnh hưởng lẫn nhau
