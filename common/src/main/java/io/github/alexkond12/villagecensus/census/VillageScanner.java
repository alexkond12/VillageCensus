package io.github.alexkond12.villagecensus.census;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collects everything the census needs from the world around a position.
 * Runs on the logical server only.
 */
public final class VillageScanner {
    /** Never highlight more than this many blocks of each kind (keeps particle traffic small). */
    private static final int MAX_HIGHLIGHTED = 64;

    private static final Predicate<Holder<PoiType>> BEDS = holder -> holder.is(PoiTypes.HOME);
    private static final Predicate<Holder<PoiType>> WORKSTATIONS = holder -> holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE);

    private VillageScanner() {
    }

    /**
     * @param level  the server level to scan
     * @param center the centre of the scan (usually the player's position)
     * @param radius scan radius in blocks
     */
    public static ScanResult scan(ServerLevel level, BlockPos center, int radius) {
        long gameTime = level.getGameTime();
        AABB box = new AABB(center).inflate(radius);
        double radiusSqr = (double) radius * radius;
        Vec3 centerPos = Vec3.atCenterOf(center);

        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, box,
                v -> v.isAlive() && v.distanceToSqr(centerPos) <= radiusSqr);

        int adults = 0;
        int children = 0;
        int withoutBed = 0;
        int withoutJob = 0;
        int readyToBreed = 0;
        int sleptRecently = 0;
        int sawGolem = 0;
        int readyForGolem = 0;
        Map<String, Integer> professions = new LinkedHashMap<>();
        Map<String, Component> professionNames = new LinkedHashMap<>();

        for (Villager villager : villagers) {
            VillagerFacts facts = VillagerFacts.of(villager, gameTime);
            if (facts.child()) {
                children++;
            } else {
                adults++;
                professions.merge(facts.professionId(), 1, Integer::sum);
                professionNames.putIfAbsent(facts.professionId(), facts.professionName());
                if (facts.canWork() && facts.jobSite().isEmpty() && !facts.jobPending()) {
                    withoutJob++;
                }
                if (facts.canBreed()) {
                    readyToBreed++;
                }
            }
            if (facts.home().isEmpty()) {
                withoutBed++;
            }
            if (facts.sleptRecently()) {
                sleptRecently++;
            }
            if (facts.sawGolemRecently()) {
                sawGolem++;
            }
            if (facts.canSummonGolem()) {
                readyForGolem++;
            }
        }

        PoiManager poi = level.getPoiManager();
        List<BlockPos> beds = positions(poi, BEDS, center, radius, PoiManager.Occupancy.ANY);
        List<BlockPos> freeBeds = positions(poi, BEDS, center, radius, PoiManager.Occupancy.HAS_SPACE);
        List<BlockPos> jobs = positions(poi, WORKSTATIONS, center, radius, PoiManager.Occupancy.ANY);
        List<BlockPos> freeJobs = positions(poi, WORKSTATIONS, center, radius, PoiManager.Occupancy.HAS_SPACE);

        int golems = level.getEntitiesOfClass(IronGolem.class, box,
                g -> g.isAlive() && g.distanceToSqr(centerPos) <= radiusSqr).size();

        // Most common profession first, unemployed always last.
        Map<String, Integer> sortedProfessions = professions.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> VillagerFacts.UNEMPLOYED_ID.equals(e.getKey()) ? 1 : 0)
                        .thenComparing(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        CensusReport report = new CensusReport(
                adults, children, sortedProfessions,
                beds.size(), beds.size() - freeBeds.size(),
                jobs.size(), jobs.size() - freeJobs.size(),
                withoutBed, withoutJob, readyToBreed,
                sleptRecently, sawGolem, readyForGolem, golems);

        return new ScanResult(report, professionNames, limit(freeBeds, center), limit(freeJobs, center));
    }

    private static List<BlockPos> positions(PoiManager poi, Predicate<Holder<PoiType>> type, BlockPos center,
                                            int radius, PoiManager.Occupancy occupancy) {
        return poi.getInRange(type, center, radius, occupancy).map(PoiRecord::getPos).toList();
    }

    /** Closest positions first, at most {@link #MAX_HIGHLIGHTED}. */
    private static List<BlockPos> limit(List<BlockPos> positions, BlockPos center) {
        return positions.stream()
                .sorted(Comparator.comparingDouble(p -> p.distSqr(center)))
                .limit(MAX_HIGHLIGHTED)
                .toList();
    }

    /** Result of a scan: the numbers plus the data needed to display and highlight them. */
    public record ScanResult(CensusReport report, Map<String, Component> professionNames,
                             List<BlockPos> freeBeds, List<BlockPos> freeWorkstations) {
    }
}
