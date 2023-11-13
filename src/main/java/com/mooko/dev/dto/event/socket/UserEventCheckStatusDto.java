package com.mooko.dev.dto.event.socket;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEventCheckStatusDto {
    private String userId;
    private boolean checkStatus;
}
