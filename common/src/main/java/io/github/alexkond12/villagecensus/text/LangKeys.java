package io.github.alexkond12.villagecensus.text;

import io.github.alexkond12.villagecensus.census.CensusReport;

import java.util.Locale;

/**
 * Every translation key used by the mod, in one place.
 *
 * <p>All keys must exist in {@code assets/villagecensus/lang/en_us.json}; the unit test
 * {@code LangFilesTest} checks this and checks that every other language file has
 * exactly the same keys.</p>
 */
public final class LangKeys {
    private static final String P = "villagecensus.";

    // ---- Item ---------------------------------------------------------------------------
    public static final String ITEM_LEDGER = "item.villagecensus.census_ledger";
    public static final String TOOLTIP_USE = P + "tooltip.use";
    public static final String TOOLTIP_SNEAK = P + "tooltip.sneak";
    public static final String TOOLTIP_VILLAGER = P + "tooltip.villager";

    // ---- Village report -----------------------------------------------------------------
    public static final String REPORT_HEADER = P + "report.header";
    public static final String REPORT_EMPTY = P + "report.empty";
    public static final String REPORT_VILLAGERS = P + "report.villagers";
    public static final String REPORT_PROFESSIONS = P + "report.professions";
    public static final String REPORT_PROFESSION_ENTRY = P + "report.profession_entry";
    public static final String REPORT_BEDS = P + "report.beds";
    public static final String REPORT_WORKSTATIONS = P + "report.workstations";
    public static final String REPORT_BREEDING = P + "report.breeding";
    public static final String REPORT_GOLEM_READY = P + "report.golem_ready";
    public static final String REPORT_GOLEMS = P + "report.golems";
    public static final String REPORT_SHIFT_HINT = P + "report.shift_hint";

    // ---- Hints (one per CensusReport.Hint) -------------------------------------------------
    public static final String HINT_PREFIX = P + "hint.";

    // ---- Highlight ------------------------------------------------------------------------
    public static final String HIGHLIGHT_START = P + "highlight.start";
    public static final String HIGHLIGHT_NONE = P + "highlight.none";

    // ---- Villager card ----------------------------------------------------------------------
    public static final String VILLAGER_HEADER = P + "villager.header";
    public static final String VILLAGER_PROFESSION = P + "villager.profession";
    public static final String VILLAGER_CHILD = P + "villager.child";
    public static final String VILLAGER_BED = P + "villager.bed";
    public static final String VILLAGER_WORKSTATION = P + "villager.workstation";
    public static final String VILLAGER_POSITION = P + "villager.position";
    public static final String VILLAGER_NONE = P + "villager.none";
    public static final String VILLAGER_JOB_PENDING = P + "villager.job_pending";
    public static final String VILLAGER_SLEPT = P + "villager.slept";
    public static final String VILLAGER_SLEPT_AGO = P + "villager.slept_ago";
    public static final String VILLAGER_NEVER = P + "villager.never";
    public static final String VILLAGER_BREED = P + "villager.breed";
    public static final String VILLAGER_GOLEM = P + "villager.golem";
    public static final String VILLAGER_GOLEM_SAW = P + "villager.golem.saw_golem";
    public static final String VILLAGER_GOLEM_SLEEP = P + "villager.golem.needs_sleep";

    // ---- Shared words -------------------------------------------------------------------------
    public static final String YES = P + "yes";
    public static final String NO = P + "no";
    public static final String UNEMPLOYED = P + "profession.unemployed";

    private LangKeys() {
    }

    /** Translation key of a report hint, e.g. {@code villagecensus.hint.no_free_beds}. */
    public static String hint(CensusReport.Hint hint) {
        return HINT_PREFIX + hint.name().toLowerCase(Locale.ROOT);
    }
}
