package com.mooko.dev.service;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.DayPhoto;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.repository.DayPhotoRepository;
import com.mooko.dev.repository.DayRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DayPhotoService {
    private final DayPhotoRepository dayPhotoRepository;
    public DayPhoto findThumnail(Day day){
        DayPhoto dayPhoto = dayPhotoRepository.findByDayAndThumbnailTrue(day);
        return dayPhoto;
    }

    public List<DayPhoto> findDayPhotoList(Day day){

        List<DayPhoto> DayPhotos = dayPhotoRepository.findByDay(day);
        return DayPhotos;
    }

    public void deleteThumbnail(DayPhoto dayThumbnail){
        dayPhotoRepository.delete(dayThumbnail);
    }

    public void makeNewThumbnail(Day day, String newThumbnailUrl, Boolean isThumbnail){
        DayPhoto dayPhoto = DayPhoto.builder()
                .url(newThumbnailUrl)
                .thumbnail(isThumbnail)
                .day(day)
                .createdAt(LocalDateTime.now())
                .build();
        dayPhotoRepository.save(dayPhoto);
    }

    public void deleteDayPhotos(List<DayPhoto> dayPhotoList){
        dayPhotoRepository.deleteAll(dayPhotoList);
    }

    public void makeNewDayPhoto(Day day, List<String> dayPhotoUrlList, Boolean isThumbnail){
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
