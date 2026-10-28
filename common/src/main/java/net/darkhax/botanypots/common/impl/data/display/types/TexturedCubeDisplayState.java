package net.darkhax.botanypots.common.impl.data.display.types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.botanypots.common.api.data.display.types.AbstractDisplay;
import net.darkhax.botanypots.common.api.data.display.types.DisplayType;
import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class TexturedCubeDisplayState extends AbstractDisplay<BasicOptions> {

    public static final Identifier TYPE_ID = BotanyPotsMod.id("textured_cube");
    public static final CachedSupplier<DisplayType<TexturedCubeDisplayState>> TYPE = CachedSupplier.cache(() -> DisplayType.get(TYPE_ID));
    public static final MapCodec<TexturedCubeDisplayState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(TexturedCubeDisplayState::getTexture),
            BasicOptions.CODEC.optionalFieldOf("options", BasicOptions.ofDefault()).forGetter(TexturedCubeDisplayState::renderOptions)
    ).apply(instance, TexturedCubeDisplayState::new));
    public static final StreamCodec<FriendlyByteBuf, TexturedCubeDisplayState> STREAM = StreamCodec.composite(Identifier.STREAM_CODEC, TexturedCubeDisplayState::getTexture, BasicOptions.STREAM, TexturedCubeDisplayState::renderOptions, TexturedCubeDisplayState::new);

    private final Identifier texture;

    public TexturedCubeDisplayState(Identifier texture, BasicOptions options) {
        super(options);
        this.texture = texture;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    @Override
    public DisplayType<?> getType() {
        return TYPE.get();
    }
}