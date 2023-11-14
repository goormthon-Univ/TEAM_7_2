package com.mooko.dev.facade;

import com.mooko.dev.configuration.S3Config;
import com.mooko.dev.domain.*;
import com.mooko.dev.dto.day.req.BarcodeDateDto;
import com.mooko.dev.dto.day.res.CalendarDto;
import com.mooko.dev.dto.day.res.DayDto;
import com.mooko.dev.dto.day.res.ThumbnailDto;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.EventInfoDto;
import com.mooko.dev.dto.event.res.UserInfoDto;
import com.mooko.dev.dto.event.socket.UserEventCheckStatusDto;
import com.mooko.dev.dto.user.req.UserNewInfoDto;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.dto.user.res.UserPassportDto;
import com.mooko.dev.event.ButtonEvent;
import com.mooko.dev.event.LeaveEvent;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final DayService dayService;
    private final DayPhotoService dayPhotoService;

    private final int MINIMUM_EVENT_PHOTO_COUNT = 2;
    private final int MAX_EVENT_PHOTO_COUNT = 130;



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
            eventService.addEventUser(user, event);
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
                .loginUserId(user.getId().toString())
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


        deleteExistingPhotoOrEventUser(user, event, false, false);
        eventPhotoService.makeNewEventPhoto(user, event, newPhotoUrlList);
    }

    public void checkEventPhotoCount(Event event, int additionalCount, boolean checkMinimum) {
        List<String> eventPhotoList = eventPhotoService.findAllEventPhotoList(event);
        int totalSize = eventPhotoList.size() + additionalCount;

        if (checkMinimum && totalSize < MINIMUM_EVENT_PHOTO_COUNT) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_IS_LESS_THAN);
        }

        if (totalSize > MAX_EVENT_PHOTO_COUNT) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_EXCEED);
        }
    }



    //deleteUserEventPhoto
    public void deleteUserEventPhoto(User tmpUser, Long eventId, Long tmpUserId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);

        if (!event.getActiveStatus()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (!Objects.equals(tmpUserId, user.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }
        deleteExistingPhotoOrEventUser(user, event, false, false);

    }

    //deleteUserEvent
    public void deleteUserEvent(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if (event.getRoomMaker().equals(user)) {
            eventPublisher.publishEvent(
                    LeaveEvent.builder()
                            .eventStatus(true)
                            .eventId(event.getId().toString())
                            .build()
            );
            deleteExistingPhotoOrEventUser(user, event, true, true);
            return;
        }
        deleteExistingPhotoOrEventUser(user, event, true, false);
    }

    private void deleteExistingPhotoOrEventUser(User user, Event event, boolean isLeaveEvent, boolean isDeleteEvent) {
        List<EventPhoto> eventPhotoList = eventPhotoService.findUserEventPhotoList(user, event);
        if (!eventPhotoList.isEmpty()) {
            eventPhotoList.forEach(eventPhoto -> {
                s3Service.deleteFromS3(eventPhoto.getUrl());
            });
            eventPhotoService.deleteEventPhoto(eventPhotoList);
            if (isLeaveEvent) {
                eventService.deleteEventUser(user, event);
                userService.deleteEvent(user);
                if(isDeleteEvent){
                    eventService.deleteEvent(event);
                }
            }
        }
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

    //바코드 생성버튼 이벤트처리
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
     *
     * DayController
     */

    // showUserCalendar
    public CalendarDto showCalendar(User tmpUser, String startDate, String endDate){
        User user = userService.findUser(tmpUser.getId());

        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);

        List<ThumbnailDto> thumbnailInfoList = new ArrayList<>();

        Boolean buttonStatus = true;

        while (!startLocalDate.isAfter(endLocalDate)) {
            startLocalDate = startLocalDate.plusDays(1);

            int year = startLocalDate.getYear();
            int month = startLocalDate.getMonthValue();
            int day = startLocalDate.getDayOfMonth();

            Day currentDay = dayService.findDayId(user,year,month,day);

            DayPhoto dayPhoto = dayPhotoService.findThumnail(currentDay);
            if (dayPhoto.getUrl()==null){
                buttonStatus=false;
            }

            ThumbnailDto thumbnailDto = ThumbnailDto.builder()
                    .thumbnailUrl(dayPhoto.getUrl())
                    .date(startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .build();
            thumbnailInfoList.add(thumbnailDto);
        }

        return CalendarDto.builder()
                .thumbnailInfoList(thumbnailInfoList)
                .buttonStatus(buttonStatus)
                .build();
    }

    // showDayPost
    public DayDto showDay(User tmpUser, String date){
        User user = userService.findUser(tmpUser.getId());

        LocalDate currentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();

        Day currentDay = dayService.findDayId(user,year,month,day);

        if (currentDay.getId()==null){
            currentDay = dayService.makeDay(user,year,month,day);
        }

        String memo = dayService.findMemo(currentDay);
        List<DayPhoto> dayImageList = dayPhotoService.findDayPhotoList(currentDay);

        List<String> dayPhotoUrlList = dayImageList.stream().map(DayPhoto::getUrl).toList();

        return DayDto.builder()
                .dayImageList(dayPhotoUrlList)
                .memo(memo)
                .build();
    }

    // post,updateDayPost
    public void updateDay(User tmpUser, String date, String memo, File thumbnail, List<File> newDayPhotoList){
        User user = userService.findUser(tmpUser.getId());

        LocalDate currentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();

        Day currentDay = dayService.findDayId(user,year,month,day);

        dayService.updateMemo(currentDay, memo);

        // Thumbnail
        String newThumbnailUrl = null;
        if (thumbnail!=null){
            String fileName = s3Service.makefileName();
            newThumbnailUrl = s3Service.putFileToS3(thumbnail, fileName, s3Config.getEventImageDir());
        }
        DayPhoto dayThumbnail = dayPhotoService.findThumnail(currentDay);
        if (dayThumbnail.getUrl()!=null) {
            s3Service.deleteFromS3(dayThumbnail.getUrl());
            dayPhotoService.deleteThumbnail(dayThumbnail);
        }
        dayPhotoService.makeNewThumbnail(currentDay,newThumbnailUrl, true);

        // Photos except thumbnail
        List<String> newDayPhotoUrlList = new ArrayList<>();;
        if (newDayPhotoList!=null){
            newDayPhotoUrlList = newDayPhotoList.parallelStream()
                    .map(newPhoto -> {
                        String fileName = s3Service.makefileName();
                        return s3Service.putFileToS3(newPhoto, fileName, s3Config.getEventImageDir());
                    }).collect(Collectors.toList());
        }

        List<DayPhoto> dayPhotoList = dayPhotoService.findDayPhotoList(currentDay);
        if (!dayPhotoList.isEmpty()) {
            deleteExistingDayPhotos(dayPhotoList);
        }
        dayPhotoService.makeNewDayPhoto(currentDay,newDayPhotoUrlList, false);
    }

    private void deleteExistingDayPhotos(List<DayPhoto> dayPhotoList) {
        dayPhotoList.forEach(eventPhoto -> {
            s3Service.deleteFromS3(eventPhoto.getUrl());
        });
        dayPhotoService.deleteDayPhotos(dayPhotoList);
    }

    // makeNewDayBarcode
    public Long makeNewDayBarcode(User tmpUser, BarcodeDateDto barcodeDateDto) throws IOException {
        User user = userService.findUser(tmpUser.getId());

        int intYear = Integer.parseInt(barcodeDateDto.getYear());
        int intMonth = Integer.parseInt(barcodeDateDto.getMonth());

        List<Day> dayList = dayService.findDayIdList(user,intYear,intMonth);
        List<String> allDayPhotos = new ArrayList<>();
        for (Day day : dayList) {
            List<String> photosForDay = dayPhotoService.findDayPhotoUrlList(day);
            allDayPhotos.addAll(photosForDay);
        }

        String barcodeFileName = s3Service.makefileName();

        File barcodeFile = barcodeService.makeNewBarcode(allDayPhotos);
        String fullPath = s3Service.putFileToS3(barcodeFile, barcodeFileName, s3Config.getBarcodeDir());

        // 해당 년월의 startDate,endDate 구하기
        LocalDate firstDayOfMonth = LocalDate.of(intYear, intMonth, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDayOfMonth.format(formatter);
        String endDate = lastDayOfMonth.format(formatter);

        Barcode barcode = barcodeService.saveBarcode(
                fullPath,
                barcodeDateDto.getYear()+"년 "+barcodeDateDto.getMonth()+"월",
                startDate,
                endDate,
                BarcodeType.DAY,
                null);
        userBarcodeService.makeUserDayBarcode(user, barcode);
        return barcode.getId();
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

    //test
    public User test(Long userId){
        return userService.findUser(userId);
    }


    // showUserInfo
    public UserPassportDto showUserInfo(User tmpUser){
        User user = userService.findUser(tmpUser.getId());

        List<UserBarcode> userBarcodeList = userBarcodeService.findUserBarcodeList(user);

        String recentBarcodeImg = userBarcodeList.stream()
                .map(UserBarcode::getBarcode)
                .sorted(Comparator.comparing(Barcode::getCreatedAt).reversed())
                .map(Barcode::getBarcodeUrl)
                .findFirst()
                .orElse(null);

        List<String> recentBarcodeTitleList = userBarcodeList.stream()
                .map(UserBarcode::getBarcode)
                .sorted(Comparator.comparing(Barcode::getCreatedAt).reversed())
                .map(Barcode::getTitle)
                .limit(3)
                .collect(Collectors.toList());

        return UserPassportDto.builder()
                .nickname(user.getNickname())
                .birth(user.getBirth())
                .gender(user.getGender())
                .dateOfIssue(user.getDateOfIssue())
                .barcodeCount(userBarcodeList.size())
                .profileUrl(user.getProfileUrl())
                .recentBarcodeImg(recentBarcodeImg)
                .recentBarcodeTitleList(recentBarcodeTitleList)
                .modalActive(user.getModalActive())
                .build();
    }

    // showUserInfo
    @Value("${cloud.aws.s3.default-img}")
    private String USER_DEFAULT_PROFILE_IMAGE;
    public void updateUserInfo(User tmpUser, UserNewInfoDto userNewInfoDto){
        User user = userService.findUser(tmpUser.getId());

        String fileName = s3Service.makefileName();
        String newProfileImgUrl = s3Service.putFileToS3(userNewInfoDto.getProfileImage(), fileName, s3Config.getEventImageDir());

        if (user.getProfileUrl()!=USER_DEFAULT_PROFILE_IMAGE) {
            s3Service.deleteFromS3(user.getProfileUrl());
        }
        user.updateUserInfo(newProfileImgUrl,userNewInfoDto.getNickname(), userNewInfoDto.getBirth(),
                userNewInfoDto.getGender(), false);
    }
}
