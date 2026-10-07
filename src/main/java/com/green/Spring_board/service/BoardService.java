package com.green.Spring_board.service;

import com.green.Spring_board.dto.BoardResponse;
import com.green.Spring_board.dto.BoardUpdateRequest;
import com.green.Spring_board.entity.Like;
import com.green.Spring_board.entity.User;
import com.green.Spring_board.exceptions.AuthorizationFailureException;
import com.green.Spring_board.exceptions.ResourceNotFoundException;
import com.green.Spring_board.exceptions.UnauthenticatedException;
import com.green.Spring_board.dto.BoardCreateRequest;
import com.green.Spring_board.entity.Board;
import com.green.Spring_board.repository.BoardRepository;
import com.green.Spring_board.repository.LikeRepository;
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
    private LikeRepository likeRepository;

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


    // 내가 작성한 게시글 보기 (조회)
    public List<BoardResponse> getMyBoards(int userId) {
        List<Board> boards = boardRepository.findByUserId(userId);
        // 작성한 게시글이 없는 경우
        if (boards.isEmpty()) {
            throw new ResourceNotFoundException("작성한 게시글이 없습니다.");
        }
        // 작성한 게시글이 있을 때
        // 게시글을 하나씩 꺼내서 전체를 만들어줘야함
        List<BoardResponse> responses = new ArrayList<>();

        for (Board board : boards) {
            responses.add(new BoardResponse(
                    board.getId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getHits(),
                    board.getUser().getId(),
                    board.getUser().getNickname(),
                    board.getCreatedDatetime(),
                    board.getUpdatedDatetime()
            ));
        }
        return responses;
    }

    // * 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        Optional<User> user = userRepository.findById(userId);

        // userId 유효성 체크 (해당 userId의 유저가 정상적으로 존재하는지)
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
    public void updateBoard(int id, BoardUpdateRequest boardUpdateRequest, int userId) {

        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
           // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoard.get();

        // 요청자의 user id
        // 작성자와 요청자 동일 여부 확인
        if (board.getUser().getId() != userId) {
            // 예외
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }


        if (boardUpdateRequest.getTitle() != null && !boardUpdateRequest.getTitle().isBlank()) {
            board.setTitle(boardUpdateRequest.getTitle());
        }
        if (boardUpdateRequest.getContent() != null && !boardUpdateRequest.getContent().isBlank()) {
            board.setContent(boardUpdateRequest.getContent());
        }

        boardRepository.save(board);
    }


    // * 삭제
    public void deleteBoard(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoard.get();

        if (board.getUser().getId() != userId) {
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }

        boardRepository.deleteById(id);
    }

    // 매개변수에 1.보드 아이디 / 2. 유저 아이디
    public void pressLike(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("존재하지 않는 게시글입니다.");
        }
        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("존재하지 않는 유저입니다.");
        }
        User user = optionalUser.get();

        // 좋아요 취소 기능
        // 1. 이 유저와 보드로 동일한 좋아요가 있는지 확인
        Optional<Like> likeOptional = likeRepository.findByUserIdAndBoardId(userId, id);
        if (likeOptional.isEmpty()) {
            // 2. 없으면 추가
            Like like = new Like();
            like.setUser(user);
            like.setBoard(board);
            likeRepository.save(like);
        } else {
            // 3. 있으면 삭제
            Like like = likeOptional.get();
            likeRepository.deleteById(like.getId());
        }


    }
}
