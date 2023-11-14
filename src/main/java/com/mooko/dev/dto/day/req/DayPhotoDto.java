package com.mooko.dev.dto.day.req;

import java.io.File;
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
public class DayPhotoDto {
    private String memo;
    private File thumbnail;
    private File photo1;
    private File photo2;
    private File photo3;
}
