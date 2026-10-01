package com.green.Spring_board.service;

import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UserRequestException;
import com.green.Spring_board.dto.BoardCreateRequest;
import com.green.Spring_board.entity.Board;
import com.green.Spring_board.repository.BoardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BoardService {
    private BoardRepository boardRepository;

    // * 전체 조회
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    // * 상세 조회
    public Board getBoards(int id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }

        Board board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);
        return board;
    }

    // * 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            // * 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            // * 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }

        Board board = new Board();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        Board saveBoard = boardRepository.save(board); // * save도 값을 돌려 받을 수 있다 (저장하고나서 저장된 데이터를 돌려줌)

        return saveBoard.getId();
    }

    // * 수정
    public void updateBoard(int id, BoardCreateRequest boardCreateRequest) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
           // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoard.get();

        if (boardCreateRequest.getTitle() != null && !boardCreateRequest.getTitle().isBlank()) {
            board.setTitle(boardCreateRequest.getTitle());
        }
        if (boardCreateRequest.getContent() != null && !boardCreateRequest.getContent().isBlank()) {
            board.setContent(boardCreateRequest.getContent());
        }

        boardRepository.save(board);
    }


    // * 삭제
    public void deleteBoard(int id) {
        boolean isExist = boardRepository.existsById(id);
        if (!isExist) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        boardRepository.deleteById(id);
    }
}
