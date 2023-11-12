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
@Table(name = "barcode")
public class Barcode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "barcode_id_seq")
    @SequenceGenerator(name = "barcode_id_seq", sequenceName = "barcode_id_seq")
    @Column(name = "barcode_id")
    private Long id;

    private String barcodeUrl;

    private String title;

    private String startDate;
    private String endDate;

    @Enumerated(EnumType.STRING)
    private BarcodeType type;

    @OneToOne(mappedBy = "barcode", fetch = FetchType.LAZY)
    private Event event;

    private LocalDateTime createdAt;

}
