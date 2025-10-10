package com.gravifox.domain.member.dto.login;


import com.gravifox.domain.member.domain.Password;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.dto.AuthDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

//
@Schema(
        description = "사용자 로그인 요청 DTO",
        example = """
    {
      "user": {
        "userId": "user@example.com"
      },
      "password": {
        "password": "Password123!"
      }
    }
    """
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest implements AuthDTO {

    @Schema(
            description = "로그인에 사용할 사용자 정보",
            required = true,
            example = """
        {
          "userId": "user@example.com"
        }
        """
    )
    @NotNull(message = "user must not be null")
    private User user;

    @Schema(
            description = "사용자 비밀번호 정보",
            required = true,
            example = """
        {
          "password": "Password123!"
        }
        """
    )
    @NotNull(message = "password must not be null")
    private Password password;

    public Map<String, String> getDataMap() {
        Map<String, String> data = new HashMap<>();
        data.put("userId", this.user.getUserId());
        data.put("userNo", String.valueOf(this.user.getUserNo()));
        return data;
    }


    public void changeUser(User user) {
        this.user = user;
    }

    @Override
    public String extractUserID() {
        return this.user.getUserId();
    }
}
