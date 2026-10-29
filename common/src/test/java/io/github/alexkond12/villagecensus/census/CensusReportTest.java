package io.github.alexkond12.villagecensus.census;

import io.github.alexkond12.villagecensus.census.CensusReport.Hint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CensusReportTest {
    /** A healthy village: everyone housed and employed, enough villagers ready for a golem. */
    private static CensusReport healthy() {
        return new CensusReport(5, 1, Map.of("minecraft:farmer", 5), 8, 6, 6, 5,
                0, 0, 2, 6, 0, 6, 1);
    }

    @Test
    void emptyReportHasNoHints() {
        CensusReport empty = new CensusReport(0, 0, Map.of(), 3, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
        assertTrue(empty.isEmpty());
        assertTrue(empty.hints().isEmpty());
    }

    @Test
    void freeCountsNeverNegative() {
        CensusReport odd = new CensusReport(1, 0, Map.of(), 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(0, odd.freeBeds());
        assertEquals(0, odd.freeWorkstations());
    }

    @Test
    void healthyVillageSaysGolemPossible() {
        assertEquals(List.of(Hint.GOLEM_POSSIBLE), healthy().hints());
    }

    @Test
    void noFreeBedsIsReportedFirst() {
        CensusReport r = new CensusReport(4, 0, Map.of(), 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(Hint.NO_FREE_BEDS, r.hints().getFirst());
    }

    @Test
    void homelessWithFreeBedsMeansUnreachable() {
        CensusReport r = new CensusReport(4, 0, Map.of(), 6, 2, 4, 4, 2, 0, 0, 4, 0, 4, 0);
        assertTrue(r.hints().contains(Hint.BEDS_UNREACHABLE));
        assertFalse(r.hints().contains(Hint.NO_FREE_BEDS));
    }

    @Test
    void joblessHintDependsOnFreeWorkstations() {
        CensusReport none = new CensusReport(4, 0, Map.of(), 6, 4, 2, 2, 0, 2, 0, 4, 0, 4, 0);
        CensusReport some = new CensusReport(4, 0, Map.of(), 6, 4, 4, 2, 0, 2, 0, 4, 0, 4, 0);
        assertTrue(none.hints().contains(Hint.NO_FREE_WORKSTATIONS));
        assertTrue(some.hints().contains(Hint.WORKSTATIONS_UNREACHABLE));
    }

    @Test
    void golemBlockersAreExplained() {
        CensusReport r = new CensusReport(2, 0, Map.of(), 4, 2, 2, 2, 0, 0, 2, 0, 1, 0, 0);
        List<Hint> hints = r.hints();
        assertTrue(hints.contains(Hint.GOLEM_SEEN_RECENTLY));
        assertTrue(hints.contains(Hint.GOLEM_NEEDS_SLEEP));
        assertTrue(hints.contains(Hint.GOLEM_TOO_FEW_VILLAGERS));
        assertFalse(hints.contains(Hint.GOLEM_POSSIBLE));
    }

    @Test
    void onlyTheRealBlockerIsShown() {
        // 3 villagers, all slept, but all of them saw a golem -> only "seen recently".
        CensusReport r = new CensusReport(3, 0, Map.of(), 4, 3, 3, 3, 0, 0, 0, 3, 3, 0, 1);
        assertEquals(List.of(Hint.GOLEM_SEEN_RECENTLY), r.hints());
    }

    @Test
    void nonEmptyReportAlwaysHasAHint() {
        for (int villagers = 1; villagers <= 6; villagers++) {
            for (int ready = 0; ready <= villagers; ready++) {
                CensusReport r = new CensusReport(villagers, 0, Map.of(), villagers + 1, villagers, villagers, villagers,
                        0, 0, 0, villagers, 0, ready, 0);
                assertFalse(r.hints().isEmpty(), "no hint for " + villagers + " villagers / " + ready + " ready");
            }
        }
    }
}
