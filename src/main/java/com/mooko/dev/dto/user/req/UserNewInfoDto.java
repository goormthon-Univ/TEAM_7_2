package com.mooko.dev.dto.user.req;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserNewInfoDto {
    private File profileImage;
    private String nickname;
    private String birth;
    private String gender;
}
