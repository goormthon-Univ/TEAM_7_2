package com.mooko.dev.dto.event.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MyEventDto {
    private Boolean isExistEvent;
    private Long eventId;
}
