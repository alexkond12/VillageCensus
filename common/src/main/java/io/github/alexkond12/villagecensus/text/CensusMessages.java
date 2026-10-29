package io.github.alexkond12.villagecensus.text;

import io.github.alexkond12.villagecensus.VillageCensus;
import io.github.alexkond12.villagecensus.census.CensusReport;
import io.github.alexkond12.villagecensus.census.VillageScanner;
import io.github.alexkond12.villagecensus.census.VillagerFacts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns scan results into chat lines. Every visible word comes from a translation key,
 * so the text appears in the player's own language.
 */
public final class CensusMessages {
    private static final ChatFormatting HEADER = ChatFormatting.GOLD;
    private static final ChatFormatting LABEL = ChatFormatting.GRAY;
    private static final ChatFormatting VALUE = ChatFormatting.WHITE;
    private static final ChatFormatting GOOD = ChatFormatting.GREEN;
    private static final ChatFormatting BAD = ChatFormatting.RED;
    private static final ChatFormatting HINT = ChatFormatting.YELLOW;

    private CensusMessages() {
    }

    // ---- Village report -----------------------------------------------------------------

    public static List<Component> report(VillageScanner.ScanResult scan, int radius) {
        CensusReport r = scan.report();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(LangKeys.REPORT_HEADER, number(radius)).withStyle(HEADER, ChatFormatting.BOLD));

        if (r.isEmpty()) {
            lines.add(Component.translatable(LangKeys.REPORT_EMPTY, number(radius)).withStyle(LABEL));
            return lines;
        }

        lines.add(line(LangKeys.REPORT_VILLAGERS, number(r.villagers()), number(r.adults()), number(r.children())));
        if (!r.professions().isEmpty()) {
            lines.add(line(LangKeys.REPORT_PROFESSIONS, professionList(r.professions(), scan.professionNames())));
        }
        lines.add(line(LangKeys.REPORT_BEDS, number(r.beds()), number(r.claimedBeds()), freeCount(r.freeBeds())));
        lines.add(line(LangKeys.REPORT_WORKSTATIONS, number(r.workstations()), number(r.claimedWorkstations()), freeCount(r.freeWorkstations())));
        lines.add(line(LangKeys.REPORT_BREEDING, ratio(r.readyToBreed(), r.adults())));
        lines.add(line(LangKeys.REPORT_GOLEM_READY, ratio(r.readyForGolem(), r.villagers())));
        lines.add(line(LangKeys.REPORT_GOLEMS, number(r.ironGolems())));

        for (CensusReport.Hint hint : r.hints()) {
            ChatFormatting color = hint == CensusReport.Hint.ALL_GOOD || hint == CensusReport.Hint.GOLEM_POSSIBLE ? GOOD : HINT;
            lines.add(Component.literal("› ").withStyle(color).append(Component.translatable(LangKeys.hint(hint)).withStyle(color)));
        }
        lines.add(Component.translatable(LangKeys.REPORT_SHIFT_HINT).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        return lines;
    }

    // ---- Highlight ------------------------------------------------------------------------

    public static Component highlight(int freeBeds, int freeWorkstations) {
        if (freeBeds == 0 && freeWorkstations == 0) {
            return Component.translatable(LangKeys.HIGHLIGHT_NONE).withStyle(HINT);
        }
        return Component.translatable(LangKeys.HIGHLIGHT_START,
                number(freeBeds).withStyle(GOOD),
                number(freeWorkstations).withStyle(ChatFormatting.WHITE),
                number(VillageCensus.HIGHLIGHT_SECONDS)).withStyle(LABEL);
    }

    // ---- Villager card ---------------------------------------------------------------------

