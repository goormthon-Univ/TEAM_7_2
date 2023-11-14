package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_seq")
    @SequenceGenerator(name = "event_id_seq", sequenceName = "event_id_seq")
    @Column(name = "event_id")
    private Long id;

    private String title;

    private Boolean activeStatus;

    private String startDate;
    private String endDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_maker_id")
    private User roomMaker;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barcode_id")
    private Barcode barcode;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<User> users = new ArrayList<>();

    private LocalDateTime createdAt;

    public void updateEventName(String eventName) {
        this.title = eventName;
    }

    public void updateEventDate(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateEventStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public void updateBarcode(Barcode barcode) {
        this.barcode = barcode;
    }

}
