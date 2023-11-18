package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.barcode.res.BarcodeInfoDto;
import com.mooko.dev.dto.barcode.res.BarcodeListDto;
import com.mooko.dev.dto.barcode.res.TicketDto;
import com.mooko.dev.facade.AggregationFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/barcode")
public class BarcodeController {
    private final AggregationFacade aggregationFacade;

    //4.무드클라우드
    @GetMapping("/list")
    public ResponseEntity<BarcodeListDto> showBarcodeList(
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        User user = principalDetails.getUser();
        BarcodeListDto barcodeList = aggregationFacade.showBarcodeInfo(user);
        return ResponseEntity.ok(barcodeList);
    }

    //4-A 바코드티켓(MY)
//    @GetMapping("/{barcodeId}/my-ticket")
//    public ResponseEntity<TicketDto> showMyTicket(
//            @AuthenticationPrincipal PrincipalDetails principalDetails,
//            @PathVariable Long barcodeId){
//        User user = principalDetails.getUser();
//        TicketDto ticketDto = aggregationFacade.showTicketInfo(user, barcodeId);
//        return ResponseEntity.ok(ticketDto);
//    }

    //4-C 바코드티켓
    @GetMapping("/{barcodeId}")
    public ResponseEntity<TicketDto> showTicketInfo(
            @PathVariable Long barcodeId){
        TicketDto ticketDto = aggregationFacade.showTicketInfo(barcodeId);
        return ResponseEntity.ok(ticketDto);
    }

}
