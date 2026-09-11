package com.gamebasic.ranking.validator;

import com.gamebasic.ranking.dto.RankingSource;
import com.gamebasic.runcard.entity.CardType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RankingValidator {

    public boolean isValidDuration(RankingSource.Record record) {
        return record.getRun().getDurationSeconds()
                >= record.getRun().getClearedFloor() * 30;
    }

    public boolean isValidHp(RankingSource.Record record) {
        int hp = record.getRun().getFinalHp();

        return hp >= 1 && hp <= 99;
    }

    public boolean isValidDeck(RankingSource.Record record) {
        int actualSize = record.getDeck().getCards().size();
        int deckSize = record.getDeck().getSize();

        return actualSize >= 9
                && actualSize <= 20
                && deckSize == actualSize;
    }

    public boolean isValidCardTypes(RankingSource.Record record) {
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

    public boolean isValidAcquiredFloor(RankingSource.Record record) {
        return record.getDeck().getCards().stream()
                .allMatch(card ->
                        card.getAcquiredFloor() >= 0
                                && card.getAcquiredFloor() <= 9
                );
    }

    public boolean isValidBossFight(RankingSource.Record record) {
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

    public boolean isValidFinishingCard(RankingSource.Record record) {
        String finishingCard = record.getBossFight().getFinishingCard();

        return record.getDeck().getCards().stream()
                .anyMatch(card -> finishingCard.equals(card.getCardType()));
    }

    public boolean isValid(RankingSource.Record record) {
        return isValidDuration(record)
                && isValidHp(record)
                && isValidDeck(record)
                && isValidCardTypes(record)
                && isValidAcquiredFloor(record)
                && isValidBossFight(record)
                && isValidFinishingCard(record);
    }
}