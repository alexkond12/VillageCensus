package io.github.alexkond12.villagecensus.census;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain numbers collected by a village scan. Contains no Minecraft classes, so the
 * hint logic can be unit-tested.
 *
 * @param adults                 adult villagers
 * @param children               baby villagers
 * @param professions            profession id (e.g. {@code minecraft:farmer}) to number of adults, in display order
 * @param beds                   beds (home points of interest) in range
 * @param claimedBeds            beds that already belong to a villager
 * @param workstations           job site blocks in range
 * @param claimedWorkstations    job site blocks that already belong to a villager
 * @param villagersWithoutBed    villagers (adults and children) that have no bed
 * @param adultsWithoutJob       adults that can work but have no job site and are not walking to one (nitwits excluded)
 * @param readyToBreed           adults that are currently willing to breed
 * @param sleptRecently          villagers that slept within the last day
 * @param sawGolemRecently       villagers that saw an iron golem recently
 * @param readyForGolem          villagers that would currently take part in summoning a golem
 * @param ironGolems             iron golems in range
 */
public record CensusReport(
        int adults,
        int children,
        Map<String, Integer> professions,
        int beds,
        int claimedBeds,
        int workstations,
        int claimedWorkstations,
        int villagersWithoutBed,
        int adultsWithoutJob,
        int readyToBreed,
        int sleptRecently,
        int sawGolemRecently,
        int readyForGolem,
        int ironGolems
) {
    public CensusReport {
        professions = Collections.unmodifiableMap(new LinkedHashMap<>(professions));
    }

    public int villagers() {
        return adults + children;
    }

    public int freeBeds() {
        return Math.max(0, beds - claimedBeds);
    }

    public int freeWorkstations() {
        return Math.max(0, workstations - claimedWorkstations);
    }

    public boolean isEmpty() {
        return villagers() == 0;
    }

    /** Advice for the player, most important first. Never empty for a non-empty report. */
    public List<Hint> hints() {
        List<Hint> hints = new ArrayList<>();
        if (isEmpty()) {
            return hints;
        }
        if (freeBeds() == 0) {
            hints.add(Hint.NO_FREE_BEDS);
        } else if (villagersWithoutBed > 0) {
            hints.add(Hint.BEDS_UNREACHABLE);
        }
        if (adultsWithoutJob > 0) {
            hints.add(freeWorkstations() == 0 ? Hint.NO_FREE_WORKSTATIONS : Hint.WORKSTATIONS_UNREACHABLE);
        }
        if (readyForGolem >= GolemRules.PANIC_MIN_VILLAGERS) {
            hints.add(Hint.GOLEM_POSSIBLE);
        } else {
            if (sawGolemRecently > 0) {
                hints.add(Hint.GOLEM_SEEN_RECENTLY);
            }
            if (sleptRecently < villagers()) {
                hints.add(Hint.GOLEM_NEEDS_SLEEP);
            }
            if (villagers() < GolemRules.PANIC_MIN_VILLAGERS) {
                hints.add(Hint.GOLEM_TOO_FEW_VILLAGERS);
            }
        }
        if (hints.isEmpty()) {
            hints.add(Hint.ALL_GOOD);
        }
        return hints;
    }

    /** Advice shown under a report. Each hint has a translation key in {@code LangKeys}. */
    public enum Hint {
        NO_FREE_BEDS,
        BEDS_UNREACHABLE,
        NO_FREE_WORKSTATIONS,
        WORKSTATIONS_UNREACHABLE,
        GOLEM_POSSIBLE,
        GOLEM_SEEN_RECENTLY,
        GOLEM_NEEDS_SLEEP,
        GOLEM_TOO_FEW_VILLAGERS,
        ALL_GOOD
    }
}
