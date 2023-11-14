package com.mooko.dev.dto.event.res;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEventStatusDto {
    private boolean isExistEvent;
    private String eventId;
}
