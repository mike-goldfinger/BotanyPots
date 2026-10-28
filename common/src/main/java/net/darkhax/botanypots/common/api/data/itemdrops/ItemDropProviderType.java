package net.darkhax.botanypots.common.api.data.itemdrops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.darkhax.bookshelf.common.api.data.codecs.map.MapCodecs;
import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a type of drop provider. Handles serialization.
 *
 * @param typeID The ID for the type.
 * @param codec  A codec that serializes the provider.
 * @param stream A stream codec that serializes the provider.
 * @param <T>    The type of the provider.
 */
public record ItemDropProviderType<T extends ItemDropProvider>(Identifier typeID, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {

    private static final Map<Identifier, ItemDropProviderType<?>> REGISTRY = new HashMap<>();
    public static final Codec<ItemDropProviderType<?>> TYPE_CODEC = Identifier.CODEC.xmap(REGISTRY::get, ItemDropProviderType::typeID);
    public static final StreamCodec<ByteBuf, ? extends ItemDropProviderType<?>> TYPE_STREAM = Identifier.STREAM_CODEC.map(REGISTRY::get, ItemDropProviderType::typeID);
    public static final Codec<ItemDropProvider> DROP_PROVIDER_CODEC = TYPE_CODEC.dispatch(ItemDropProvider::getType, ItemDropProviderType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDropProvider> DROP_PROVIDER_STREAM = new StreamCodec<>() {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ItemDropProvider dropProvider) {
            buf.writeIdentifier(dropProvider.getType().typeID());
            ((StreamCodec) dropProvider.getType().stream).encode(buf, dropProvider);
        }

        @NotNull
        @Override
        public ItemDropProvider decode(@NotNull RegistryFriendlyByteBuf buf) {
            final Identifier id = buf.readIdentifier();
            if (!REGISTRY.containsKey(id)) {
                BotanyPotsMod.LOG.error("Drop provider {} does not exist!", id);
                throw new IllegalStateException("Drop provider " + id + " does not exist.");
            }
            return REGISTRY.get(id).stream.decode(buf);
        }
    };
    public static final Codec<List<ItemDropProvider>> LIST_CODEC = MapCodecs.flexibleList(DROP_PROVIDER_CODEC);

    public static Supplier<ItemDropProviderType<?>> getLazy(Identifier id) {
        return CachedSupplier.cache(() -> get(id));
    }

    public static ItemDropProviderType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    /**
     * Creates and registers a drop provider type.
     *
     * @param id     The ID for the type.
     * @param codec  The codec for the type.
     * @param stream The stream codec for the type.
     * @param <T>    The provider represented by the type.
     * @return The newly created and registered type.
     */
    public static <T extends ItemDropProvider> ItemDropProviderType<T> register(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {
        final ItemDropProviderType<T> type = new ItemDropProviderType<>(id, codec, stream);
        if (REGISTRY.containsKey(id)) {
            BotanyPotsMod.LOG.warn("Item drop provider {} has already been assigned to {}. Replacing it with {}.", id, REGISTRY.get(id), type);
        }
        REGISTRY.put(id, type);
        return type;
    }
}