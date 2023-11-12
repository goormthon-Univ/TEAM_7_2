package com.mooko.dev.facade;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.UserInfoDto;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.EventPhotoService;
import com.mooko.dev.service.EventService;
import com.mooko.dev.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AggregationFacade {
    private final EventService eventService;
    private final UserService userService;
    private final EventPhotoService eventPhotoService;

    /**
     * EventController
     */
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

    public EventInfoDto showEventPage(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        List<String> profilImgeUrlList = event.getUsers().stream().map(User::getProfileUrl).toList();
        boolean isRoomMaker = user.equals(event.getRoomMaker());

        List<UserInfoDto> userInfoList = event.getUsers().stream()
                .map(eventUser -> {
                    List<String> userEventPhotoList = eventPhotoService.findUserEventPhotoList(eventUser, event);
                    if(userEventPhotoList.isEmpty()){
                        return null;
                    }

                    return UserInfoDto.builder()
                            .userId(eventUser.getId().toString())
                            .nickname(eventUser.getNickname())
                            .imageUrlList(userEventPhotoList)
                            .checkStatus(eventUser.getCheckStatus())
                            .imageCount(userEventPhotoList.size())
                            .build();
                })
                .toList();

        return EventInfoDto.builder()
                .profileImgUrlList(profilImgeUrlList)
                .isRoomMaker(isRoomMaker)
                .eventName(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .userInfo(userInfoList)
                .build();
    }

    public void updateEventName(User tmpUser, UpdateEventNameDto updateEventNameDto, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if(!event.getRoomMaker().equals(user)){
            throw new CustomException(ErrorCode.NOT_ROOM_MAKER);
        }
        eventService.updateEventName(updateEventNameDto.getEventName(), event);
    }
}
