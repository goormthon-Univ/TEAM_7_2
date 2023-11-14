package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "days")
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_id_seq")
    @SequenceGenerator(name = "day_id_seq", sequenceName = "day_id_seq")
    @Column(name = "day_id")
    private Long id;

    private int year;
    private int month;
    private int day;

    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    public void updateMemo(String memo) {
        this.memo = memo;
    }

}


