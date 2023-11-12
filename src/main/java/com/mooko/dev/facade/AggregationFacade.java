package com.mooko.dev.facade;

import com.mooko.dev.domain.User;
import com.mooko.dev.service.EventService;
import com.mooko.dev.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregationFacade {
    private final EventService eventService;
    private final UserService userService;


}
