package net.darkhax.botanypots.neoforge;

import net.darkhax.botanypots.common.impl.data.SyncedRecipes;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BotanyPotsNeoForgeClient {

    public static void init() {
        NeoForge.EVENT_BUS.addListener(BotanyPotsNeoForgeClient::receiveRecipes);
    }

    private static void receiveRecipes(RecipesReceivedEvent event) {
        SyncedRecipes.update(event.getRecipeMap().values());
    }
}
