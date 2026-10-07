package com.green.Spring_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BoardResponse {
    int id; // Board Id
    String title; // 제목
    String content; // 내용
    int hits; // 조회수
    int likeCount; // 좋아요 개수
    boolean isLikedByMe; // 좋아요 눌렀는지
    Integer authorId; // 작성자 ID
    String authorNickname; // 작성자 닉네임
    LocalDateTime createdDatetime; // 생성일지
    LocalDateTime updateDatetime; // 수정일
}
