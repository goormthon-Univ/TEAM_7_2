package com.mooko.dev.dto.event.res;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class EventList {
    private List<EventListDto> eventList;
}
