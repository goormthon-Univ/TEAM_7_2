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
@Table(name = "day_photo")
public class DayPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_photo_id_seq")
    @SequenceGenerator(name = "day_photo_id_seq", sequenceName = "day_photo_id_seq")
    @Column(name = "day_photo_id")
    private Long id;

    private String url;

    private boolean thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id")
    private Day day;

    private LocalDateTime createdAt;

}
