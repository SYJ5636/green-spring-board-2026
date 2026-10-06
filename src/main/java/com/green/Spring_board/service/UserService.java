package com.green.Spring_board.service;

import com.green.Spring_board.dto.LoginRequest;
import com.green.Spring_board.dto.MyInfoResponse;
import com.green.Spring_board.dto.SignupRequest;
import com.green.Spring_board.dto.UserUpdateRequest;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.ResourceConflictException;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.exceptions.UserRequestException;
import com.green.Spring_board.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void signup(SignupRequest signupRequest) {
        // 이메일과 비밀번호가 공백이 아닌지 확인
        if (signupRequest.getEmail().isBlank() || signupRequest.getPassword().isBlank()) {
            throw new UserRequestException("Email or password cannot be blank");
        }

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // db save
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        userRepository.save(user);
    }

    public int login(LoginRequest loginRequest) {
        // 1. 이메일 존재하는건지 확인
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get(); // 이 이메일의 사용자 정보
        // 2. 비밀번호가 올바른지 확인
        // matches 라는 함수가 맞는지 아닌지 검사를 해줌 개꿀
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Wrong password");
        }

        // 3. 로그인 성공
        return user.getId();
    }

    public MyInfoResponse getUserInfo(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOptional.get();

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
            throw new ResourceNotFoundException("Not found User");
        }
        User user = optionalUser.get();

        // 닉네임만 들어온 경우에만 덮어 씌우기
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            throw new UserRequestException("잘못된 입력");
        } user.setNickname(request.getNickname());
        // 이메일이 들어온 경우에만 덮어 씌우기
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            throw new UserRequestException("잘못된 입력");
        } user.setEmail(request.getEmail());

        userRepository.save(user);
    }

    public void deleteUser(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = userOptional.get();
        userRepository.delete(user);
    }
}










