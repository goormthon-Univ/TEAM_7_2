package com.mooko.dev.dto.event.res;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventInfoDto {
    private List<String> profileImgUrlList;
    private boolean isRoomMaker;
    private String eventName;
    private String startDate;
    private String endDate;
    private String loginUserId;
    private int userCount;
    private List<UserInfoDto> userInfo;
}
