package com.mooko.dev.dto.barcode.res;

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
public class BarcodeInfoDto {
    private String id;
    private String imageUrl;
    private String title;
}
