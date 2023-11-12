package com.mooko.dev.dto.event.req;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateEventDateDto {
    private String startDate;
    private String endDate;
}
