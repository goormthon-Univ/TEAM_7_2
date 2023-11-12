package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users_barcode")
public class UsersBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_barcode_id_seq")
    @SequenceGenerator(name = "user_barcode_id_seq", sequenceName = "user_barcode_id_seq")
    @Column(name = "user_barcode_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barcode_id")
    private Barcode barcode;

}
