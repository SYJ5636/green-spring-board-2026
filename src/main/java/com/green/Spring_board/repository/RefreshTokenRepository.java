package com.green.Spring_board.repository;


import com.green.Spring_board.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    void deleteByUserId(int userId);
    Optional<RefreshToken> findByToken(String token);
}
