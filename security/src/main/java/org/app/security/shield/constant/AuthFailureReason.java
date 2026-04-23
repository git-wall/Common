package org.app.security.shield.constant;

/**
 * // Lý do xác thực thất bại
 * <pre>{@code
 * | Reason             | Signal                         |
 * | ------------------ | ------------------------------ |
 * | NO_TOKEN           | Request không có token         |
 * | TOKEN_EXPIRED      | Token quá hạn                  |
 * | INVALID_SIGNATURE  | Token bị sửa                   |
 * | MALFORMED_TOKEN    | Token hỏng format              |
 * | INSUFFICIENT_SCOPE | Token hợp lệ nhưng thiếu quyền |
 * }</pre>
 * // Hành vi người dùng tương ứng
 * <pre>{@code
 * | Reason             | Hành vi        |
 * | ------------------ | -------------- |
 * | NO_TOKEN           | Gọi API bừa    |
 * | INSUFFICIENT_SCOPE | Thử vượt quyền |
 * | INVALID_SIGNATURE  | Fake Token     |
 * | TOKEN_EXPIRED      | Old Token      |
 * }</pre>
 * // Mối đe dọa bảo mật có thể liên quan
 * <pre>{@code
 * | Reason             | Có thể liên quan          |
 * | ------------------ | ------------------------- |
 * | NO_TOKEN           | Scan, crawler, bug client |
 * | TOKEN_EXPIRED      | App bug, replay           |
 * | INVALID_SIGNATURE  | Forgery                   |
 * | INSUFFICIENT_SCOPE | Privilege probing         |
 * | MALFORMED_TOKEN    | Fuzzing                   |
 * }</pre>
 * */
public enum AuthFailureReason {
    NO_TOKEN,
    TOKEN_EXPIRED,
    INVALID_SIGNATURE,
    MALFORMED_TOKEN,
    INSUFFICIENT_SCOPE,
    UNKNOWN
}
