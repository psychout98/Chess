package com.example.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {

    private String moveCode;
    private String moveString;
    private String boardKeyString;
}
