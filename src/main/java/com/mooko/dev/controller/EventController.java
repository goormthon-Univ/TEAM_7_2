package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.NewEventDto;
import com.mooko.dev.facade.AggregationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
