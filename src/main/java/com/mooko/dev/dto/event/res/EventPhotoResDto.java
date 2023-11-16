package com.mooko.dev.dto.event.res;

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
public class EventPhotoResDto {
    private String eventId;
    private List<String> imageUrlList;
}
