package com.green.Spring_board.service;

import com.green.Spring_board.dto.CommentCreateRequest;
import com.green.Spring_board.dto.CommentResponse;
import com.green.Spring_board.dto.CommentUpdateRequest;
import com.green.Spring_board.entity.Board;
import com.green.Spring_board.entity.Comment;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.AuthorizationFailureException;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.repository.BoardRepository;
import com.green.Spring_board.repository.CommentRepository;
import com.green.Spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public List<CommentResponse> readComments(int boardId) {
        boolean isBoardExists = boardRepository.existsById(boardId);
        if (!isBoardExists) {
            throw new ResourceNotFoundException("Board Not Found");
        }


        List<Comment> comments = commentRepository.findByBoardId(boardId);
        // 댓글은 가져왔는데, 이걸 이제 CommentResponse 로 변환

        List<CommentResponse> commentResponses =new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse commentResponse = new CommentResponse();

            commentResponse.setCommentId(comment.getId());
            commentResponse.setContent(comment.getComment());
            commentResponse.setNickname(comment.getUser().getNickname());
            commentResponse.setCommentDate(comment.getCreatedDatetime());

            commentResponses.add(commentResponse);
        }
        return commentResponses;
    }


    // 수정
    public void updateComment(
            int commentId,
            CommentUpdateRequest commentUpdateRequest,
            int userId
    ) {

        // 1. 수정할 댓글 조회
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResourceNotFoundException("댓글을 찾을 수 없습니다.");
        }

        Comment comment = optionalComment.get();

        // 요청자의 user id
        // 댓글 작성자와 요청자 동일 여부 확인
        if (comment.getUser().getId() != userId){
            throw new AuthorizationFailureException("댓글 수정 권한이 없습니다.");
        }

        // 수정할 댓글 내용이 null이 아니고 공백이 아닌 경우
        if (commentUpdateRequest.getContent() != null){
            comment.setComment(commentUpdateRequest.getContent());
            commentRepository.save(comment);
        }

    }


    // 삭제
    public void deleteComment(
            int commentId,
            int userId
    ) {
        // 1. 수정할 댓글 조회
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResourceNotFoundException("댓글을 찾을 수 없습니다.");
        }
        Comment comment = optionalComment.get();
        // 요청자의 user id
        // 댓글 작성자와 요청자 동일 여부 확인
        if (comment.getUser().getId() != userId){
            throw new AuthorizationFailureException("댓글 수정 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }


}
