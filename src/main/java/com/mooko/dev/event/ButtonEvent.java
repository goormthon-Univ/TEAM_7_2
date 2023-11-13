package com.mooko.dev.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ButtonEvent {
    private boolean buttonStatus;
    private String eventId;
}
