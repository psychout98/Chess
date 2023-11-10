package com.example.chess.controller;

import com.example.chess.service.BoardService;
import com.example.chess.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin
@RequestMapping("/board")
public class BoardController {

    @Autowired
    BoardService boardService;

    @PostMapping("")
    public ResponseEntity<Board> createBoard() {
        return new ResponseEntity<>(boardService.createBoard(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoard(@PathVariable String id) {
        return new ResponseEntity<>(boardService.getBoard(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/move/{moveCode}")
    public ResponseEntity<Board> move(@PathVariable String id, @PathVariable String moveCode) {
        return new ResponseEntity<>(boardService.move(id, moveCode), HttpStatus.OK);
    }

//    @MessageMapping("/{id}/move/{moveCode}")
//    @SendTo("/board/{id}")
//    public ResponseEntity<BoardResponse> move(@PathVariable String id, @PathVariable String moveCode) {
//        return new ResponseEntity<>(boardService.move(id, moveCode), HttpStatus.OK);
//    }
}
