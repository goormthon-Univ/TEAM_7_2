package com.mooko.dev.service;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.DayPhoto;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.repository.DayPhotoRepository;
import com.mooko.dev.repository.DayRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DayPhotoService {
    private final DayPhotoRepository dayPhotoRepository;
    public DayPhoto findThumnail(Day day){
        Optional<DayPhoto> dayPhoto = dayPhotoRepository.findByDayAndThumbnailTrue(day);
        if (dayPhoto.isPresent()){
            return dayPhoto.get();
        }else{
            return null;
        }
    }

    public List<DayPhoto> findDayPhotoList(Day day){

        List<DayPhoto> DayPhotos = dayPhotoRepository.findByDayAndThumbnailFalse(day);
        return DayPhotos;
    }

    @Transactional
    public void deleteThumbnail(DayPhoto dayThumbnail){
        dayPhotoRepository.delete(dayThumbnail);
    }

    @Transactional
    public void makeNewThumbnail(Day day,String newThumbnailUrl,boolean isThumbnail){
        DayPhoto dayPhoto = DayPhoto.builder()
                .url(newThumbnailUrl)
                .thumbnail(isThumbnail)
                .day(day)
                .createdAt(LocalDateTime.now())
                .build();
        dayPhotoRepository.save(dayPhoto);
    }

    @Transactional
    public void deleteDayPhotos(List<DayPhoto> dayPhotoList){
        dayPhotoRepository.deleteAll(dayPhotoList);
    }

    @Transactional
    public void makeNewDayPhoto(Day day, List<String> dayPhotoUrlList, boolean isThumbnail){
        List<DayPhoto> dayPhotos = new ArrayList<>();

        for (String url : dayPhotoUrlList) {
            DayPhoto dayPhoto = DayPhoto.builder()
                    .url(url)
                    .thumbnail(isThumbnail)
                    .day(day)
                    .createdAt(LocalDateTime.now())
                    .build();
            dayPhotos.add(dayPhoto);
        }

        dayPhotoRepository.saveAll(dayPhotos);
    }

    public List<String> findDayPhotoUrlList(Day day){
        List<DayPhoto> DayPhotos = dayPhotoRepository.findByDay(day);
        return DayPhotos.stream().map(DayPhoto::getUrl).toList();
    }
}
