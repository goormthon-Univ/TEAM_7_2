package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.UserInfoDto;
import com.mooko.dev.facade.AggregationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/event")
public class EventController {

    private final AggregationFacade aggregationFacade;

    @PostMapping("/new-event")
    public ResponseEntity<Void> makeNewEvent(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody NewEventDto newEventDto){
        User user = principalDetails.getUser();
        aggregationFacade.makeNewEvent(user, newEventDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventInfoDto> showEventPage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long eventId) {
        User user = principalDetails.getUser();
        EventInfoDto eventInfoDto = aggregationFacade.showEventPage(user, eventId);
        return ResponseEntity.ok(eventInfoDto);
    }

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

}
