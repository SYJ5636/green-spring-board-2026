package com.green.Spring_board;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // * 해당 클래스를 API 전용으로 설정
@RequestMapping("/api/test") // * 해당 클래스 아래 작성되는 모든 API의 URL은, 무조건 /api/test(만든 API)로 지정

public class TestController {

    @GetMapping
    public String test() {
        return "hello world!";
    }

}
