package com.gamebasic.runcard.dto;

import lombok.Getter;

@Getter
public class DeckCount {

    private final Long gameId;
    private final long count;

    public DeckCount(Long gameId, long count) {
        this.gameId = gameId;
        this.count = count;
    }
}