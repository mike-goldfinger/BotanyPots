package net.darkhax.botanypots.common.impl.addons.jei.interaction;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.darkhax.botanypots.common.impl.addons.jei.BotanyPotsJeiPlugin;
import net.darkhax.botanypots.common.impl.BotanyPotsContent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractionCategory implements IRecipeCategory<InteractionInfo> {

    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic blankSlot;
    private final IDrawableStatic plusIcon;
    private final IDrawableStatic arrowIcon;

    public InteractionCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("gui.jei.category.botanypots.interaction");
        this.icon = guiHelper.createDrawableItemStack(BotanyPotsContent.TAB_ICON.get());
        this.blankSlot = guiHelper.getSlotDrawable();
        this.plusIcon = guiHelper.getRecipePlusSign();
        this.arrowIcon = guiHelper.getRecipeArrow();
    }

    @NotNull
    @Override
    public RecipeType<InteractionInfo> getRecipeType() {
        return BotanyPotsJeiPlugin.INTERACTION;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 128;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull InteractionInfo recipe, @NotNull IFocusGroup focuses) {
        final IRecipeSlotBuilder heldItem = builder.addSlot(RecipeIngredientRole.INPUT, 12 + 1, 24 + 1);
        heldItem.addIngredients(recipe.input());

        final IRecipeSlotBuilder soilInput = builder.addSlot(RecipeIngredientRole.INPUT, 51 + 1, 36 + 1);
        recipe.soilTest().ifPresent(soilInput::addIngredients);

        final IRecipeSlotBuilder soilOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 99 + 1, 36 + 1);
        soilOutput.addItemStack(recipe.soilOutput());

        final IRecipeSlotBuilder seedInput = builder.addSlot(RecipeIngredientRole.INPUT, 51 + 1, 11 + 1);
        recipe.seedTest().ifPresent(seedInput::addIngredients);

        final IRecipeSlotBuilder seedOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 99 + 1, 11 + 1);
        seedOutput.addItemStack(recipe.seedOutput());
    }

    @Override
    public void draw(@NotNull InteractionInfo recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.blankSlot.draw(graphics, 12, 24); // hand input
        this.plusIcon.draw(graphics, 34, 26); // Plus Sign
        this.blankSlot.draw(graphics, 51, 36); // soil input
        this.blankSlot.draw(graphics, 99, 36); // soil output
        if (!recipe.seedOutput().isEmpty() || !recipe.soilOutput().isEmpty()) {
            this.arrowIcon.draw(graphics, 73, 24); // arrow
        }
        this.blankSlot.draw(graphics, 51, 11); // seed input
        this.blankSlot.draw(graphics, 99, 11); // seed output
    }
}
