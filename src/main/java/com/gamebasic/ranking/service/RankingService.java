package com.gamebasic.ranking.service;

import com.gamebasic.ranking.client.RankingClient;
import com.gamebasic.ranking.dto.RankingEntry;
import com.gamebasic.ranking.dto.RankingResponse;
import com.gamebasic.ranking.dto.RankingSource;
import com.gamebasic.ranking.validator.RankingValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RankingService {

    private final RankingClient rankingClient;
    private final RankingValidator rankingValidator;

    public RankingService(
            RankingClient rankingClient,
            RankingValidator rankingValidator
    ) {
        this.rankingClient = rankingClient;
        this.rankingValidator = rankingValidator;
    }

    public RankingResponse getRankings() {
        RankingSource source = rankingClient.fetch();
        List<RankingEntry> entries = new ArrayList<>();
        int rank = 1;
        int excludedCount = 0;

        List<RankingSource.Record> candidates = source.getRecords().stream()
                .filter(record -> "CLEARED".equals(record.getRun().getStatus()))
                .filter(record -> record.getRun().getClearedFloor() == 10)
                .toList();

        List<RankingSource.Record> validRecords = new ArrayList<>();
        for (RankingSource.Record record : candidates) {
            if (rankingValidator.isValid(record)) {
                validRecords.add(record);
            } else {
                excludedCount++;
            }
        }
        validRecords.sort(
                Comparator.comparingInt(
                                (RankingSource.Record record) ->
                                        record.getRun().getDurationSeconds()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        (RankingSource.Record record) ->
                                                record.getRun().getFinalHp()
                                ).reversed()
                        )
                        .thenComparing(RankingSource.Record::getId)
        );
        Set<String> playerIds = new HashSet<>();
        List<RankingSource.Record> rankedRecords = new ArrayList<>();

        for (RankingSource.Record record : validRecords) {
            if (playerIds.add(record.getPlayer().getId())) {
                rankedRecords.add(record);
            }
        }


        for (RankingSource.Record record : rankedRecords) {
            entries.add(new RankingEntry(
                    rank++,
                    record.getPlayer().getName(),
                    record.getRun().getDurationSeconds(),
                    record.getRun().getFinalHp(),
                    record.getBossFight().getTotalTurns(),
                    record.getDeck().getCards().size()
            ));
        }
        return new RankingResponse(
                source.getMeta().getSeason().getId(),
                source.getRecords().size(),
                excludedCount,
                entries
        );
    }
}