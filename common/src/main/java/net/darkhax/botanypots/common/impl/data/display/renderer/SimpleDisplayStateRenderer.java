package net.darkhax.botanypots.common.impl.data.display.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.darkhax.bookshelf.common.api.util.IRenderHelper;
import net.darkhax.botanypots.common.api.data.display.math.TintColor;
import net.darkhax.botanypots.common.api.data.display.render.AbstractDisplayRenderer;
import net.darkhax.botanypots.common.api.data.display.types.RenderOptions;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.darkhax.botanypots.common.impl.data.display.types.SimpleDisplayState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class SimpleDisplayStateRenderer extends AbstractDisplayRenderer<SimpleDisplayState, RenderOptions> {

    public static final SimpleDisplayStateRenderer RENDERER = new SimpleDisplayStateRenderer();

    @Override
    public RenderOptions getRenderOptions(SimpleDisplayState display) {
        return display.renderOptions();
    }

    @Override
    public void render(BlockEntityRendererProvider.Context context, SimpleDisplayState display, RenderOptions renderOptions, PoseStack pose, Level level, BlockPos pos, float tickDelta, SubmitNodeCollector collector, CameraRenderState camera, int light, int overlay, BotanyPotBlockEntity pot, float progress, float growthScale, float heightOffset) {
        final BlockState blockState = display.getState();
        if (display.renderOptions().shouldRenderFluid()) {
            final FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                renderFluidBox(fluidState, blockState, pose, level, pos, collector, light);
            }
        }
        renderBlockState(blockState, context, pose, level, pos, collector, display.renderOptions().getColor(), light, overlay, renderOptions.getFaces());
    }

    public static void renderFluidBox(FluidState fluidState, BlockState blockState, PoseStack pose, Level level, BlockPos pos, SubmitNodeCollector collector, int light) {
        final FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        final BlockTintSource tintSource = model.tintSource();
        final int tint = tintSource != null ? ARGB.opaque(tintColor(tintSource, blockState, level, pos)) : -1;
        final int[] color = {ARGB.alpha(tint), ARGB.red(tint), ARGB.green(tint), ARGB.blue(tint)};
        collector.submitCustomGeometry(pose, Sheets.translucentBlockSheet(), (renderPose, consumer) -> IRenderHelper.GET.renderBox(consumer, renderPose.pose(), model.stillMaterial().sprite(), light, OverlayTexture.NO_OVERLAY, 0f, 1f, 0f, 1f, 0f, 1f, color));
    }

    public static void renderBlockState(BlockState blockState, BlockEntityRendererProvider.Context context, PoseStack pose, Level level, BlockPos pos, SubmitNodeCollector collector, Optional<TintColor> tintColor, int light, int overlay, Set<Direction> faces) {
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            final BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            final List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(RandomSource.create(42), parts);
            if (parts.isEmpty()) {
                return;
            }
            final boolean translucent = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT);
            collector.submitCustomGeometry(pose, translucent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet(), (renderPose, consumer) -> {
                final QuadInstance instance = new QuadInstance();
                for (BlockStateModelPart part : parts) {
                    for (Direction direction : faces) {
                        renderQuadList(blockState, level, pos, renderPose, consumer, instance, tintColor, part.getQuads(direction), light, overlay);
                    }
                    renderQuadList(blockState, level, pos, renderPose, consumer, instance, tintColor, part.getQuads(null), light, overlay);
                }
            });
        }
    }

    private static int tintColor(BlockTintSource tintSource, BlockState state, Level level, BlockPos pos) {
        return level instanceof BlockAndTintGetter tintGetter ? tintSource.colorInWorld(state, tintGetter, pos) : tintSource.color(state);
    }

    private static void renderQuadList(BlockState blockState, Level level, BlockPos pos, PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer consumer, QuadInstance instance, Optional<TintColor> tintColor, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        for (BakedQuad bakedQuad : quads) {
            int color = -1;
            if (tintColor.isPresent()) {
                color = tintColor.get().pack();
            }
            else if (bakedQuad.materialInfo().isTinted()) {
                final BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(blockState, bakedQuad.materialInfo().tintIndex());
                if (tintSource != null) {
                    color = ARGB.opaque(tintColor(tintSource, blockState, level, pos));
                }
            }
            instance.setColor(color);
            instance.setLightCoords(packedLight);
            instance.setOverlayCoords(packedOverlay);
            consumer.putBakedQuad(pose, bakedQuad, instance);
        }
    }
}
