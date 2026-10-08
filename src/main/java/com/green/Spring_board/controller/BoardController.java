package com.green.Spring_board.controller;

import com.green.Spring_board.dto.*;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// 전역 핸들러

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // * 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoards(
            HttpServletRequest httpServletRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String order
    ) {
        // 기존 세션이 없으면 null 반환
        HttpSession session = httpServletRequest.getSession(false);

        // 노 로그인
        int userId = -1;

        // 로그인
        if (session != null && session.getAttribute("userId") != null) {
            userId = (int) session.getAttribute("userId");
        }
        return ResponseEntity.ok(
                ApiResponse.ok(boardService.getAllBoards(userId, page, size, order))
        );
    }

    // # 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardDetail(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        // # 기존 세션이 없으면 null 반환
        HttpSession session = httpServletRequest.getSession(false);

        // # 비로그인 상태
        int userId = -1;

        // # 로그인 상태라면 세션에서 userId 가져오기
        if (session != null && session.getAttribute("userId") != null) {
            userId = (int) session.getAttribute("userId");
        }

        // # 로그인 여부와 관계없이 게시글 상세 조회
        BoardResponse board = boardService.getBoards(id, userId);

        return ResponseEntity.ok(ApiResponse.ok(board));
    }


    // 내가 작성한 게시글 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getMyBoard(
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");
        List<BoardResponse> response = boardService.getMyBoards(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // * 삽입 (생성)
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
        @Valid @RequestBody BoardCreateRequest boardCreateRequest,
        HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        int newBoardId = boardService.createBoard(boardCreateRequest, userId);
        URI location = URI.create("/api/board/" + newBoardId);
        return ResponseEntity.created(location).body(ApiResponse.ok());

    }

    // * 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable int id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest,
            HttpServletRequest httpServletRequest
    ) {
            HttpSession session = httpServletRequest.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                throw new UnauthenticatedException("로그인이 필요합니다.");
            }
            int userId = (int) session.getAttribute("userId");
            boardService.updateBoard(id, boardUpdateRequest, userId);
            return ResponseEntity.ok().body(ApiResponse.ok());
    }

    // * 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

            // 둘 중 어느 방법을 쓸 지는 속한 팀, 조직 컨벤션 따르기

            // 삭제 성공 시 응답 방법 1.
            // 200 + ApiResponse<Void>

            // 삭제 성공 시 응답 방법 2.
            // 204 + (No Content) + No Body
            int userId = (int) session.getAttribute("userId");
            boardService.deleteBoard(id, userId);
            return ResponseEntity.ok(ApiResponse.ok());
    }

    // 좋아요를 누른다는건 Post
    @PostMapping("/like/{id}")
    public ResponseEntity<ApiResponse<Void>> likeBoard(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        boardService.pressLike(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 상세 눌렀을 때 어느 유저들이 이 게시글 좋아요를 눌렀는지
    @GetMapping("/like/{id}")
    public ResponseEntity<ApiResponse<LikeDetailResponse>> viewLikeDetails(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        // 이 게시글에 좋아요 누른 유저들의 유저명
        LikeDetailResponse response = boardService.getLikeDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    // 내가 이 게시글 좋아요 눌렀는지
    // 전체 조회든 상세조회든 게시글에 좋아요를 눌렀는지 안눌렀는지 포함되어 나와야함
    // 로그인한 계정으로 전체, 상세 조회 때리면 내가 좋아요 눌렀는지 안눌렀는지 나와야함
    // 좋아요를 누른적 있으면 true 없으면 false로 내려주면 됨



}
