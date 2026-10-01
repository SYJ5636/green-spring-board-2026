package com.green.Spring_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // * JPA 한테 DB의 구조를 알려주는 역할
@Table(name = "boards") // * 진짜 DB 테이블의 이름
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false) // * null이 가능한 컬림인지 지정 해주는 기능
    private String title;

    @Column(nullable = false) // * null이 가능한 컬림인지 지정 해주는 기능
    private String content;

    @Column(nullable = false)
    private int hits;
}
