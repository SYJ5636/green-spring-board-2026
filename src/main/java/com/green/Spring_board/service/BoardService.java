package com.green.Spring_board.service;

import com.green.Spring_board.dto.BoardResponse;
import com.green.Spring_board.dto.BoardUpdateRequest;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.exceptions.UserRequestException;
import com.green.Spring_board.dto.BoardCreateRequest;
import com.green.Spring_board.entity.Board;
import com.green.Spring_board.repository.BoardRepository;
import com.green.Spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BoardService {
    private BoardRepository boardRepository;
    private UserRepository userRepository;

    // * 전체 조회
    public List<BoardResponse> getAllBoards() {

        List<Board> boards = boardRepository.findAll();
        List<BoardResponse> boardResponses = new ArrayList<>();
        // List<Board> -> List<BoardResponse> 형태로 변환


        for (Board board : boards) {
            boardResponses.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }
        return boardResponses;
        // 1. List<BoardResponse> 형채의 빈 리스트 생성
        // 2. Board 개수만큼 반복하며 new BoardResponse 생성
        // 3. 1번에서 만든 리스트에 추가
    }

    // * 상세 조회
    public BoardResponse getBoards(int id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }

        Board board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getHits(),
                board.getUser().getId(),
                board.getUser().getNickname(),
                board.getCreatedDatetime(),
                board.getUpdatedDatetime()
        );
    }

    // * 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            // * 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            // * 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }


        Optional<User> user = userRepository.findById(userId);

        // userId 유효성 체크 (해당 userId의 유저가 정상적으로 존재하는지)
        // TODO :: 이후 삭제/탈퇴 유저에 대한 검증도 추가 필요
        if (user.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
        }

        Board board = new Board();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());
        board.setUser(user.get());

        Board saveBoard = boardRepository.save(board); // * save도 값을 돌려 받을 수 있다 (저장하고나서 저장된 데이터를 돌려줌)

        return saveBoard.getId();
    }

    // * 수정
    public void updateBoard(int id, BoardUpdateRequest boardUpdateRequest) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
           // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoard.get();

        if (boardUpdateRequest.getTitle() != null && !boardUpdateRequest.getTitle().isBlank()) {
            board.setTitle(boardUpdateRequest.getTitle());
        }
        if (boardUpdateRequest.getContent() != null && !boardUpdateRequest.getContent().isBlank()) {
            board.setContent(boardUpdateRequest.getContent());
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
