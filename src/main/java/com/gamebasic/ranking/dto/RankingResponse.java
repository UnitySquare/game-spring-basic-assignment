package com.gamebasic.ranking.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class RankingResponse {

    private final String season;
    private final int totalRecords;
    private final int excludedCount;
    private final List<RankingEntry> entries;

    public RankingResponse(
            String season,
            int totalRecords,
            int excludedCount,
            List<RankingEntry> entries
    ) {
        this.season = season;
        this.totalRecords = totalRecords;
        this.excludedCount = excludedCount;
        this.entries = entries;
    }
}