package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.EventPhotoDto;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.BarcodeIdDto;
import com.mooko.dev.dto.event.res.EventIdDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.EventPhotoResDto;
import com.mooko.dev.facade.AggregationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/event")
public class EventController {

    private final AggregationFacade aggregationFacade;

    //3-A. 이벤트생성
    @PostMapping("/new-event")
    public ResponseEntity<EventIdDto> makeNewEvent(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody NewEventDto newEventDto
    ) {
        User user = principalDetails.getUser();
        Long eventId = aggregationFacade.makeNewEvent(user, newEventDto);
        return ResponseEntity.ok(EventIdDto.builder().eventId(eventId.toString()).build());
    }

    //3-C. 이벤트 바코드 생성
    @PostMapping("/{eventId}/result")
    public ResponseEntity<BarcodeIdDto> makeNewEventBarcode(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId
    ) throws IOException {
        User user = principalDetails.getUser();
        Long barcodeId = aggregationFacade.makeNewEventBarcode(user, eventId);
        return ResponseEntity.ok(BarcodeIdDto.builder().barcodeId(barcodeId.toString()).build());

    }

    //3-0. 이벤트 페이지
    @GetMapping("/{eventId}")
    public ResponseEntity<EventInfoDto> showEventPage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId) {
        User user = principalDetails.getUser();
        EventInfoDto eventInfoDto = aggregationFacade.showEventPage(user, eventId);
        return ResponseEntity.ok(eventInfoDto);
    }

    //3-1. 이벤트 이름 수정
    @PutMapping("/{eventId}/event-name")
    public ResponseEntity<Void> updateEventName(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody UpdateEventNameDto updateEventNameDto,
            @PathVariable Long eventId
    ) {
        User user = principalDetails.getUser();
        aggregationFacade.updateEventName(user, updateEventNameDto, eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //3-2. 이벤트 기간 수정
    @PutMapping("/{eventId}/event-date")
    public ResponseEntity<Void> updateEventDate(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody UpdateEventDateDto updateEventDateDto,
            @PathVariable Long eventId
    ) {
        User user = principalDetails.getUser();
        aggregationFacade.updateEventDate(user, updateEventDateDto, eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //3-4. 이벤트 사진
    @GetMapping("/{eventId}/image-list")
    public ResponseEntity<EventPhotoResDto> showUserEventPhoto(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId
    ) {
        User user = principalDetails.getUser();
        EventPhotoResDto eventPhotoResDto = aggregationFacade.showUserEventPhoto(user, eventId);
        return ResponseEntity.ok(eventPhotoResDto);
    }

    //3-5. 이벤트 사진 등록/수정
    @PostMapping("/{eventId}")
    public ResponseEntity<Void> updateUserEventPhoto(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId,
            @ModelAttribute EventPhotoDto eventPhotoDto)
    {
        User user = principalDetails.getUser();
        aggregationFacade.updateUserEventPhoto(user, eventId, eventPhotoDto.getImageList());
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    //3-6. 이벤트 사진 리스트 삭제
    @DeleteMapping("/{eventId}/{userId}/image-list")
    public ResponseEntity<EventInfoDto> deleteUserEventPhoto(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long userId,
            @PathVariable Long eventId
    )
    {
        User user = principalDetails.getUser();
        EventInfoDto eventInfoDto = aggregationFacade.deleteUserEventPhoto(user, eventId, userId);

        return ResponseEntity.ok(eventInfoDto);
    }

    //3-7. 이벤트 나가기
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteUserEvent(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId
    ) {
        User user = principalDetails.getUser();
        aggregationFacade.deleteUserEvent(user, eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
