package net.darkhax.botanypots.common.impl.data.display.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.darkhax.bookshelf.common.api.util.IRenderHelper;
import net.darkhax.botanypots.common.api.data.display.math.TintColor;
import net.darkhax.botanypots.common.api.data.display.render.AbstractDisplayRenderer;
import net.darkhax.botanypots.common.api.data.display.types.RenderOptions;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.darkhax.botanypots.common.impl.data.display.types.TexturedCubeDisplayState;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class TexturedCubeStateRenderer extends AbstractDisplayRenderer<TexturedCubeDisplayState, RenderOptions> {

    public static final TexturedCubeStateRenderer RENDERER = new TexturedCubeStateRenderer();

    @Override
    public RenderOptions getRenderOptions(TexturedCubeDisplayState display) {
        return display.renderOptions();
    }

    @Override
    public void render(BlockEntityRendererProvider.Context context, TexturedCubeDisplayState display, RenderOptions renderOptions, PoseStack pose, Level level, BlockPos pos, float tickDelta, SubmitNodeCollector collector, CameraRenderState camera, int light, int overlay, BotanyPotBlockEntity pot, float progress, float growthScale, float heightOffset) {
        final TextureAtlasSprite sprite = IRenderHelper.GET.blockSprite(display.getTexture());
        final int[] color = display.renderOptions().getColor().orElse(TintColor.WHITE).asArray();
        collector.submitCustomGeometry(pose, Sheets.translucentBlockSheet(), (renderPose, consumer) -> IRenderHelper.GET.renderBox(consumer, renderPose.pose(), sprite, light, overlay, 0f, 1f, 0f, 1f, 0f, 1f, color));
    }
}
