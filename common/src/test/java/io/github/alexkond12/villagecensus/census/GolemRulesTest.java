package io.github.alexkond12.villagecensus.census;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GolemRulesTest {
    @Test
    void villagerThatNeverSleptCannotSummon() {
        assertFalse(GolemRules.canSummonGolem(100_000L, null, false));
    }

    @Test
    void villagerThatSleptRecentlyCanSummon() {
        assertTrue(GolemRules.canSummonGolem(100_000L, 100_000L - 1_000L, false));
    }

    @Test
    void sleepWindowIsOneDayExclusive() {
        long now = 200_000L;
        assertTrue(GolemRules.sleptRecently(now, now - GolemRules.SLEEP_WINDOW_TICKS + 1));
        assertFalse(GolemRules.sleptRecently(now, now - GolemRules.SLEEP_WINDOW_TICKS));
    }

    @Test
    void seeingAGolemBlocksSummoning() {
        assertFalse(GolemRules.canSummonGolem(100_000L, 99_000L, true));
    }
}
