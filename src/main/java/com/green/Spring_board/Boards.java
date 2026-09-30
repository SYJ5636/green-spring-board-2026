package com.green.Spring_board;

import jakarta.persistence.*;

@Entity // * JPA 한테 DB의 구조를 알려주는 역할
@Table(name = "boards") // * 진짜 DB 테이블의 이름
public class Boards {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false) // * null이 가능한 컬림인지 지정 해주는 기능
    private String title;

    @Column(nullable = false) // * null이 가능한 컬림인지 지정 해주는 기능
    private String content;

    public Boards() {}

    public Boards(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
