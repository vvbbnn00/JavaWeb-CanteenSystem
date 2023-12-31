package cn.vvbbnn00.canteen.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.beans.JavaBean;

@Data
@JavaBean
public class UserLoginRequest {
    @NotEmpty(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名不合法")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Size(min = 2, max = 20, message = "密码不合法")
    private String password;

    @NotEmpty(message = "签名不能为空")
    @Size(min = 64, max = 64, message = "签名不合法")
    private String sign;

    @NotEmpty(message = "时间戳不能为空")
    @Size(min = 13, max = 13, message = "时间戳不合法")
    private String timestamp;
}
