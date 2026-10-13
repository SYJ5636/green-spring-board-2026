package com.green.Spring_board.service;

import com.green.Spring_board.dto.*;
import com.green.Spring_board.entity.RefreshToken;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.ResourceConflictException;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.global.UserState;
import com.green.Spring_board.repository.RefreshTokenRepository;
import com.green.Spring_board.repository.UserRepository;
import com.green.Spring_board.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public void signup(SignupRequest signupRequest) {
        // 아이디가 사용 중인지 확인
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ResourceConflictException("유저네임이 이미 사용중입니다.");
        }

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceConflictException("이메일이 이미 사용중입니다.");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // db save
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        user.setState(UserState.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()
            ));
        } catch (AuthenticationException e) {
            throw new UnauthenticatedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 1. 이메일 존재하는건지 확인
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
        }

        if (user.getState() == UserState.QUITTED){
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        // 액세스 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user.getUsername());
        // 리프레시 토큰 발급
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        // 1. 해당 유저의 기존 리프레시 토큰(들) 삭제
        refreshTokenRepository.deleteByUserId(user.getId());

        // 2. 해당 유저의 신규 리프레시 토큰 저장
        Date expiration = tokenProvider.getExpiration(refreshToken);
        LocalDateTime expirationDateTime = expiration.toInstant().atZone(
                ZoneId.systemDefault()
        ).toLocalDateTime();

        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(refreshToken);
        refresh.setExpirationDateTime(expirationDateTime);
        refreshTokenRepository.save(refresh);
        // 3. 로그인 성공
        return new LoginResponse(accessToken, refreshToken);
    }

    public MyInfoResponse getUserInfo(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
        }
        User user = userOptional.get();


        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }


        // 4. DB에서 이 유저의 닉네임과 이메일을 받아옴
        String email = user.getEmail();
        String nickname = user.getNickname();

        // 5. 돌려줌.
        MyInfoResponse myInfoResponse = new MyInfoResponse(email, nickname);
        myInfoResponse.setEmail(email);
        myInfoResponse.setNickname(nickname);

        return myInfoResponse;
    }


    // 업데이트(수정)
    public void updateUserInfo(int userId, UserUpdateRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        if(request.getEmail()!=null
                && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())
        ){
            user.setEmail(request.getEmail());
        }

        if(request.getNickname()!=null
                && !request.getNickname().isBlank()
        ) {
            user.setNickname(request.getNickname());
        }
        userRepository.save(user);
    }

    public void deleteUser(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
        }
        User user = userOptional.get();

        user.setState(UserState.QUITTED);
        userRepository.save(user);
    }
}










