package net.darkhax.botanypots.common.api.data.growthamount;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record GrowthAmountType<T extends GrowthAmount>(Identifier typeID, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {

    private static final Map<Identifier, GrowthAmountType<?>> REGISTRY = new HashMap<>();
    public static final Codec<GrowthAmountType<?>> TYPE_CODEC = Identifier.CODEC.xmap(REGISTRY::get, GrowthAmountType::typeID);
    public static final StreamCodec<ByteBuf, ? extends GrowthAmountType<?>> TYPE_STREAM = Identifier.STREAM_CODEC.map(REGISTRY::get, GrowthAmountType::typeID);
    public static final Codec<GrowthAmount> GROWTH_AMOUNT_CODEC = TYPE_CODEC.dispatch(GrowthAmount::getType, GrowthAmountType::codec);
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final StreamCodec<RegistryFriendlyByteBuf, GrowthAmount> GROWTH_AMOUNT_STREAM = StreamCodec.of(
            (buf, val) -> {
                buf.writeIdentifier(val.getType().typeID());
                ((StreamCodec) val.getType().stream).encode(buf, val);
            },
            buf -> {
                final Identifier id = buf.readIdentifier();
                if (!REGISTRY.containsKey(id)) {
                    BotanyPotsMod.LOG.error("Growth type {} does not exist!", id);
                    throw new IllegalStateException("Growth type " + id + " does not exist.");
                }
                return REGISTRY.get(id).stream.decode(buf);
            }
    );

    public static Supplier<GrowthAmountType<?>> getLazy(Identifier id) {
        return CachedSupplier.cache(() -> get(id));
    }

    public static GrowthAmountType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static <T extends GrowthAmount> GrowthAmountType<T> register(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {
        final GrowthAmountType<T> type = new GrowthAmountType<>(id, codec, stream);
        if (REGISTRY.containsKey(id)) {
            BotanyPotsMod.LOG.warn("Growth type {} has already been assigned to {}. Replacing it with {}.", id, REGISTRY.get(id), type);
        }
        REGISTRY.put(id, type);
        return type;
    }
}