# Bugfix Requirements Document

## Introduction

Khi người dùng thực hiện đăng nhập với email, số điện thoại và mật khẩu hợp lệ, hệ thống trả về lỗi "Email not found" mặc dù tài khoản tồn tại trong database. Nguyên nhân là sự không nhất quán giữa `User.getUsername()` (trả về `phone`) và `UserRepositoryDetailsService.loadUserByUsername()` (tra cứu theo `email`), khiến Spring Security không thể xác thực người dùng đúng cách.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN người dùng gửi request đăng nhập với email, phoneNumber và password hợp lệ THEN hệ thống trả về HTTP 404 với thông báo "Email not found"

1.2 WHEN Spring Security gọi `loadUserByUsername(username)` với giá trị `username` lấy từ `User.getUsername()` (là `phone`) THEN hệ thống thực hiện `findByEmail(phone)` và không tìm thấy user, ném `UsernameNotFoundException`

1.3 WHEN `AuthServiceImpl.login()` tìm thấy user qua `findByEmail()` nhưng Spring Security authentication context không được thiết lập đúng THEN hệ thống không thể hoàn tất luồng xác thực JWT

### Expected Behavior (Correct)

2.1 WHEN người dùng gửi request đăng nhập với email, phoneNumber và password hợp lệ THEN hệ thống SHALL trả về HTTP 200 kèm JWT token

2.2 WHEN Spring Security gọi `loadUserByUsername(identifier)` THEN hệ thống SHALL tra cứu user bằng đúng trường định danh khớp với giá trị `User.getUsername()` trả về

2.3 WHEN `User.getUsername()` trả về `phone` THEN `loadUserByUsername()` SHALL dùng `findByPhone()` để tra cứu, hoặc `User.getUsername()` SHALL được cập nhật để trả về `email` và `loadUserByUsername()` dùng `findByEmail()`

### Unchanged Behavior (Regression Prevention)

3.1 WHEN người dùng gửi request với email không tồn tại trong database THEN hệ thống SHALL CONTINUE TO trả về lỗi phù hợp (không tìm thấy tài khoản)

3.2 WHEN người dùng gửi request với password sai THEN hệ thống SHALL CONTINUE TO trả về HTTP 401 với thông báo "Password is incorrect"

3.3 WHEN người dùng gửi request với phoneNumber không khớp với email THEN hệ thống SHALL CONTINUE TO trả về lỗi phù hợp

3.4 WHEN người dùng gửi request thiếu các trường bắt buộc hoặc sai định dạng THEN hệ thống SHALL CONTINUE TO trả về HTTP 400 với danh sách lỗi validation

3.5 WHEN người dùng đăng nhập thành công THEN hệ thống SHALL CONTINUE TO sinh JWT token hợp lệ có thể dùng cho các request tiếp theo

---

## Bug Condition (Pseudocode)

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type LoginRequest
  OUTPUT: boolean

  // Bug xảy ra khi User.getUsername() trả về phone
  // nhưng loadUserByUsername() tra cứu bằng email
  RETURN User.getUsername() != field used in loadUserByUsername()
END FUNCTION
```

```pascal
// Property: Fix Checking
FOR ALL X WHERE isBugCondition(X) DO
  result ← login'(X)
  ASSERT result.status = 200 AND result.body = JWT_TOKEN
END FOR
```

```pascal
// Property: Preservation Checking
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT login(X) = login'(X)
END FOR
```
