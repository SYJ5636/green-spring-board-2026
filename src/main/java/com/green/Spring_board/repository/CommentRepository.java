package com.green.Spring_board.repository;

import com.green.Spring_board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBoardUserIdAndIsDeletedFalse(int boardId);
}