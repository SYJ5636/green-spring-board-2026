package com.green.Spring_board.repository;

import com.green.Spring_board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like>findByUserIdAndBoardId(int userId, int boardId);
    List<Like> findByBoardId(int boardId);
    boolean existsByUserIdAndBoardId(int userId, int boardId);
}
