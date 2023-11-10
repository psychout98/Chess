package com.example.chess.model;

import java.util.HashSet;
import java.util.Set;

public class Bishop extends Piece {

    public Bishop(int row, int col, boolean white, boolean shallow) { super(row, col, white, shallow); }

    @Override
    public void generateMoves(Board board) {
        addBishopMoves(board);
    }

}
