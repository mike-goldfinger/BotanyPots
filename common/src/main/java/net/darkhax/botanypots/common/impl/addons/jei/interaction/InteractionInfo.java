package net.darkhax.botanypots.common.impl.addons.jei.interaction;

import net.darkhax.botanypots.common.impl.data.recipe.interaction.BasicPotInteraction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public record InteractionInfo(Ingredient input, Optional<Ingredient> soilTest, Optional<Ingredient> seedTest, ItemStack soilOutput, ItemStack seedOutput) {
    public InteractionInfo(BasicPotInteraction interaction) {
        this(interaction.properties.heldTest(), interaction.properties.soilTest(), interaction.properties.seedTest(), interaction.properties.newSoil().map(ItemStackTemplate::create).orElse(ItemStack.EMPTY), interaction.properties.newSeed().map(ItemStackTemplate::create).orElse(ItemStack.EMPTY));
    }
}
