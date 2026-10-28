package net.darkhax.botanypots.neoforge;

import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.darkhax.botanypots.common.impl.data.SyncedRecipes;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@Mod(BotanyPotsMod.MOD_ID)
public class BotanyPotsNeoForge {

    public BotanyPotsNeoForge(IEventBus bus, Dist dist) {
        BotanyPotsMod.init();
        bus.addListener(BotanyPotsNeoForge::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(BotanyPotsNeoForge::syncRecipes);
        if (dist.isClient()) {
            BotanyPotsNeoForgeClient.init();
        }
    }

    private static void syncRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(SyncedRecipes.syncedTypes());
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BotanyPotBlockEntity.TYPE.get(), (be, side) -> {
            if (side == Direction.DOWN) {
                return new WorldlyContainerWrapper(be, Direction.DOWN);
            }
            return null;
        });
    }
}