    public static List<Component> villager(Component villagerName, VillagerFacts f) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(LangKeys.VILLAGER_HEADER, villagerName.copy()).withStyle(HEADER, ChatFormatting.BOLD));

        if (f.child()) {
            lines.add(line(LangKeys.VILLAGER_PROFESSION, Component.translatable(LangKeys.VILLAGER_CHILD).withStyle(VALUE)));
        } else {
            MutableComponent profession = f.professionName().copy().withStyle(VALUE);
            if (f.canWork() && !VillagerFacts.UNEMPLOYED_ID.equals(f.professionId())) {
                profession.append(Component.literal(", ").withStyle(VALUE))
                        .append(Component.translatable("merchant.level." + f.level()).withStyle(VALUE));
            }
            lines.add(line(LangKeys.VILLAGER_PROFESSION, profession));
        }

        lines.add(line(LangKeys.VILLAGER_BED, position(f.home())));
        if (f.canWork()) {
            Component job = f.jobPending()
                    ? Component.translatable(LangKeys.VILLAGER_JOB_PENDING).withStyle(HINT)
                    : position(f.jobSite());
            lines.add(line(LangKeys.VILLAGER_WORKSTATION, job));
        }

        Component slept = f.ticksSinceSleep() < 0
                ? Component.translatable(LangKeys.VILLAGER_NEVER).withStyle(BAD)
                : Component.translatable(LangKeys.VILLAGER_SLEPT_AGO, number(f.ticksSinceSleep() / 1200L))
                .withStyle(f.sleptRecently() ? GOOD : BAD);
        lines.add(line(LangKeys.VILLAGER_SLEPT, slept));

        if (!f.child()) {
            lines.add(line(LangKeys.VILLAGER_BREED, yesNo(f.canBreed())));
        }

        MutableComponent golem = yesNo(f.canSummonGolem());
        if (!f.canSummonGolem()) {
            // Missing sleep is the harder blocker to fix (it needs a night), so it is named first.
            String reason = f.sleptRecently() ? LangKeys.VILLAGER_GOLEM_SAW : LangKeys.VILLAGER_GOLEM_SLEEP;
            golem.append(Component.literal(" ").append(Component.translatable(reason)).withStyle(LABEL));
        }
        lines.add(line(LangKeys.VILLAGER_GOLEM, golem));
        return lines;
    }

    // ---- Helpers ----------------------------------------------------------------------------

    /** "Label: value" — the key's own text is gray, its arguments keep their colours. */
    private static Component line(String key, Object... args) {
        return Component.translatable(key, args).withStyle(LABEL);
    }

    private static MutableComponent number(long value) {
        return Component.literal(Long.toString(value)).withStyle(VALUE);
    }

    private static MutableComponent freeCount(int free) {
        return Component.literal(Integer.toString(free)).withStyle(free > 0 ? GOOD : BAD);
    }

    private static MutableComponent ratio(int part, int whole) {
        return Component.literal(part + "/" + whole).withStyle(part > 0 ? GOOD : BAD);
    }

    private static MutableComponent yesNo(boolean value) {
        return Component.translatable(value ? LangKeys.YES : LangKeys.NO).withStyle(value ? GOOD : BAD);
    }

    private static MutableComponent position(Optional<GlobalPos> pos) {
        if (pos.isEmpty()) {
            return Component.translatable(LangKeys.VILLAGER_NONE).withStyle(BAD);
        }
        BlockPos p = pos.get().pos();
        return Component.translatable(LangKeys.VILLAGER_POSITION, p.getX(), p.getY(), p.getZ()).withStyle(GOOD);
    }

    private static MutableComponent professionList(Map<String, Integer> counts, Map<String, Component> names) {
        MutableComponent list = Component.empty();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first) {
                list.append(Component.literal(", ").withStyle(LABEL));
            }
            first = false;
            Component name = names.getOrDefault(entry.getKey(), Component.literal(entry.getKey()));
            list.append(Component.translatable(LangKeys.REPORT_PROFESSION_ENTRY, name.copy(), entry.getValue()).withStyle(VALUE));
        }
        return list;
    }
}
