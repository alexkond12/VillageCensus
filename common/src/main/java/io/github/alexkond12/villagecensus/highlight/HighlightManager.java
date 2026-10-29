package io.github.alexkond12.villagecensus.highlight;

import io.github.alexkond12.villagecensus.VillageCensus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shows free beds (green sparkles) and free workstations (white sparks) to one player
 * for a few seconds. Only that player sees the particles.
 *
 * <p>Driven by {@code CensusLedgerItem#inventoryTick}, so it needs no loader-specific
 * tick event: the highlight runs while the ledger is in the player's inventory.</p>
 */
public final class HighlightManager {
    private static final int PARTICLE_INTERVAL_TICKS = 10;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private HighlightManager() {
    }

    public static void start(ServerPlayer player, List<BlockPos> freeBeds, List<BlockPos> freeWorkstations) {
        ServerLevel level = (ServerLevel) player.level();
        long until = level.getGameTime() + VillageCensus.HIGHLIGHT_SECONDS * 20L;
        SESSIONS.put(player.getUUID(), new Session(level.dimension(), List.copyOf(freeBeds), List.copyOf(freeWorkstations), until, -1L));
        // Show the first burst right away instead of waiting for the next interval.
        tick(level, player);
    }

    /** Called every tick for every ledger in a player's inventory; cheap when nothing is highlighted. */
    public static void tick(ServerLevel level, ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        long now = level.getGameTime();
        if (now > session.until() || !level.dimension().equals(session.dimension())) {
            SESSIONS.remove(player.getUUID());
            return;
        }
        // Several ledgers in one inventory call this several times per tick; emit once.
        if (session.lastEmitted() == now || (session.lastEmitted() >= 0 && now - session.lastEmitted() < PARTICLE_INTERVAL_TICKS)) {
            return;
        }
        SESSIONS.put(player.getUUID(), session.emittedAt(now));
        emit(level, player, session.beds(), ParticleTypes.HAPPY_VILLAGER);
        emit(level, player, session.workstations(), ParticleTypes.END_ROD);
    }

    /** Forget a player (e.g. on logout). Sessions also expire on their own. */
    public static void clear(UUID player) {
        SESSIONS.remove(player);
    }

    private static void emit(ServerLevel level, ServerPlayer player, List<BlockPos> positions, ParticleOptions particle) {
        for (BlockPos pos : positions) {
            level.sendParticles(player, particle, true, true,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    4, 0.25, 0.25, 0.25, 0.0);
        }
    }

    private record Session(ResourceKey<Level> dimension, List<BlockPos> beds, List<BlockPos> workstations,
                           long until, long lastEmitted) {
        Session emittedAt(long time) {
            return new Session(dimension, beds, workstations, until, time);
        }
    }
}
