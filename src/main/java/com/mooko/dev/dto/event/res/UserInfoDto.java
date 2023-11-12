package com.mooko.dev.dto.event.res;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserInfoDto {
    private String userId;
    private String nickname;
    private List<String> imageUrlList;
    private boolean checkStatus;
    private int imageCount;
}
