package io.github.alexkond12.villagecensus.neoforge;

import io.github.alexkond12.villagecensus.VillageCensus;
import io.github.alexkond12.villagecensus.item.CensusLedgerItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge entry point: registers the Census Ledger and hooks up the events the
 * shared code needs. All gameplay logic lives in the common project.
 */
@Mod(VillageCensus.MOD_ID)
public final class VillageCensusNeoForge {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VillageCensus.MOD_ID);

    public static final DeferredItem<CensusLedgerItem> LEDGER = ITEMS.registerItem(
            VillageCensus.LEDGER_ITEM_NAME, CensusLedgerItem::new, properties -> properties.stacksTo(1));

    public VillageCensusNeoForge(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(VillageCensusNeoForge::addToCreativeTab);
        NeoForge.EVENT_BUS.addListener(VillageCensusNeoForge::onEntityInteract);
        VillageCensus.LOGGER.info("{} loaded (NeoForge)", VillageCensus.MOD_NAME);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(LEDGER);
        }
    }

    /** Right-clicking a villager with the ledger shows its card instead of the trading screen. */
    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = CensusLedgerItem.useOnVillager(event.getEntity(), event.getHand(), event.getTarget());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
