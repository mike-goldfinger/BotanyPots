package net.darkhax.botanypots.common.impl.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.darkhax.botanypots.common.api.data.display.math.AxisAlignedRotation;
import net.darkhax.botanypots.common.api.data.display.render.DisplayRenderer;
import net.darkhax.botanypots.common.api.data.display.types.Display;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.darkhax.botanypots.common.impl.data.display.types.BasicOptions;
import net.darkhax.botanypots.common.impl.data.display.types.SimpleDisplayState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BotanyPotRenderer implements BlockEntityRenderer<BotanyPotBlockEntity, BotanyPotRenderer.State> {

    private static final Map<Item, Display> DEFAULT_SOIL_DISPLAYS = new HashMap<>();
    private final BlockEntityRendererProvider.Context renderContext;

    public BotanyPotRenderer(BlockEntityRendererProvider.Context ctx) {
        this.renderContext = ctx;
    }

    private static AxisAlignedRotation getRotationForFace(Direction facing) {
        return switch (facing) {
            case EAST -> AxisAlignedRotation.Y_90;
            case NORTH -> AxisAlignedRotation.Y_180;
            case WEST -> AxisAlignedRotation.Y_270;
            default -> AxisAlignedRotation.Y_0;
        };
    }

    public static void applyRotation(AxisAlignedRotation rotation, PoseStack pose) {
        // Applies the rotation to the render.
        pose.mulPose(rotation.rotation);
        // Realigns the render with the original axis-aligned position.
        pose.translate(rotation.offset.x(), rotation.offset.y(), rotation.offset.z());
    }

    private static boolean canRenderSoil(ItemStack stack, @Nullable RecipeHolder<Soil> soil) {
        return !stack.isEmpty() && (soil != null || stack.getItem() instanceof BlockItem);
    }

    @NotNull
    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@NotNull BotanyPotBlockEntity pot, @NotNull State state, float partialTicks, @NotNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pot, state, partialTicks, cameraPosition, breakProgress);
        state.pot = pot;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(@NotNull State state, @NotNull PoseStack pose, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
        final BotanyPotBlockEntity pot = state.pot;
        if (pot == null) {
            return;
        }
        final Level level = pot.getLevel();
        if (level == null) {
            return;
        }
        final float tickDelta = state.partialTicks;
        final int light = state.lightCoords;
        final int overlay = OverlayTexture.NO_OVERLAY;
        final BlockPos pos = pot.getBlockPos();
        final int maxGrowth = pot.getRequiredGrowthTicks();
        final float progress = maxGrowth > 0 ? Math.clamp(Math.clamp(pot.growthTime.getTicks(), 0, maxGrowth) / maxGrowth, 0f, 1f) : 0f;
        final AxisAlignedRotation baseRotation = getRotationForFace(pot.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        final Soil soil = pot.getOrInvalidateSoil();
        final Crop crop = pot.getOrInvalidateCrop();
        final boolean isGrowing = crop != null && crop.isGrowthSustained(pot.getRecipeContext(), level);
        final ItemStack soilItem = pot.getSoilItem();

        final float cropScale = 0.40f + Math.clamp(0.60f * progress, 0f, 0.60f);
        float lastOffset = 0.3984375f;

        if (isGrowing) {
            for (Display display : crop.getDisplayState(pot.getRecipeContext(), pot.getLevel())) {
                lastOffset = DisplayRenderer.renderState(this.renderContext, display, pose, level, pos, tickDelta, collector, camera, light, overlay, pot, progress, cropScale, lastOffset);
            }
        }

        if (!soilItem.isEmpty() && camera.pos.y >= pos.getY()) {
            if (soil != null || (crop != null && soilItem.getItem() instanceof BlockItem && isGrowing)) {
                pose.pushPose();
                applyRotation(baseRotation, pose);
                pose.scale(1, 0.6375f, 1f);
                final Display soilRender = soil != null ? soil.getDisplay(pot.getRecipeContext(), level) : soilItem.getItem() instanceof BlockItem blockItem ? DEFAULT_SOIL_DISPLAYS.computeIfAbsent(blockItem, i -> new SimpleDisplayState(blockItem.getBlock().defaultBlockState(), BasicOptions.ofDefault())) : null;
                if (soilRender != null) {
                    DisplayRenderer.renderState(this.renderContext, soilRender, pose, level, pos, tickDelta, collector, camera, light, overlay, pot, progress, 1f, 0f);
                }
                pose.popPose();
            }
        }
    }

    @Override
    public int getViewDistance() {
        return 40;
    }

    /**
     * The render state for a botany pot. The pot is held directly as the display system needs access to the recipes
     * and growth state of the pot while submitting the render.
     */
    public static class State extends BlockEntityRenderState {

        @Nullable
        public BotanyPotBlockEntity pot;
        public float partialTicks;
    }
}
