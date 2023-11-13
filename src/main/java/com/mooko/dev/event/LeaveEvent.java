package com.mooko.dev.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LeaveEvent {
    private boolean eventStatus;
    private String eventId;
}
