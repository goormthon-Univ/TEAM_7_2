package com.mooko.dev.dto.day.res;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;
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
public class CalendarResDto {
    private List<ThumbnailDto> thumbnailInfoList;
    @Enumerated(EnumType.STRING)
    private ButtonStatus buttonStatus;
}
