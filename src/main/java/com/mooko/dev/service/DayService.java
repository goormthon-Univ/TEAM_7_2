package com.mooko.dev.service;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.repository.DayRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayService {
    public static final String MEMO_NULL = "";
    private final DayRepository dayRepository;

    public Day findDayId(User user, int year, int month, int day){
        Optional<Day> currentDay = dayRepository.findByUserAndYearAndMonthAndDay(user,year,month,day);
        if (currentDay.isPresent()){
            return currentDay.get();
        } else {
            return null;
        }
    }

    public Optional<Day> findDayIdOptinal(User user, int year, int month, int day){
        Optional<Day> currentDay = dayRepository.findByUserAndYearAndMonthAndDay(user,year,month,day);
        return currentDay;
    }

    public String findMemo(Day day){
        Optional<Day> currentDay = dayRepository.findById(day.getId());
        if (currentDay!=null){
            return currentDay.get().getMemo();
        } else {
            return MEMO_NULL;
        }

    }
    @Transactional
    public Day makeDay(User user,int year,int month,int day){
        Day currentDay = Day.builder()
                .year(year)
                .month(month)
                .day(day)
                .user(user)
                .build();
        return dayRepository.save(currentDay);
    }

    @Transactional
    public void updateMemo(Day day, String memo) {
        day.updateMemo(memo);
        dayRepository.save(day);
    }

    public List<Day> findDayIdList(User user, int year, int month){
        List<Day> dayList = dayRepository.findByUserAndYearAndMonth(user,year,month);
        return dayList;
    }
}
