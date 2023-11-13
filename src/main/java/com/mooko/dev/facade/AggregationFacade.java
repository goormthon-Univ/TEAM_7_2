package com.mooko.dev.facade;

import com.mooko.dev.configuration.S3Config;
import com.mooko.dev.domain.*;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.UserInfoDto;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
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


    /**
     * EventController
     */

    //makeNewEvent
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


    //ShowEventPage
    public EventInfoDto showEventPage(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if(event.getUsers().stream().noneMatch(existingUser -> existingUser.equals(user))){
            eventService.addUser(user, event);
            userService.addEvent(user, event);
        }

        List<String> profilImgeUrlList = event.getUsers().stream().map(User::getProfileUrl).toList();
        boolean isRoomMaker = user.equals(event.getRoomMaker());

        List<UserInfoDto> userInfoList = event.getUsers().stream()
                .map(eventUser -> {
                    List<EventPhoto> eventPhotoList = eventPhotoService.findUserEventPhotoList(eventUser, event);
                    List<String> evnetPhotoUrlList = eventPhotoList.stream().map(EventPhoto::getUrl).toList();


                    if (evnetPhotoUrlList.isEmpty()) {
                        return null;
                    }

                    return UserInfoDto.builder()
                            .userId(eventUser.getId().toString())
                            .nickname(eventUser.getNickname())
                            .imageUrlList(evnetPhotoUrlList)
                            .checkStatus(eventUser.getCheckStatus())
                            .imageCount(evnetPhotoUrlList.size())
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

    //makeNewBarcode
    public Long makeNewBarcode(User tmpUser, Long eventId) throws IOException {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        checkUserRoomMaker(user, event);
        List<String> eventPhotoList = eventPhotoService.findAllEventPhotoList(event);

        String barcodeFileName = s3Service.makefileName();
        String barcodeFilePath = s3Config.getBarcodeDir() + barcodeFileName;

        File barcodeFile = barcodeService.makeNewBarcode(eventPhotoList, barcodeFilePath);
        s3Service.putFileToS3(barcodeFile, barcodeFileName, s3Config.getBarcodeDir());

        Barcode barcode = barcodeService.saveBarcode(
                barcodeFilePath,
                event.getTitle(),
                event.getStartDate(),
                event.getEndDate(),
                BarcodeType.EVENT,
                event);
        userBarcodeService.makeUserBarcode(event.getUsers(), barcode);
        eventService.updateEventStatus(event, false);
        return barcode.getId();
    }

    private void checkUserRoomMaker(User user, Event event) {
        if (!event.getRoomMaker().equals(user)) {
            throw new CustomException(ErrorCode.NOT_ROOM_MAKER);
        }
    }


    //showUserEventStatus
    public UserEventStatusDto showUserEventStatus(User tmpUser) {
        User user = userService.findUser(tmpUser.getId());
        if (user.getEvent() != null) {
            return UserEventStatusDto.builder()
                    .isExistEvent(true)
                    .eventId(user.getEvent().getId().toString())
                    .build();
        }

        return UserEventStatusDto.builder()
                .isExistEvent(false)
                .eventId(null)
                .build();
    }


    //updateUserEventPhoto
    public void updateUserEventPhoto(User tmpUser, Long eventId, List<File> newPhotoList) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if (!event.getActiveStatus()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

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
}
