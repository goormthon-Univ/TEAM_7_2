package com.mooko.dev.facade;

import com.mooko.dev.configuration.S3Config;
import com.mooko.dev.domain.*;
import com.mooko.dev.dto.barcode.res.BarcodeInfoDto;
import com.mooko.dev.dto.barcode.res.ImageInfoDto;
import com.mooko.dev.dto.barcode.res.TicketDto;
import com.mooko.dev.dto.day.req.BarcodeDateDto;
import com.mooko.dev.dto.day.req.DayPhotoDto;
import com.mooko.dev.dto.day.res.ButtonStatus;
import com.mooko.dev.dto.day.res.CalendarResDto;
import com.mooko.dev.dto.day.res.DayDto;
import com.mooko.dev.dto.day.res.ThumbnailDto;
import com.mooko.dev.dto.event.req.EventPhotoDto;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.dto.event.req.UpdateEventNameDto;
import com.mooko.dev.dto.event.res.*;
import com.mooko.dev.dto.event.socket.UserEventCheckStatusDto;
import com.mooko.dev.dto.user.req.UserNewInfoDto;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.dto.user.res.UserPassportDto;
import com.mooko.dev.event.ButtonEvent;
import com.mooko.dev.event.LeaveEvent;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
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

    private final int MIN_PHOTO_COUNT = 2;
    private final int MAX_PHOTO_COUNT = 130;

    /**
     * EventController
     */

    //makeNewEvent 새로운 버전
    public void makeNewEvent(User tempUser, NewEventDto newEventDto) {
        User user = userService.findUser(tempUser.getId());
        if(newEventDto.getTitle()==null|| newEventDto.getTitle().equals("")){throw new CustomException(ErrorCode.EVENT_TITLE_EMPTY);}
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = Instant.parse(newEventDto.getStartDate()).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDate = Instant.parse(newEventDto.getEndDate()).atZone(ZoneId.of("UTC")).toLocalDate();

        // 시작 날짜와 종료 날짜 비교
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.START_DATE_EXCEED_END_DATE);
        }
        Event event = eventService.makeNewEvent(newEventDto.getTitle(),startDate.toString(), endDate.toString());
        userService.addEvent(user, event);
    }




    //ShowEventPage
    public EventInfoDto showEventPage(User tmpUser, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if (!user.getEvent().getId().equals(event.getId())) {
            throw new CustomException(ErrorCode.USER_ALREADY_HAS_EVENT);
        }
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
                .roomMaker(isRoomMaker)
                .eventName(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .loginUserId(user.getId().toString())
                .userCount(event.getUsers().size())
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

        if(updateEventNameDto.getEventName()==null|| updateEventNameDto.getEventName().equals("")){throw new CustomException(ErrorCode.EVENT_TITLE_EMPTY);}

        checkUserRoomMaker(user, event);
        eventService.updateEventName(updateEventNameDto.getEventName(), event);
    }



    //updateEventDate
    public void updateEventDate(User tmpUser, UpdateEventDateDto updateEventDateDto, Long eventId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        checkUserRoomMaker(user, event);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = Instant.parse(updateEventDateDto.getStartDate()).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDate = Instant.parse(updateEventDateDto.getEndDate()).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.START_DATE_EXCEED_END_DATE);
        }
        eventService.updateEventDate(event, startDate.toString(), endDate.toString());
    }

    //makeNewEventBarcode
    public void makeNewEventBarcode(User tmpUser, Long eventId) throws IOException, InterruptedException {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        if(!event.getUser().equals(user)){
            throw new CustomException(ErrorCode.NOT_OWNER_ACCESS);
        }
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
        userBarcodeService.makeUserBarcode(event.getUser(), barcode);
        eventService.addBarcode(event, barcode);
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

        List<EventPhoto> userEventPhotoList = eventPhotoService.findUserEventPhotoList(user, event);
        userEventPhotoList.forEach(eventPhoto -> s3Service.deleteFromS3(eventPhoto.getUrl()));
        eventPhotoService.deleteEventPhoto(userEventPhotoList);
        deleteExistingPhotoOrEventUser(user, event, false, false);

        if(newPhotoList != null){
            checkEventPhotoCount(event, newPhotoList.size(), false);
            List<String> newPhotoUrlList = newPhotoList.parallelStream()
                    .map(newPhoto -> {
                        String fileName = s3Service.makefileName();
                        return s3Service.putFileToS3(newPhoto, fileName, s3Config.getEventImageDir());
                    }).toList();
            eventPhotoService.makeNewEventPhoto(user, event, newPhotoUrlList);
        }

    }

    public void checkEventPhotoCount(Event event, int additionalCount, boolean checkMinimum) {
        List<String> eventPhotoList = eventPhotoService.findAllEventPhotoList(event);
        int totalSize = eventPhotoList.size() + additionalCount;

        if (checkMinimum && totalSize < MIN_PHOTO_COUNT) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_IS_LESS_THAN);
        }

        if (totalSize > MAX_PHOTO_COUNT) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_EXCEED);
        }
    }



    //deleteUserEventPhoto
    public EventInfoDto deleteUserEventPhoto(User tmpUser, Long eventId, Long tmpUserId) {
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);

        if (!event.getActiveStatus()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (!Objects.equals(tmpUserId, user.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }
        deleteExistingPhotoOrEventUser(user, event, false, false);


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
                .roomMaker(isRoomMaker)
                .eventName(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .loginUserId(user.getId().toString())
                .userCount(event.getUsers().size())
                .userInfo(userInfoList)
                .build();

    }

    //showUserEventPhoto
    public EventPhotoResDto showUserEventPhoto(User tmpUser, Long eventId){
        User user = userService.findUser(tmpUser.getId());
        Event event = eventService.findEvent(eventId);
        List<EventPhoto> eventPhotoByEvent = eventPhotoService.findEventPhotoByEvent(event);
        List<String> imageUrlList = eventPhotoByEvent.stream().map(EventPhoto::getUrl).toList();
        return EventPhotoResDto
                .builder()
                .eventId(eventId.toString())
                .imageUrlList(imageUrlList)
                .build();
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
            eventPhotoList.forEach(eventPhoto -> s3Service.deleteFromS3(eventPhoto.getUrl()));
            eventPhotoService.deleteEventPhoto(eventPhotoList);

        }
        if (isLeaveEvent) {
            eventService.deleteEventUser(user, event);
            userService.deleteEvent(user);
            if (isDeleteEvent) {
                eventService.deleteEvent(event);
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
        List<User> userByEvent = userService.findUserByEvent(event);
        boolean allUsersChecked = userByEvent.stream()
                .allMatch(User::getCheckStatus);
        log.info("바코드 생성버튼 상태입니다 id = {}, status = {} ", event.getId(), allUsersChecked);
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
    public CalendarResDto showCalendar(User tmpUser, String startDate, String endDate, String currentYear, String currentMonth){
        User user = userService.findUser(tmpUser.getId());

        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);

        List<ThumbnailDto> thumbnailInfoList = new ArrayList<>();

        ButtonStatus buttonStatus = ButtonStatus.ACTIVE;
        while (!startLocalDate.isAfter(endLocalDate)) {
            startLocalDate = startLocalDate.plusDays(1);

            int year = startLocalDate.getYear();
            int month = startLocalDate.getMonthValue();
            int day = startLocalDate.getDayOfMonth();

            Optional<Day> currentDay = dayService.findDayIdOptinal(user,year,month,day);
            DayPhoto dayPhoto = null;
            if (currentDay.isPresent()){
                dayPhoto = dayPhotoService.findThumbnail(currentDay.get());
            }

            if (dayPhoto!=null){
                ThumbnailDto thumbnailDto = ThumbnailDto.builder()
                        .thumbnailUrl(dayPhoto.getUrl())
                        .date(startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .build();
                thumbnailInfoList.add(thumbnailDto);
            } else {
                if (Integer.toString(year).equals(currentYear) && Integer.toString(month).equals(currentMonth)){
                    buttonStatus = ButtonStatus.INACTIVE;
                }
                ThumbnailDto thumbnailDto = ThumbnailDto.builder()
                        .thumbnailUrl(null)
                        .date(startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .build();
                thumbnailInfoList.add(thumbnailDto);
            }
        }

        String title = currentYear+"년 "+currentMonth+"월";

        if (buttonStatus!=ButtonStatus.INACTIVE){
            if(findBarcodeByTitle(user,title)!=null){
                buttonStatus=ButtonStatus.ACTIVE_WITH_MODAL;
            }else{
                buttonStatus=ButtonStatus.ACTIVE;
            }
        }

        return CalendarResDto.builder()
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

        if(currentDay==null){throw new CustomException(ErrorCode.DAY_NOT_FOUND);}

        String memo = dayService.findMemo(currentDay);
        List<DayPhoto> dayImageList = dayPhotoService.findDayPhotoList(currentDay);

        List<String> dayPhotoUrlList = dayImageList.stream().map(DayPhoto::getUrl).toList();

        return DayDto.builder()
                .dayImageList(dayPhotoUrlList)
                .memo(memo)
                .build();
    }

    // post,updateDayPost
    public void updateDay(User tmpUser, String date, DayPhotoDto dayPhotoDto){
        User user = userService.findUser(tmpUser.getId());

        List<File> newDayPhotoList = Stream.of(dayPhotoDto.getPhoto1(),dayPhotoDto.getPhoto2(), dayPhotoDto.getPhoto3())
                .filter(photo -> photo != null && photo.length() > 0)
                .collect(Collectors.toList());

        LocalDate currentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();

        Day currentDay = dayService.findDayId(user,year,month,day);
        if (currentDay==null){
            currentDay = dayService.makeDay(user,year,month,day);
        }

        dayService.updateMemo(currentDay, dayPhotoDto.getMemo());

        List<DayPhoto> dayPhotoList = dayPhotoService.findDayPhotoList(currentDay);
        if (dayPhotoList!=null) {
            deleteExistingDayPhotos(dayPhotoList);
        }

        // Thumbnail
        updateThumbnail(dayPhotoDto.getThumbnail(), currentDay);

        // Photos except thumbnail
        updateDayPhotos(newDayPhotoList, currentDay);
    }

    private void updateThumbnail(File thumbnail, Day day){
        String newThumbnailUrl = null;
        if (thumbnail!=null){
            String fileName = s3Service.makefileName();
            newThumbnailUrl = s3Service.putFileToS3(thumbnail, fileName, s3Config.getDayImageDir());
        }

        if(newThumbnailUrl!=null){ dayPhotoService.makeNewThumbnail(day,newThumbnailUrl,true);}
    }

    private void updateDayPhotos(List<File> newDayPhotoList, Day day){
        List<String> newDayPhotoUrlList = new ArrayList<>();
        if (newDayPhotoList!=null){
            newDayPhotoUrlList = newDayPhotoList.parallelStream()
                    .map(newPhoto -> {
                        String fileName = s3Service.makefileName();
                        return s3Service.putFileToS3(newPhoto, fileName, s3Config.getDayImageDir());
                    }).collect(Collectors.toList());
        }

        if(newDayPhotoList!=null){dayPhotoService.makeNewDayPhoto(day,newDayPhotoUrlList,false);}
    }

    private void deleteExistingDayPhotos(List<DayPhoto> dayPhotoList) {
        dayPhotoList.forEach(eventPhoto -> s3Service.deleteFromS3(eventPhoto.getUrl()));
        dayPhotoService.deleteDayPhotos(dayPhotoList);
    }

    // makeNewDayBarcode
    public Long makeNewDayBarcode(User tmpUser, BarcodeDateDto barcodeDateDto) throws IOException, InterruptedException {
        User user = userService.findUser(tmpUser.getId());

        int intYear = Integer.parseInt(barcodeDateDto.getYear());
        int intMonth = Integer.parseInt(barcodeDateDto.getMonth());

        String barcodeTitle = barcodeDateDto.getYear()+"년 "+barcodeDateDto.getMonth()+"월";

        Barcode pastBarcode = findBarcodeByTitle(user, barcodeTitle);

        if (pastBarcode!=null) {
            UserBarcode userBarcode = userBarcodeService.findUserBarcodeByBarcode(pastBarcode)
                    .stream()
                    .findFirst()
                    .orElseGet(null);
            s3Service.deleteFromS3(pastBarcode.getBarcodeUrl());
            userBarcodeService.deleteUserBarcode(userBarcode);
            barcodeService.deleteBarcode(pastBarcode);
        }

        List<Day> dayList = dayService.findDayIdList(user,intYear,intMonth);
        List<String> allDayPhotos = new ArrayList<>();
        for (Day day : dayList) {
            List<String> photosForDay = dayPhotoService.findDayPhotoUrlList(day);
            allDayPhotos.addAll(photosForDay);
        }

        if (dayList.size()<MIN_PHOTO_COUNT){
            throw new CustomException(ErrorCode.DAY_PHOTO_IS_LESS_THAN);
        } else if (dayList.size()>MAX_PHOTO_COUNT){
            throw new CustomException(ErrorCode.DAY_PHOTO_EXCEED);
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

        Barcode currentBarcode = barcodeService.saveBarcode(
                fullPath,
                barcodeTitle,
                startDate,
                endDate,
                BarcodeType.DAY,
                null);
        userBarcodeService.makeUserDayBarcode(user, currentBarcode);
        return currentBarcode.getId();
    }

    private Barcode findBarcodeByTitle(User user, String title){
        List<UserBarcode> userBarcodeList = userBarcodeService.findUserBarcodeList(user);
        Barcode barcode = barcodeService.findBarcodeByTitle(userBarcodeList, title);
        return barcode;
    }

    /**
     * UserController
     */

    //showUserEventStatus
    public UserEventStatusDto showUserEventStatus(User tmpUser) {
        User user = userService.findUser(tmpUser.getId());
        boolean isExistEvent = checkUserAlreadyInEvent(user);

        if(isExistEvent){
           return UserEventStatusDto.builder()
                    .existEvent(isExistEvent)
                    .eventId(user.getEvent().getId().toString())
                    .build();
        }
        return UserEventStatusDto.builder()
                .existEvent(isExistEvent)
                .eventId(null)
                .build();
    }

    //test
    public User test(Long userId){
        return userService.findUser(userId);
    }


    // showUserInfo
    public UserPassportDto showUserInfo(User tmpUser){
        User user = userService.findUser(tmpUser.getId());

        List<UserBarcode> userBarcodeList = userBarcodeService.findUserBarcodeList(user);

        String recentBarcodeImg = userBarcodeService.findUserBarcodeList(user)
                .stream()
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

    // updateUserInfo
    @Value("${cloud.aws.s3.default-img}")
    private String USER_DEFAULT_PROFILE_IMAGE;
    public void updateUserInfo(User tmpUser, UserNewInfoDto userNewInfoDto){
        User user = userService.findUser(tmpUser.getId());

        if(userNewInfoDto.getNickname()==null||userNewInfoDto.getNickname().equals("")){
            throw new CustomException(ErrorCode.NICKNAME_EMPTY);
        }

        String fileName = s3Service.makefileName();
        String newProfileImgUrl = s3Service.putFileToS3(userNewInfoDto.getProfileImage(), fileName, s3Config.getProfileImgDir());

        if (!user.getProfileUrl().equals(USER_DEFAULT_PROFILE_IMAGE)) {
            s3Service.deleteFromS3(user.getProfileUrl());
        }
        userService.updateUserInfo(user, newProfileImgUrl,userNewInfoDto.getNickname(), userNewInfoDto.getBirth(),
                userNewInfoDto.getGender(), false);
    }

    /**
     * BarcodeController
     */

    //showBarcodeInfo(moodCloud)
    public List<BarcodeInfoDto> showBarcodeInfo(User tmpUser){
        User user = userService.findUser(tmpUser.getId());

        List<UserBarcode> userBarcodeList = userBarcodeService.findUserBarcodeList(user);

        List<BarcodeInfoDto> recentBarcodeInfo = userBarcodeList.stream()
                .map(UserBarcode::getBarcode)
                .sorted(Comparator.comparing(Barcode::getCreatedAt).reversed())
                .map(barcode -> new BarcodeInfoDto(barcode.getId().toString(), barcode.getBarcodeUrl(), barcode.getTitle()))
                .collect(Collectors.toList());

        return recentBarcodeInfo;
    }

    // showTicketInfo(my-ticket)
    public TicketDto showTicketInfo(User tmpUser, Long barcodeId){
        User user = userService.findUser(tmpUser.getId());
        Barcode barcode = barcodeService.findBarcode(barcodeId);
        User ownerUser = userBarcodeService.findUserBarcodeByBarcode(barcode).stream()
                .map(UserBarcode::getUser)
                .filter(currentUser -> currentUser.equals(user))
                .findFirst()
                .get();

        if(user!=ownerUser){throw new CustomException(ErrorCode.NOT_OWNER_ACCESS);}

        if (barcode.getType()==BarcodeType.DAY){
            return createDayTicket(user, barcode);
        } else {
            return createEventTicket(user, barcode);}
    }

    private TicketDto createDayTicket(User user, Barcode barcode){
        List<ImageInfoDto> imageInfoList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String createdAt = barcode.getCreatedAt().format(formatter);

        LocalDate startLocalDate = LocalDate.parse(barcode.getStartDate());
        LocalDate endLocalDate = LocalDate.parse(barcode.getEndDate());

        while (!startLocalDate.isAfter(endLocalDate)) {
            startLocalDate = startLocalDate.plusDays(1);

            int year = startLocalDate.getYear();
            int month = startLocalDate.getMonthValue();
            int day = startLocalDate.getDayOfMonth();

            Day currentDay = dayService.findDayId(user, year, month, day);

            List<String> dayPhotoUrlList = dayPhotoService.findDayPhotoUrlList(currentDay);

            ImageInfoDto imageInfoDto = ImageInfoDto
                    .builder()
                    .date(startLocalDate.toString())
                    .imageList(dayPhotoUrlList)
                    .build();
            imageInfoList.add(imageInfoDto);
        }
            return TicketDto
                    .builder()
                    .nickname(user.getNickname())
                    .title(barcode.getTitle())
                    .barcodeUrl(barcode.getBarcodeUrl())
                    .startDate(barcode.getStartDate())
                    .endDate(barcode.getEndDate())
                    .createdAt(createdAt)
                    .memberCnt(0)
                    .imageInfoList(imageInfoList)
                    .build();
    }

    private TicketDto createEventTicket(User user, Barcode barcode){
        List<ImageInfoDto> imageInfoList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String createdAt = barcode.getCreatedAt().format(formatter);

        Event event = eventService.findEventByBarcode(barcode);
        int memberCnt = event.getUsers().size()-1;

        List<String> eventPhotoUrlList = eventPhotoService.findAllEventPhotoList(event);
        ImageInfoDto imageInfoDto = ImageInfoDto
                .builder()
                .date(null)
                .imageList(eventPhotoUrlList)
                .build();
        imageInfoList.add(imageInfoDto);

        return TicketDto
                .builder()
                .nickname(user.getNickname())
                .title(barcode.getTitle())
                .barcodeUrl(barcode.getBarcodeUrl())
                .startDate(barcode.getStartDate())
                .endDate(barcode.getEndDate())
                .createdAt(createdAt)
                .memberCnt(memberCnt)
                .imageInfoList(imageInfoList)
                .build();
    }

    // showTicketInfo(quest-ticket)
    public TicketDto showTicketInfoGuest(Long barcodeId){
        Barcode barcode = barcodeService.findBarcode(barcodeId);
        User user = userBarcodeService.findUserBarcodeByBarcode(barcode)
                .stream()
                .findFirst()
                .get()
                .getUser();
        return showTicketInfo(user, barcodeId);
    }

    public EventList showEventList(User tmpUser){
        User user = userService.findUser(tmpUser.getId());
        List<Event> eventList = user.getEvent();
        List<EventListDto> eventListDtos = eventList.stream().map(event ->
                EventListDto.builder()
                        .id(event.getId().toString())
                        .title(event.getTitle())
                        .imageCount(String.valueOf(event.getEventPhoto().size()))
                        .build()
        ).toList();

        return EventList.builder()
                .eventListDtoList(eventListDtos)
                .build();

    }

    public void updateEventPhoto(Long eventId, List<File> newPhotoList){
        Event event = eventService.findEvent(eventId);

        List<String> newPhotoUrlList = new ArrayList<>();
        if (newPhotoList!=null){
            newPhotoUrlList = newPhotoList.parallelStream()
                    .map(newPhoto -> {
                        String fileName = s3Service.makefileName();
                        return s3Service.putFileToS3(newPhoto, fileName, s3Config.getDayImageDir());
                    }).collect(Collectors.toList());
        }

        List<EventPhoto> eventPhotos = eventPhotoService.makeEventPhoto(event, newPhotoUrlList);
        eventService.addEventPhoto(event, eventPhotos);
    }
}
