package com.mooko.dev.dto.event.req;

import lombok.*;

import java.io.File;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventPhotoDto {
    private List<File> imageList;
}
