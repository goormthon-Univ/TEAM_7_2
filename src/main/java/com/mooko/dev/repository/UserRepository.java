package com.mooko.dev.repository;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);

    Optional<User> findByRefreshToken(String refreshToken);

    List<User> findByEvent(Event event);
}
