package io.github.alexkond12.villagecensus.census;

import io.github.alexkond12.villagecensus.text.LangKeys;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.Optional;

/**
 * Everything the census knows about one villager, read from its brain memories.
 *
 * @param professionId     registry id of the profession, e.g. {@code minecraft:farmer}
 * @param professionName   display name of the profession (already translated by the client)
 * @param level            merchant level, 1 (Novice) to 5 (Master)
 * @param child            whether the villager is a baby
 * @param canWork          whether the villager can take a job (false for nitwits and babies)
 * @param home             position of the villager's bed, if it has one
 * @param jobSite          position of the villager's workstation, if it has one
 * @param jobPending       has claimed a workstation and is walking to it, but has not taken the job yet
 * @param lastSlept        game time of the last sleep, if it ever slept
 * @param ticksSinceSleep  ticks since the last sleep, or -1 if it never slept
 * @param sleptRecently    slept within the last in-game day
 * @param sawGolemRecently saw an iron golem recently (blocks summoning a new one)
 * @param canBreed         currently willing to breed
 * @param canSummonGolem   would currently take part in summoning a golem
 */
public record VillagerFacts(
        String professionId,
        Component professionName,
        int level,
        boolean child,
        boolean canWork,
        Optional<GlobalPos> home,
        Optional<GlobalPos> jobSite,
        boolean jobPending,
        Optional<Long> lastSlept,
        long ticksSinceSleep,
        boolean sleptRecently,
        boolean sawGolemRecently,
        boolean canBreed,
        boolean canSummonGolem
) {
    /** Profession id used for villagers without a job. */
    public static final String UNEMPLOYED_ID = "minecraft:none";

    public static VillagerFacts of(Villager villager, long gameTime) {
        Brain<Villager> brain = villager.getBrain();
        VillagerData data = villager.getVillagerData();
        Holder<VillagerProfession> profession = data.profession();

        boolean unemployed = profession.is(VillagerProfession.NONE);
        String professionId = unemployed ? UNEMPLOYED_ID : profession.getRegisteredName();
        Component professionName = unemployed
                ? Component.translatable(LangKeys.UNEMPLOYED)
                : profession.value().name();

        boolean child = villager.isBaby();
        boolean canWork = !child && !profession.is(VillagerProfession.NITWIT);

        Optional<GlobalPos> home = brain.getMemory(MemoryModuleType.HOME);
        Optional<GlobalPos> jobSite = brain.getMemory(MemoryModuleType.JOB_SITE);
        boolean jobPending = jobSite.isEmpty() && brain.hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
        Optional<Long> lastSlept = brain.getMemory(MemoryModuleType.LAST_SLEPT);
        boolean sawGolem = brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY);

        Long slept = lastSlept.orElse(null);
        long ticksSinceSleep = slept == null ? -1L : Math.max(0L, gameTime - slept);

        return new VillagerFacts(
                professionId,
                professionName,
                data.level(),
                child,
                canWork,
                home,
                jobSite,
                jobPending,
                lastSlept,
                ticksSinceSleep,
                GolemRules.sleptRecently(gameTime, slept),
                sawGolem,
                !child && villager.canBreed(),
                GolemRules.canSummonGolem(gameTime, slept, sawGolem));
    }
}
