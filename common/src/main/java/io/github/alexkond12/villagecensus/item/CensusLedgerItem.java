package io.github.alexkond12.villagecensus.item;

import io.github.alexkond12.villagecensus.VillageCensus;
import io.github.alexkond12.villagecensus.census.VillageScanner;
import io.github.alexkond12.villagecensus.census.VillagerFacts;
import io.github.alexkond12.villagecensus.highlight.HighlightManager;
import io.github.alexkond12.villagecensus.text.CensusMessages;
import io.github.alexkond12.villagecensus.text.LangKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Census Ledger.
 * <ul>
 *     <li>Right-click: chat report about the village around the player.</li>
 *     <li>Sneak + right-click: highlights free beds and workstations for a few seconds.</li>
 *     <li>Right-click a villager: detailed card about that villager
 *     (handled through {@link #useOnVillager}, called from each loader's entity-interact event,
 *     because villagers would otherwise open their trading screen first).</li>
 * </ul>
 */
public class CensusLedgerItem extends Item {
    public CensusLedgerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            VillageScanner.ScanResult scan = VillageScanner.scan(serverLevel, serverPlayer.blockPosition(), VillageCensus.SCAN_RADIUS);
            if (serverPlayer.isShiftKeyDown()) {
                HighlightManager.start(serverPlayer, scan.freeBeds(), scan.freeWorkstations());
                serverPlayer.sendSystemMessage(CensusMessages.highlight(scan.freeBeds().size(), scan.freeWorkstations().size()));
            } else {
                send(serverPlayer, CensusMessages.report(scan, VillageCensus.SCAN_RADIUS));
            }
            playPageSound(serverLevel, serverPlayer);
            serverPlayer.getCooldowns().addCooldown(stack, VillageCensus.USE_COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Shows the villager card. Called by the loader glue when a player right-clicks an entity.
     *
     * @return {@link InteractionResult#PASS} if the click is not ours (not a ledger or not a villager),
     * otherwise {@link InteractionResult#SUCCESS} on both sides so the trading screen does not open.
     */
    public static InteractionResult useOnVillager(Player player, InteractionHand hand, Entity target) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof CensusLedgerItem) || !(target instanceof Villager villager)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel) {
            VillagerFacts facts = VillagerFacts.of(villager, serverLevel.getGameTime());
            send(serverPlayer, CensusMessages.villager(villager.getDisplayName(), facts));
            playPageSound(serverLevel, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof ServerPlayer player) {
            HighlightManager.tick(level, player);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(LangKeys.TOOLTIP_USE).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(LangKeys.TOOLTIP_SNEAK).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(LangKeys.TOOLTIP_VILLAGER).withStyle(ChatFormatting.GRAY));
    }

    private static void send(ServerPlayer player, List<Component> lines) {
        lines.forEach(player::sendSystemMessage);
    }

    private static void playPageSound(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
