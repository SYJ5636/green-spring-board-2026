package com.green.Spring_board.controller;

import com.green.Spring_board.dto.*;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.repository.UserRepository;
import com.green.Spring_board.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그린 커뮤니티 회원 API", description = "회원 관련 API 모음입니다.")
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "회원가입 API", description = "회원가입을 할 때 씀")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest) {
        int userId = userService.login(loginRequest);
        // 세션 작업 시작 (장부 관리 시작)
        // 이 요청이 세션 정보를 가지고 있어? 라고 하는 부분
        HttpSession session = httpServletRequest.getSession();
        // 잘못 된 키를 가지고 있을 수 있으니 자동으로 변경 해주는 부분
        httpServletRequest.changeSessionId();
        // 유저 아이디를 장부에 넣어야 함
        session.setAttribute("userId", userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getCurrentUser(HttpServletRequest httpServletRequest) {
        // 내 정보 조회하기
        // 이메일과 닉네임만 내려주기
        // 1. 이 사람의 세션을 가져옴
        /*
        /me 는 회원 전용 서비스다.
        이 사람의 세션이 없으면, 새로 만들어주는게 아니라 내쫒아야 함
        그래서 세션이 없으면 없다고 세션은 만들지 않도록  getSession 안에 (false) 옵션을 추가함.
         */
        HttpSession session = httpServletRequest.getSession(false);

        // 모든 아이디에 세션을 추가하고 있기 때문에 확인을 다 한다.
        // 유저 id 가 없으면 정상적인 처리를 못함(위 로그인에서 만들어 줬기 때문)
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        MyInfoResponse response = userService.getUserInfo(userId);

        return ResponseEntity.ok().body(ApiResponse.ok(response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null ) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok());
        // 사용자가 다시 로그인 할 때까지
        // 사용자의 로그인 세션은 만료 함
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        // 이메일 닉네임 업데이트
        // 현재 유저를 가져와서, 해당 유저 정보를 사용자가 올린 요청으로 덮어 씌운다
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
            int userId = (int) session.getAttribute("userId");
            userService.updateUserInfo(userId, userUpdateRequest);
            return ResponseEntity.ok(ApiResponse.ok());
    }


    // 유저 탈퇴 기능
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        // 1. DB 에서 삭제
        // 서비스에게 맞김
        userService.deleteUser(userId);
        // 2. 자동 로그아웃되게 한다. (세션 비활성화)
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
