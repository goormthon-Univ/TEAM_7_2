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
public class EventListDto {
    private String title;
    private String id;
    private String imageCount;
}
