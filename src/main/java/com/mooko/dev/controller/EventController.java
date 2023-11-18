package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.EventPhotoDto;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.res.EventList;
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


    //5. 이벤트 목록
    @GetMapping("/event-list")
    public ResponseEntity<EventList> showEventList(
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        User user = principalDetails.getUser();
        EventList eventList = aggregationFacade.showEventList(user);
        return ResponseEntity.ok(eventList);
    }

    //5-1. 이벤트 블록 (accessToken x)
    @GetMapping("/{eventId}")
    public ResponseEntity<EventPhotoResDto> showEventBlock(
            @PathVariable Long eventId
    ){
        EventPhotoResDto eventPhotoResDto = aggregationFacade.showEventBlock(eventId);
        return ResponseEntity.ok(eventPhotoResDto);
    }



    //5-2. 이벤트 사진 등록/수정
    @PostMapping("/{eventId}/save-photo")
    public ResponseEntity<Void> updateUserEventPhoto(
            @PathVariable Long eventId,
            @ModelAttribute EventPhotoDto eventPhotoDto)
    {
        aggregationFacade.updateEventPhoto(eventId, eventPhotoDto.getImageList());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //5.4. 이벤트생성
    @PostMapping("/new-event")
    public ResponseEntity<Void> makeNewEvent(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody NewEventDto newEventDto
    ) {
        User user = principalDetails.getUser();
        aggregationFacade.makeNewEvent(user, newEventDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //5.3. 이벤트 바코드 생성
    @PostMapping("/{eventId}/result")
    public ResponseEntity<Void> makeNewEventBarcode(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId
    ) throws IOException, InterruptedException {
        User user = principalDetails.getUser();
        aggregationFacade.makeNewEventBarcode(user, eventId);
        return ResponseEntity.status(HttpStatus.OK).build();

    }






}
