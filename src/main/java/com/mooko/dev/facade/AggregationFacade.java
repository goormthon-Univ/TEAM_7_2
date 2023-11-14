package com.mooko.dev.facade;

import com.mooko.dev.configuration.S3Config;
import com.mooko.dev.domain.*;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.UserInfoDto;
import com.mooko.dev.dto.event.socket.UserEventCheckStatusDto;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.event.ButtonEvent;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AggregationFacade {
    private final EventService eventService;
    private final UserService userService;
    private final EventPhotoService eventPhotoService;
    private final BarcodeService barcodeService;
    private final UserBarcodeService userBarcodeService;
    private final S3Service s3Service;
    private final S3Config s3Config;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * EventController
     */

    //makeNewEvent
    public void makeNewEvent(User tempUser, NewEventDto newEventDto) {
        User user = userService.findUser(tempUser.getId());
        checkUserAlreadyInEvent(user);
        Event event = eventService.makeNewEvent(newEventDto, user);
        userService.addEvent(user, event);
    }

    private void checkUserAlreadyInEvent(User user) {
        Optional.ofNullable(user.getEvent())
                .filter(Event::getActiveStatus)
                .ifPresent(event -> {
                    throw new CustomException(ErrorCode.USER_ALREADY_HAS_EVENT);
                });
    }


    //ShowEventPage
    public EventInfoDto showEventPage(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);

        // 이벤트에 사용자 등록 여부 확인 및 등록
        if (event.getUsers().stream().noneMatch(existingUser -> existingUser.equals(user))) {
            eventService.addUser(user, event);
            userService.addEvent(user, event);
        }

        List<String> profileImageUrlList = event.getUsers().stream()
                .map(User::getProfileUrl)
                .collect(Collectors.toList());

        boolean isRoomMaker = user.equals(event.getRoomMaker());

        List<UserInfoDto> userInfoList = event.getUsers().stream()
                .map(eventUser -> createUserInfoDto(eventUser, event))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return EventInfoDto.builder()
                .profileImgUrlList(profileImageUrlList)
                .isRoomMaker(isRoomMaker)
                .eventName(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .userInfo(userInfoList)
                .build();
    }

    private UserInfoDto createUserInfoDto(User eventUser, Event event) {
        List<EventPhoto> eventPhotoList = eventPhotoService.findUserEventPhotoList(eventUser, event);
        List<String> eventPhotoUrlList = eventPhotoList.stream()
                .map(EventPhoto::getUrl)
                .collect(Collectors.toList());

        if (eventPhotoUrlList.isEmpty()) {
            return null;
        }

        return UserInfoDto.builder()
                .userId(eventUser.getId().toString())
                .nickname(eventUser.getNickname())
                .imageUrlList(eventPhotoUrlList)
                .checkStatus(eventUser.getCheckStatus())
                .imageCount(eventPhotoUrlList.size())
                .build();
    }



    //updateEventName
    public void updateEventName(User tmpUser, UpdateEventNameDto updateEventNameDto, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        checkUserRoomMaker(user, event);
        eventService.updateEventName(updateEventNameDto.getEventName(), event);
    }


    //updateEventDate
    public void updateEventDate(User tmpUser, UpdateEventDateDto updateEventDateDto, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        checkUserRoomMaker(user, event);
        eventService.updateEventDate(updateEventDateDto, event);
    }

    //makeNewEventBarcode
    public Long makeNewEventBarcode(User tmpUser, Long eventId) throws IOException {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        checkUserRoomMaker(user, event);
        List<String> eventPhotoList = eventPhotoService.findAllEventPhotoList(event);
        checkEventPhotoCount(event, 0, true);

        String barcodeFileName = s3Service.makefileName();

        File barcodeFile = barcodeService.makeNewBarcode(eventPhotoList);
        String fullPath = s3Service.putFileToS3(barcodeFile, barcodeFileName, s3Config.getBarcodeDir());

        Barcode barcode = barcodeService.saveBarcode(
                fullPath,
                event.getTitle(),
                event.getStartDate(),
                event.getEndDate(),
                BarcodeType.EVENT,
                event);
        userBarcodeService.makeUserBarcode(event.getUsers(), barcode);
        eventService.addBarcode(event, barcode);
        eventService.updateEventStatus(event, false);
        return barcode.getId();
    }

    private void checkUserRoomMaker(User user, Event event) {
        if (!event.getRoomMaker().equals(user)) {
            throw new CustomException(ErrorCode.NOT_ROOM_MAKER);
        }
    }


    //updateUserEventPhoto
    public void updateUserEventPhoto(User tmpUser, Long eventId, List<File> newPhotoList) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if (!event.getActiveStatus()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        checkEventPhotoCount(event, newPhotoList.size(), false);
        List<EventPhoto> userEventPhotoList = eventPhotoService.findUserEventPhotoList(user, event);
        userEventPhotoList.forEach(eventPhoto -> s3Service.deleteFromS3(eventPhoto.getUrl()));
        eventPhotoService.deleteEventPhoto(userEventPhotoList);

        List<String> newPhotoUrlList = newPhotoList.parallelStream()
                .map(newPhoto -> {
                    String fileName = s3Service.makefileName();
                    return s3Service.putFileToS3(newPhoto, fileName, s3Config.getEventImageDir());
                }).collect(Collectors.toList());

        List<EventPhoto> eventPhotoList = eventPhotoService.findUserEventPhotoList(user, event);
        if (!eventPhotoList.isEmpty()) {
            deleteExistingPhotos(eventPhotoList);
        }
        eventPhotoService.makeNewEventPhoto(user, event, newPhotoUrlList);
    }

    public void checkEventPhotoCount(Event event, int additionalCount, boolean checkMinimum) {
        List<String> eventPhotoList = eventPhotoService.findAllEventPhotoList(event);
        int totalSize = eventPhotoList.size() + additionalCount;

        if (checkMinimum && totalSize < 30) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_IS_LESS_THAN);
        }

        if (totalSize > 130) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_EXCEED);
        }
    }



    //deleteUserEventPhoto
    public void deleteUserEventPhoto(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if (!event.getActiveStatus()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        List<EventPhoto> eventPhotoList = eventPhotoService.findUserEventPhotoList(user, event);
        if (!eventPhotoList.isEmpty()) {
            deleteExistingPhotos(eventPhotoList);
        }
    }

    private void deleteExistingPhotos(List<EventPhoto> eventPhotoList) {
        eventPhotoList.forEach(eventPhoto -> {
            s3Service.deleteFromS3(eventPhoto.getUrl());
        });
        eventPhotoService.deleteEventPhoto(eventPhotoList);
    }


    /**
     *
     * SocketController
     */

    //updateUserEventCheckStatus
    public UserEventCheckStatusDto updateUserEventCheckStatus(UserEventCheckStatusDto userEventCheckStatusDto, Long eventId) {
        User user = userService.findUser(Long.parseLong(userEventCheckStatusDto.getUserId()));
        userService.updateCheckStatus(user, userEventCheckStatusDto.isCheckStatus());
        Event event = eventService.findEvent(eventId);
        checkEventButtonStatus(event);
        return UserEventCheckStatusDto.builder()
                .checkStatus(user.getCheckStatus())
                .userId(user.getId().toString())
                .build();
    }

    //버튼 이벤트처리
    private void checkEventButtonStatus(Event event) {
        boolean allUsersChecked = event.getUsers().stream()
                .allMatch(User::getCheckStatus);

        eventPublisher.publishEvent(
                ButtonEvent.builder()
                        .buttonStatus(allUsersChecked)
                        .eventId(event.getId().toString())
                        .build());
    }




    /**
     * UserController
     */

    //showUserEventStatus
    public UserEventStatusDto showUserEventStatus(User tmpUser) {
        User user = userService.findUser(tmpUser.getId());

        return Optional.ofNullable(user.getEvent())
                .filter(Event::getActiveStatus)
                .map(event -> UserEventStatusDto.builder()
                        .isExistEvent(true)
                        .eventId(event.getId().toString())
                        .build())
                .orElseGet(() -> UserEventStatusDto.builder()
                        .isExistEvent(false)
                        .eventId(null)
                        .build());
    }

}
