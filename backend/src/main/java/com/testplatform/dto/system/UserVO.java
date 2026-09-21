package com.testplatform.dto.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表/详情视图对象（不含密码）
 */
@Data
public class UserVO {

    /** 用户 ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 昵称/姓名 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 URL */
    private String avatar;

    /** 状态：1启用 / 0禁用 */
    private Integer status;

    /** 绑定角色 ID 列表 */
    private List<Long> roleIds;

    /** 绑定角色名称列表 */
    private List<String> roleNames;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}
