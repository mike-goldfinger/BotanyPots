package net.darkhax.botanypots.fabric;

import net.darkhax.botanypots.common.impl.data.SyncedRecipes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;

public class BotanyPotsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> SyncedRecipes.update(recipes.recipes()));
    }
}
