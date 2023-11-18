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
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.res.EventIdDto;
import com.mooko.dev.dto.event.res.EventList;
import com.mooko.dev.dto.event.res.EventListDto;
import com.mooko.dev.dto.event.res.EventPhotoResDto;
import com.mooko.dev.dto.user.req.UserNewInfoDto;
import com.mooko.dev.dto.user.res.UserPassportDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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


    //makeNewEventBarcode
    public void makeNewEventBarcode(User tmpUser, Long eventId, EventIdDto eventIdDto) throws IOException, InterruptedException {
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

    //showEventBlock
    public EventPhotoResDto showEventBlock(Long eventId) {
        Event event = eventService.findEvent(eventId);
        List<EventPhoto> eventPhotoByEvent = eventPhotoService.findEventPhotoByEvent(event);
        if (!eventPhotoByEvent.isEmpty()) {
            List<String> imageUrlList = eventPhotoByEvent.stream().map(EventPhoto::getUrl).toList();
            return EventPhotoResDto
                    .builder()
                    .eventId(eventId.toString())
                    .imageUrlList(imageUrlList)
                    .build();
        }
        return EventPhotoResDto.builder()
                .eventId(eventId.toString())
                .imageUrlList(null)
                .build();

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

        if (currentDay==null){
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
        if(currentDay==null){throw new CustomException(ErrorCode.DAY_NOT_FOUND);}

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
                    .imageInfoList(imageInfoList)
                    .build();
    }

    private TicketDto createEventTicket(User user, Barcode barcode){
        List<ImageInfoDto> imageInfoList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String createdAt = barcode.getCreatedAt().format(formatter);

        Event event = eventService.findEventByBarcode(barcode);

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
                .imageInfoList(imageInfoList)
                .build();
    }

    // showTicketInfo(quest-ticket)
    public TicketDto showTicketInfo(Long barcodeId){
        Barcode barcode = barcodeService.findBarcode(barcodeId);
        if(barcode==null){
            throw new CustomException(ErrorCode.BARCODE_NOT_FOUND);
        }
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

        List<EventListDto> eventListDtos = eventList.stream()
                .filter(event -> event.getBarcode() != null)
                .map(event -> EventListDto.builder()
                        .id(event.getId().toString())
                        .title(event.getTitle())
                        .imageCount(String.valueOf(event.getEventPhoto().size()))
                        .build())
                .collect(Collectors.toList()); // 리스트로 수집

        return EventList.builder()
                .eventListDtoList(eventListDtos)
                .build();
    }


    public void updateEventPhoto(Long eventId, List<File> newPhotoList){
        if (newPhotoList == null || newPhotoList.isEmpty()) {
            return; // 빈 목록일 경우 조기 반환
        }

        Event event = eventService.findEvent(eventId);
        List<EventPhoto> eventPhotoList = event.getEventPhoto();
        if (!eventPhotoList.isEmpty()) {
            eventPhotoList.forEach(eventPhoto -> s3Service.deleteFromS3(eventPhoto.getUrl()));
        }
        List<String> newPhotoUrlList = newPhotoList.stream()
                .map(this::uploadPhoto)
                .collect(Collectors.toList());

        List<EventPhoto> eventPhotos = eventPhotoService.makeEventPhoto(event, newPhotoUrlList);
        eventService.addEventPhoto(event, eventPhotos);
    }

    private String uploadPhoto(File photo) {
        String fileName = s3Service.makefileName();
        return s3Service.putFileToS3(photo, fileName, s3Config.getEventImageDir());
    }
}
