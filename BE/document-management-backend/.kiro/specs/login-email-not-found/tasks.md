# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - getUsername() Returns Phone Instead of Email
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Scope the property to the concrete failing case: `User.getUsername()` must return email, not phone
  - Test that `user.getUsername()` returns the email field (from Bug Condition in design: `User.getUsername() != field used in loadUserByUsername()`)
  - Test that `loadUserByUsername(user.getPhone())` throws `UsernameNotFoundException` (confirms mismatch)
  - Test that `AuthServiceImpl.login()` with valid email/phone/password returns HTTP 200 (will FAIL on unfixed code, returns 404)
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (confirms bug - `getUsername()` returns phone, login returns 404 instead of 200)
  - Document counterexamples found (e.g., `user.getUsername()` returns `"0901234567"` instead of `"user@example.com"`)
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Error Handling Behaviors Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe: `login()` với email không tồn tại → trả về HTTP 404 "Email not found" trên code chưa fix
  - Observe: `login()` với password sai → trả về HTTP 401 "Password is incorrect" trên code chưa fix
  - Observe: `login()` với phone không khớp → trả về HTTP 404 "User with matching phone not found" trên code chưa fix
  - Observe: `login()` với request thiếu trường → controller trả về HTTP 400 với map lỗi validation
  - Write property-based tests: for all LoginRequest with non-existent email, result is NOT HTTP 200 (from Preservation Requirements in design)
  - Write property-based tests: for all LoginRequest with wrong password, result is HTTP 401
  - Verify tests PASS on UNFIXED code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix getUsername() mismatch bug

  - [x] 3.1 Implement the fix in User.java
    - Sửa `User.getUsername()` để trả về `email` thay vì `phone`
    - Đảm bảo nhất quán với `loadUserByUsername()` đang dùng `findByEmail()`
    - Đảm bảo JWT subject được set bằng email (vì `generateToken` dùng `userDetails.getUsername()`)
    - _Bug_Condition: `User.getUsername()` trả về `phone` trong khi `loadUserByUsername()` tra cứu bằng `findByEmail()`_
    - _Expected_Behavior: `login()` với email/phone/password hợp lệ trả về HTTP 200 + JWT token_
    - _Preservation: email không tồn tại → lỗi phù hợp; password sai → HTTP 401; phone không khớp → lỗi phù hợp; validation lỗi → HTTP 400_
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Valid Login Returns HTTP 200 + JWT
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms `getUsername()` returns email and login returns HTTP 200 + JWT)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Error Handling Behaviors Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions in error handling)
    - Confirm all error scenarios still behave identically after fix

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
