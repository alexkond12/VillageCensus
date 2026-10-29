package io.github.alexkond12.villagecensus.fabric;

import io.github.alexkond12.villagecensus.VillageCensus;
import io.github.alexkond12.villagecensus.item.CensusLedgerItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Fabric entry point: registers the Census Ledger and hooks up the events the
 * shared code needs. All gameplay logic lives in the common project.
 */
public final class VillageCensusFabric implements ModInitializer {
    public static final ResourceKey<Item> LEDGER_KEY =
            ResourceKey.create(Registries.ITEM, VillageCensus.id(VillageCensus.LEDGER_ITEM_NAME));

    @Override
    public void onInitialize() {
        Item ledger = Registry.register(BuiltInRegistries.ITEM, LEDGER_KEY,
                new CensusLedgerItem(new Item.Properties().setId(LEDGER_KEY).stacksTo(1)));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.accept(ledger));

        // Right-clicking a villager with the ledger shows its card instead of the trading screen.
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                CensusLedgerItem.useOnVillager(player, hand, entity));

        VillageCensus.LOGGER.info("{} loaded (Fabric)", VillageCensus.MOD_NAME);
    }
}
