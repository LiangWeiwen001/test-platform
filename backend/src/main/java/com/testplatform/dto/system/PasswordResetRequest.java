package com.testplatform.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class PasswordResetRequest {

    /** 新密码（明文，服务端 BCrypt 加密存储） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度需在 6-50 之间")
    private String password;
}
