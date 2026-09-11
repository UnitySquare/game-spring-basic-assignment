package com.gamebasic.ranking.service;

import com.gamebasic.ranking.client.RankingClient;
import com.gamebasic.ranking.dto.RankingEntry;
import com.gamebasic.ranking.dto.RankingResponse;
import com.gamebasic.ranking.dto.RankingSource;
import com.gamebasic.runcard.entity.CardType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RankingService {

    private final RankingClient rankingClient;

    public RankingService(RankingClient rankingClient) {
        this.rankingClient = rankingClient;
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
            if (isValidDuration(record)
                    && isValidHp(record)
                    && isValidDeck(record)
                    && isValidCardTypes(record)
                    && isValidAcquiredFloor(record)
                    && isValidBossFight(record)
                    && isValidFinishingCard(record)) {

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

    private boolean isValidDuration(RankingSource.Record record) {
        return record.getRun().getDurationSeconds()
                >= record.getRun().getClearedFloor() * 30;
    }

    private boolean isValidHp(RankingSource.Record record) {
        int hp = record.getRun().getFinalHp();

        return hp >= 1 && hp <= 99;
    }

    private boolean isValidDeck(RankingSource.Record record) {
        int actualSize = record.getDeck().getCards().size();
        int deckSize = record.getDeck().getSize();

        return actualSize >= 9
                && actualSize <= 20
                && deckSize == actualSize;
    }

    private boolean isValidCardTypes(RankingSource.Record record) {
        return record.getDeck().getCards().stream()
                .allMatch(card -> {
                    try {
                        CardType.valueOf(card.getCardType());
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                });
    }

    private boolean isValidAcquiredFloor(RankingSource.Record record) {
        return record.getDeck().getCards().stream()
                .allMatch(card ->
                        card.getAcquiredFloor() >= 0
                                && card.getAcquiredFloor() <= 9
                );
    }

    private boolean isValidBossFight(RankingSource.Record record) {
        RankingSource.BossFight bossFight = record.getBossFight();

        if (bossFight == null) {
            return false;
        }

        List<RankingSource.Phase> phases = bossFight.getPhases();

        if (phases == null || phases.size() != 3) {
            return false;
        }

        if (!"THRONE".equals(phases.get(0).getPhase())
                || !"UNBOUND".equals(phases.get(1).getPhase())
                || !"ECLIPSE".equals(phases.get(2).getPhase())) {
            return false;
        }

        if (phases.get(0).getTurns() < 1
                || phases.get(1).getTurns() < 1
                || phases.get(2).getTurns() < 1) {
            return false;
        }

        int phaseTurns = phases.get(0).getTurns()
                + phases.get(1).getTurns()
                + phases.get(2).getTurns();

        return bossFight.getTotalTurns() == phaseTurns;
    }

    private boolean isValidFinishingCard(RankingSource.Record record) {
        String finishingCard = record.getBossFight().getFinishingCard();

        return record.getDeck().getCards().stream()
                .anyMatch(card -> finishingCard.equals(card.getCardType()));
    }
}