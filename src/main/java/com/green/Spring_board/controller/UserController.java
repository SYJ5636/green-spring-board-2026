package com.green.Spring_board.controller;

import com.green.Spring_board.dto.LoginRequest;
import com.green.Spring_board.dto.MyInfoResponse;
import com.green.Spring_board.dto.SignupRequest;
import com.green.Spring_board.dto.UserUpdateRequest;
import com.green.Spring_board.exceptions.ResourceConflictException;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.exceptions.UserRequestException;
import com.green.Spring_board.repository.UserRepository;
import com.green.Spring_board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            userService.signup(signupRequest);
            return ResponseEntity.ok().build();
        } catch (ResourceConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest,
                                      HttpServletRequest httpServletRequest) {
        // DTO Valid
        try {
            int userId = userService.login(loginRequest);
            // 세션 작업 시작 (장부 관리 시작)
            // 이 요청이 세션 정보를 가지고 있어? 라고 하는 부분
            HttpSession session = httpServletRequest.getSession();
            // 잘못 된 키를 가지고 있을 수 있으니 자동으로 변경 해주는 부분
            httpServletRequest.changeSessionId();
            // 유저 아이디를 장부에 넣어야 함
            session.setAttribute("userId", userId);
            return ResponseEntity.ok().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthenticatedException e) {
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getCurrentUser(HttpServletRequest httpServletRequest) {
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
            return  ResponseEntity.status(401).build();
        }
        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        MyInfoResponse response = userService.getUserInfo(userId);

        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null ) {
            return ResponseEntity.status(401).build();
        }

        session.invalidate();
        return ResponseEntity.ok().build();
        // 사용자가 다시 로그인 할 때까지
        // 사용자의 로그인 세션은 만료 함
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        // 이메일 닉네임 업데이트
        // 현재 유저를 가져와서, 해당 유저 정보를 사용자가 올린 요청으로 덮어 씌운다
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            int userId = (int) session.getAttribute("userId");
            userService.updateUserInfo(userId, userUpdateRequest);

            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // 유저 탈퇴 기능
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        int userId = (int) session.getAttribute("userId");

        // 1. DB 에서 삭제
        // 서비스에게 맞김
        userService.deleteUser(userId);
        // 2. 자동 로그아웃되게 한다. (세션 비활성화)
        session.invalidate();

        return ResponseEntity.noContent().build();
    }
}
