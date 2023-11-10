package com.example.chess.model;

import com.example.chess.exception.InvalidKeyException;
import com.example.chess.exception.InvalidMoveException;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Document("boards")
public class Board {

    @Id
    private String id;
    private HashMap<String, Piece> pieces;
    private String[][] boardKey;
    private boolean whiteToMove;
    private Integer currentMove;
    private boolean check;
    private boolean checkmate;
    private boolean stalemate;
    private List<Move> history;
    private boolean shallow;

    public Board(String[][] boardKey, Integer currentMove, List<Move> history, boolean shallow) {
        this.boardKey = boardKey;
        this.currentMove = currentMove;
        this.whiteToMove = currentMove % 2 == 0;
        this.history = history;
        this.shallow = shallow;
        this.check = false;
        this.checkmate = false;
        this.stalemate = false;
        this.pieces = new HashMap<>();
        addPieces();
    }

    public Board shallowCopy() {
        String[][] keyCopy = new String[8][8];
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                keyCopy[i][j] = boardKey[i][j];
            }
        }
        return new Board(keyCopy, history.size(), List.copyOf(history), true);
    }

    private void addPieces() {
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                addPiece(boardKey[i][j], i, j);
            }
        }
        if (!checkmate) {
            pieces.forEach((key, value) -> value.generateMoves(this));
        }
    }

    private void addPiece(String key, int row, int col) {
        if (!key.isEmpty()) {
            char color = key.charAt(0);
            if (color == 'w') {
                addPiece(row, col, true, key);
            } else if (color == 'b') {
                addPiece(row, col, false, key);
            }
        }
    }

    private void addPiece(int row, int col, boolean white, String key) {
        char name = key.charAt(1);
        Piece piece = switch (name) {
            case 'p' -> new Pawn(row, col, white, shallow);
            case 'r' -> new Rook(row, col, white, shallow);
            case 'n' -> new Knight(row, col, white, shallow);
            case 'b' -> new Bishop(row, col, white, shallow);
            case 'k' -> new King(row, col, white, shallow);
            case 'q' -> new Queen(row, col, white, shallow);
            default -> throw new InvalidKeyException("Invalid piece key");
        };
        pieces.put(key, piece);
    }

    public void validateKingMove(boolean white, int[] move) {
        Set<Piece> attackers = pieces.values().stream().filter(piece -> piece.isWhite() != white).collect(Collectors.toSet());
        Stream<int[]> hotspots = attackers.stream().flatMap(piece -> piece.getMoves().stream());
        if (hotspots.anyMatch(m -> Arrays.equals(m, move))) {
            throw new InvalidMoveException("King move to attacked square");
        }
    }

    public void move(String moveCode) {
        if (checkmate) {
            throw new InvalidMoveException("Game is over");
        }
        String moveString = "";
        if (moveCode.length() == 4) {
            int[] move = new int[4];
            for (int i=0; i<4; i++) {
                move[i] = moveCode.charAt(i) - '0';
            }
            if (Arrays.stream(move).allMatch(i -> i < 8 && i >= 0)) {
                String key = boardKey[move[0]][move[1]];
                Piece piece = pieces.get(key);
                if (piece != null) {
                    if (piece.isWhite() != whiteToMove) {
                        throw new InvalidMoveException("It is " + (whiteToMove ? "white" : "black") + "'s turn");
                    }
                    int[] destination = Arrays.copyOfRange(move, 2, 4);
                    moveString += key.contains("p") ? "" : key.substring(1, 2);
                    String takenPieceKey = boardKey[move[2]][move[3]];
                    if (pieces.containsKey(takenPieceKey)) {
                        pieces.remove(takenPieceKey);
                        moveString += "x";
                    } else if (key.contains("p")) {
                        int direction = whiteToMove ? 1 : -1;
                        if(enPassantable(destination, direction)) {
                            takenPieceKey = boardKey[move[2] - direction][move[3]];
                            if (pieces.containsKey(takenPieceKey)) {
                                pieces.remove(takenPieceKey);
                                boardKey[move[2] - direction][move[3]] = "";
                                moveString += "x";
                            }
                        }
                    }
                    piece.move(destination);
                    char col = (char) (move[3] + 97);
                    moveString += col;
                    moveString += (move[2] + 1);
                    if (!shallow) {
                        history.add(new Move(moveCode, moveString));
                    }
                    boardKey[move[2]][move[3]] = boardKey[move[0]][move[1]];
                    boardKey[move[0]][move[1]] = "";
                    pieces = new HashMap<>();
                    addPieces();
                    Piece king = pieces.get(whiteToMove ? "wk" : "bk");
                    validateKingMove(whiteToMove, new int[]{king.getRow(), king.getCol()});
                    whiteToMove = !whiteToMove;
                    currentMove++;
                    if (!shallow) {
                        checkmate = pieces.values().stream().noneMatch(p -> p.isWhite() == whiteToMove && !p.getMoves().isEmpty());
                        try {
                            king = pieces.get(whiteToMove ? "wk" : "bk");
                            validateKingMove(whiteToMove, new int[]{king.getRow(), king.getCol()});
                            check = false;
                        } catch (InvalidMoveException e) {
                            check = true;
                        }
                        stalemate = checkmate && !check;
                    }
                } else {
                    throw new InvalidMoveException("No piece at given start coordinate");
                }
            } else {
                throw new InvalidMoveException("Unable to parse move code");
            }
        } else {
            throw new InvalidMoveException("Move code has incorrect format");
        }
    }

    public boolean enPassantable(int[] move, int direction) {
        if (currentMove > 1) {
            String lastMoveCode = history.get(history.size() - 1).getMoveCode();
            int[] lastMove = {lastMoveCode.charAt(0) - '0', lastMoveCode.charAt(1) - '0', lastMoveCode.charAt(2) - '0', lastMoveCode.charAt(3) - '0'};
            boolean lastMovePawn = boardKey[lastMove[2]][lastMove[3]].contains("p");
            boolean lastMovePushTwo = lastMove[2] - lastMove[0] == -2 * direction;
            boolean lastMoveAttackable = lastMove[2] + direction == move[0] && lastMove[3] == move[1];
            return lastMovePawn && lastMovePushTwo && lastMoveAttackable;
        } else {
            return false;
        }
    }
}
