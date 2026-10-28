package net.darkhax.botanypots.fabric;

import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.darkhax.botanypots.common.impl.data.recipe.crop.BasicCrop;
import net.darkhax.botanypots.common.impl.data.recipe.crop.BlockDerivedCrop;
import net.darkhax.botanypots.common.impl.data.recipe.fertilizer.BasicFertilizer;
import net.darkhax.botanypots.common.impl.data.recipe.interaction.BasicPotInteraction;
import net.darkhax.botanypots.common.impl.data.recipe.soil.BasicSoil;
import net.darkhax.botanypots.common.impl.data.recipe.soil.BlockDerivedSoil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

public class BotanyPotsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        BotanyPotsMod.init();
        RecipeSynchronization.synchronizeRecipeSerializer(BasicSoil.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(BlockDerivedSoil.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(BasicCrop.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(BlockDerivedCrop.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(BasicPotInteraction.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(BasicFertilizer.SERIALIZER);
    }
}
