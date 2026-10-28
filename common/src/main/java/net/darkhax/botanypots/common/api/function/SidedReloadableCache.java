package net.darkhax.botanypots.common.api.function;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A reloadable cache that holds separate values for the client and server.
 *
 * @param <T> The type of the cached value.
 */
public class SidedReloadableCache<T> implements Function<Level, T> {

    private final ReloadableCache<T> client;
    private final ReloadableCache<T> server;

    public SidedReloadableCache(ReloadableCache<T> client, ReloadableCache<T> server) {
        this.client = client;
        this.server = server;
    }

    public ReloadableCache<T> getCache(Level level) {
        return level.isClientSide() ? client : server;
    }

    @Nullable
    @Override
    public T apply(Level level) {
        return getCache(level).apply(level);
    }

    public void invalidate(Level level) {
        getCache(level).invalidate();
    }

    public boolean isCached(Level level) {
        return getCache(level).isCached();
    }

    public void apply(Level level, Consumer<T> consumer) {
        getCache(level).apply(level, consumer);
    }

    public void ifPresent(Level level, Consumer<T> consumer) {
        getCache(level).ifPresent(level, consumer);
    }

    @Nullable
    public <R> R map(Level level, Function<T, R> mapper) {
        return getCache(level).map(level, mapper);
    }

    public static <T> SidedReloadableCache<T> of(Function<Level, T> cacheFunc) {
        return new SidedReloadableCache<>(ReloadableCache.of(cacheFunc), ReloadableCache.of(cacheFunc));
    }

    public static <T extends Recipe<?>> SidedReloadableCache<Map<Identifier, RecipeHolder<T>>> recipes(Supplier<RecipeType<T>> type) {
        return of(level -> ReloadableCache.collectRecipes(level, type.get()));
    }
}
