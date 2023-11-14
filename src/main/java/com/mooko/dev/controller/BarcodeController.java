package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.barcode.res.BarcodeInfoDto;
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

    @GetMapping("/list")
    public ResponseEntity<List<BarcodeInfoDto>> showBarcodeList(
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        User user = principalDetails.getUser();
        List<BarcodeInfoDto> barcodeList = aggregationFacade.showBarcodeInfo(user);
        return ResponseEntity.ok(barcodeList);
    }

    @GetMapping("/{barcodeId}/my-ticket")
    public ResponseEntity<TicketDto> showMyTicket(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long barcodeId){
        User user = principalDetails.getUser();
        TicketDto ticketDto = aggregationFacade.showTicketInfo(user, barcodeId);
        return ResponseEntity.ok(ticketDto);
    }

    @GetMapping("/{barcodeId}/guest-ticket")
    public ResponseEntity<TicketDto> showGuestTicket(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long barcodeId){
        User user = principalDetails.getUser();
        TicketDto ticketDto = aggregationFacade.showTicketInfo(user, barcodeId);
        return ResponseEntity.ok(ticketDto);
    }

}
