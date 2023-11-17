package com.mooko.dev.dto.user.res;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEventStatusDto {

    private boolean existEvent;
    private String eventId;
}
