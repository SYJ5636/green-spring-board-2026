package com.green.Spring_board.controller;


import com.green.Spring_board.dto.*;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;
    // 댓글 작성
    @PostMapping("/board/{id}/comment")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable int id,
            @RequestBody CommentCreateRequest commentCreateRequest,
            HttpServletRequest httpServletRequest
    ){
        HttpSession session = httpServletRequest.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.createComment(commentCreateRequest, userId, id);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/board/{id}/comment")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readComment(
            @PathVariable int id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.readComments(id)));
    }


    // 수정
    @PatchMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            HttpServletRequest httpServletRequest,
            @PathVariable int id,
            @RequestBody CommentUpdateRequest commentUpdateRequest
            ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");
        commentService.updateComment(id, commentUpdateRequest, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    //삭제
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            HttpServletRequest httpServletRequest,
            @PathVariable int id
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
