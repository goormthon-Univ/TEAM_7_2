package com.mooko.dev.dto.user.res;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPassportDto {
    private String nickname;
    private String birth;
    private String gender;
    private String dateOfIssue;

    private int barcodeCount;

    private String profileUrl;

    private String recentBarcodeImg;
    private List<String> recentBarcodeTitleList;

    private boolean modalActive;
}
