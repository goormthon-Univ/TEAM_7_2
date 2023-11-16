package com.mooko.dev.dto.day.req;

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
public class CalendarReqDto {
    private String startDate;
    private String endDate;
    private String year;
    private String month;
}
