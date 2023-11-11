package com.example.chess.service;

import com.example.chess.exception.BoardNotFoundException;
import com.example.chess.model.Move;
import com.example.chess.repository.BoardRepository;
import com.example.chess.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@EnableMongoRepositories
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public Board createBoard() {
        String[][] boardKey = {{"wr1", "wn1", "wb1", "wq", "wk", "wb2", "wn2", "wr2"},
                {"wp1", "wp2", "wp3", "wp4", "wp5", "wp6", "wp7", "wp8"},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"bp1", "bp2", "bp3", "bp4", "bp5", "bp6", "bp7", "bp8"},
                {"br1", "bn1", "bb1", "bq", "bk", "bb2", "bn2", "br2"}
        };
        return boardRepository.save(new Board(boardKey, 0, null, false, false, false));
    }

    public Board getBoard(String id) {
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()) {
            return board.get();
        } else {
            throw new BoardNotFoundException("Board id=" + id + " not found");
        }
    }

    public Board move(String id, String moveCode) {
        Board board = getBoard(id);
        board.move(moveCode);
        return boardRepository.save(board);
    }
}
