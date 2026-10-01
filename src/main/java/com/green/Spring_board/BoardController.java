package com.green.Spring_board;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {
    private BoardRepository boardRepository;

    // * 전체 조회
    @GetMapping
    public ResponseEntity<List<Boards>> getBoards() {
        return ResponseEntity.ok(
                boardRepository.findAll()
        );
    }

    // * 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity getBoardDetail(@PathVariable int id) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            return ResponseEntity.notFound().build();
        }

        Boards board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        return ResponseEntity.ok(board);
    }

    // * 삽입 (생성)
    @PostMapping
    public ResponseEntity<Boards> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Boards board = new Boards();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        Boards saveBoard = boardRepository.save(board); // * save도 값을 돌려 받을 수 있다 (저장하고나서 저장된 데이터를 돌려줌)
        int newBoardId = saveBoard.getId();
        URI location = URI.create("/api/board/" + newBoardId);
        return ResponseEntity.created(location).body(board);
    }

    // * 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Boards board = optionalBoard.get();

        if (boardCreateRequest.getTitle() != null && !boardCreateRequest.getTitle().isBlank()) {
            board.setTitle(boardCreateRequest.getTitle());
        }
        if (boardCreateRequest.getContent() != null && !boardCreateRequest.getContent().isBlank()) {
            board.setContent(boardCreateRequest.getContent());
        }

        boardRepository.save(board);

        return ResponseEntity.ok().build();
    }

    // * 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        Boolean isExist = boardRepository.existsById(id);
        if (!isExist) {
            return ResponseEntity.notFound().build();
        }
        boardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
