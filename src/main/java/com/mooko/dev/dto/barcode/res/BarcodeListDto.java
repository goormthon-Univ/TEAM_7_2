package com.mooko.dev.dto.barcode.res;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BarcodeListDto {
    private List<BarcodeInfoDto> barcodeList;
}
