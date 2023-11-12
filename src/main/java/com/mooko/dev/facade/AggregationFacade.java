package com.mooko.dev.facade;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.NewEventDto;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.EventService;
import com.mooko.dev.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregationFacade {
    private final EventService eventService;
    private final UserService userService;

    public void makeNewEvent(User tempUser, NewEventDto newEventDto) {
        User user = userService.findUser(tempUser.getId());
        checkUserEventStatus(user);
        eventService.makeNewEvent(newEventDto, user);
    }

    private void checkUserEventStatus(User user) {
        Optional.ofNullable(user.getEvent())
                .ifPresent(e -> {
                    throw new CustomException(ErrorCode.USER_ALREADY_HAS_EVENT);
                });
    }

}
