package com.mooko.dev.dto.event.req;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewEventDto {

    private String title;
    private String startDate;
    private String endDate;
}
