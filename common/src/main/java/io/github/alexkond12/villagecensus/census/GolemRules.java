package io.github.alexkond12.villagecensus.census;

/**
 * The vanilla rules that decide whether a villager is able to summon an iron golem.
 *
 * <p>Kept free of Minecraft classes so the logic can be unit-tested. The numbers mirror
 * the vanilla {@code Villager} implementation.</p>
 */
public final class GolemRules {
    /** A villager only wants a golem if it slept within this many ticks (one in-game day). */
    public static final long SLEEP_WINDOW_TICKS = 24_000L;
    /** Villagers that gossip together need at least this many ready villagers nearby. */
    public static final int GOSSIP_MIN_VILLAGERS = 5;
    /** Villagers that panic (e.g. see a zombie) need at least this many ready villagers nearby. */
    public static final int PANIC_MIN_VILLAGERS = 3;

    private GolemRules() {
    }

    /**
     * @param gameTime              current level game time
     * @param lastSlept             game time of the villager's last sleep, or {@code null} if never
     * @param golemDetectedRecently whether the villager saw an iron golem recently
     * @return {@code true} if this villager would take part in summoning a golem
     */
    public static boolean canSummonGolem(long gameTime, Long lastSlept, boolean golemDetectedRecently) {
        return sleptRecently(gameTime, lastSlept) && !golemDetectedRecently;
    }

    /** Whether the villager slept within the last in-game day. */
    public static boolean sleptRecently(long gameTime, Long lastSlept) {
        return lastSlept != null && gameTime - lastSlept < SLEEP_WINDOW_TICKS;
    }
}
