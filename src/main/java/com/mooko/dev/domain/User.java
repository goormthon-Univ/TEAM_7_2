package com.mooko.dev.domain;

import jakarta.persistence.*;
import java.io.File;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", initialValue = 1, allocationSize = 50)
    @Column(name = "user_id")
    private Long id;

    private String nickname;
    private String profileUrl;
    private String refreshToken;
    private String socialId;
    private LocalDateTime createdAt;
    private Boolean checkStatus;
    private String birth;
    private String gender;
    private String dateOfIssue;     //유저가 회원가입했을때의 시점으로
    private Boolean modalActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;


    public void updateEvent(Event event) {
        this.event = event;
    }

    public void updateCheckStatus(Boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateUserInfo(String profileUrl, String nickname,
            String birth, String gender, Boolean modalActive){
        this.profileUrl = profileUrl;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.modalActive = modalActive;
    }
}
