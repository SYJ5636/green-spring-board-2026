package com.green.Spring_board.service;

import com.green.Spring_board.dto.CommentCreateRequest;
import com.green.Spring_board.entity.Board;
import com.green.Spring_board.entity.Comment;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.repository.BoardRepository;
import com.green.Spring_board.repository.CommentRepository;
import com.green.Spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void createComment(
            CommentCreateRequest commentCreateRequest,
            int userId,
            int boardId
    ) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("Board not found");
        }

        User user = userOptional.get();
        Board board = boardOptional.get();

        Comment comment = new Comment();
        comment.setComment(commentCreateRequest.getContent());
        comment.setUser(user);
        comment.setBoard(board);
        commentRepository.save(comment);
    }
}
