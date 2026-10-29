package io.github.alexkond12.villagecensus;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants of the Village Census mod.
 *
 * <p>Everything in the {@code common} project is loader-independent. Each loader project
 * (fabric / neoforge / forge) only registers the item and forwards a few events here.</p>
 */
public final class VillageCensus {
    public static final String MOD_ID = "villagecensus";
    public static final String MOD_NAME = "Village Census";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /** Registry name of the only item the mod adds. */
    public static final String LEDGER_ITEM_NAME = "census_ledger";

    /** How far (in blocks) around the player the census looks for villagers, beds and workstations. */
    public static final int SCAN_RADIUS = 48;
    /** How long free beds and workstations stay highlighted, in seconds. */
    public static final int HIGHLIGHT_SECONDS = 10;
    /** Cooldown after using the ledger, in ticks (1 second). Prevents chat spam. */
    public static final int USE_COOLDOWN_TICKS = 20;

    private VillageCensus() {
    }

    /** Creates an identifier in the mod's namespace, e.g. {@code villagecensus:census_ledger}. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
