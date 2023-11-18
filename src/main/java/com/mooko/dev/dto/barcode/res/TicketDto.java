package com.mooko.dev.dto.barcode.res;

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
public class TicketDto {
    private String nickname;
    private String title;
    private String barcodeUrl;
    private String startDate;
    private String endDate;
    private String createdAt;
    private List<ImageInfoDto> imageInfoList;
}
