package io.github.alexkond12.villagecensus.forge;

import io.github.alexkond12.villagecensus.VillageCensus;
import io.github.alexkond12.villagecensus.item.CensusLedgerItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Predicate;

/**
 * Minecraft Forge entry point: registers the Census Ledger and hooks up the events the
 * shared code needs. All gameplay logic lives in the common project.
 */
@Mod(VillageCensus.MOD_ID)
public final class VillageCensusForge {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VillageCensus.MOD_ID);

    public static final RegistryObject<Item> LEDGER = ITEMS.register(VillageCensus.LEDGER_ITEM_NAME,
            () -> new CensusLedgerItem(new Item.Properties()
                    .setId(ITEMS.key(VillageCensus.LEDGER_ITEM_NAME))
                    .stacksTo(1)));

    public VillageCensusForge(FMLJavaModLoadingContext context) {
        ITEMS.register(context.getModBusGroup());
        BuildCreativeModeTabContentsEvent.BUS.addListener(VillageCensusForge::addToCreativeTab);
        PlayerInteractEvent.EntityInteractSpecific.BUS.addListener(
                (Predicate<PlayerInteractEvent.EntityInteractSpecific>) VillageCensusForge::onEntityInteract);
        VillageCensus.LOGGER.info("{} loaded (Forge)", VillageCensus.MOD_NAME);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(LEDGER);
        }
    }

    /**
     * Right-clicking a villager with the ledger shows its card instead of the trading screen.
     *
     * @return {@code true} to cancel the vanilla interaction
     */
    private static boolean onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        InteractionResult result = CensusLedgerItem.useOnVillager(event.getEntity(), event.getHand(), event.getTarget());
        if (result == InteractionResult.PASS) {
            return false;
        }
        event.setCancellationResult(result);
        return true;
    }
}
