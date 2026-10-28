package net.darkhax.botanypots.common.api.function;

import net.darkhax.botanypots.common.impl.data.SyncedRecipes;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A cached value that is lazily loaded and will be invalidated automatically after the game has been reloaded. This
 * was previously provided by Bookshelf, which removed it in the 26.1 port.
 *
 * @param <T> The type of the cached value.
 */
public class ReloadableCache<T> implements Function<Level, T> {

    /**
     * Incremented every time the server reloads its recipes.
     */
    private static int serverRevision = 0;

    /**
     * Incremented every time the client receives synchronized recipes.
     */
    private static int clientRevision = 0;

    /**
     * A reloadable cache that will always return null. Not all empty instances will match this instance.
     */
    @SuppressWarnings("rawtypes")
    public static final ReloadableCache EMPTY = ReloadableCache.of(level -> null);

    private final Function<Level, T> delegate;
    private boolean cached = false;
    private int revision = -1;

    @Nullable
    private T cachedValue;

    protected ReloadableCache(Function<Level, T> delegate) {
        this.delegate = delegate;
    }

    public static void markServerReload() {
        serverRevision++;
    }

    public static void markClientReload() {
        clientRevision++;
    }

    private static int revision(Level level) {
        return level.isClientSide() ? clientRevision : serverRevision;
    }

    @Nullable
    @Override
    public T apply(Level level) {
        final int currentRevision = revision(level);
        if (!this.isCached() || this.revision != currentRevision) {
            this.cachedValue = this.delegate.apply(level);
            this.revision = currentRevision;
            this.cached = true;
        }
        return this.cachedValue;
    }

    /**
     * Manually invalidates the cache. This will result in a new value being cached the next time {@link #apply(Level)}
     * is invoked.
     */
    public void invalidate() {
        this.cached = false;
        this.cachedValue = null;
        this.revision = -1;
    }

    /**
     * Checks if the cache has cached a value. This is not a substitute for null checking.
     *
     * @return Has the supplier cached a value.
     */
    public boolean isCached() {
        return this.cached;
    }

    /**
     * Invokes the consumer with the cached value. This will cause a value to be cached if one has not been cached
     * already.
     *
     * @param level    The current game level.
     * @param consumer The consumer to invoke.
     */
    public void apply(Level level, Consumer<T> consumer) {
        consumer.accept(this.apply(level));
    }

    /**
     * Applies a function to the cached value if the value is not null.
     *
     * @param level    The current game level.
     * @param consumer The consumer to invoke.
     */
    public void ifPresent(Level level, Consumer<T> consumer) {
        final T value = this.apply(level);
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Maps non null cache values to a new value.
     *
     * @param level  The current game level.
     * @param mapper A mapper function to map the cached value to something new.
     * @param <R>    The return type.
     * @return The mapped value or null.
     */
    @Nullable
    public <R> R map(Level level, Function<T, R> mapper) {
        final T value = this.apply(level);
        return value != null ? mapper.apply(value) : null;
    }

    public static <T> ReloadableCache<T> of(Supplier<T> supplier) {
        return new ReloadableCache<>(level -> supplier.get());
    }

    public static <T> ReloadableCache<T> of(Function<Level, T> delegate) {
        return new ReloadableCache<>(delegate);
    }

    public static <T> ReloadableCache<T> of(ResourceKey<? extends Registry<T>> registry, Identifier id) {
        return ReloadableCache.of(level -> level.registryAccess().lookupOrThrow(registry).getValue(id));
    }

    /**
     * Creates a cache of recipe entries for a recipe type. On the server this will read from the recipe manager, on
     * the client it will read from the recipes that were synchronized by the server.
     *
     * @param type The type of recipe.
     * @param <T>  The type of the recipe.
     * @return A map of recipes for the recipe type.
     */
    public static <T extends Recipe<?>> ReloadableCache<Map<Identifier, RecipeHolder<T>>> recipes(Supplier<RecipeType<T>> type) {
        return ReloadableCache.of(level -> collectRecipes(level, type.get()));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> Map<Identifier, RecipeHolder<T>> collectRecipes(Level level, RecipeType<T> type) {
        final Map<Identifier, RecipeHolder<T>> byId = new HashMap<>();
        if (level instanceof ServerLevel serverLevel) {
            for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
                if (holder.value().getType() == type) {
                    byId.put(holder.id().identifier(), (RecipeHolder<T>) holder);
                }
            }
        }
        else {
            for (RecipeHolder<T> holder : SyncedRecipes.byType(type)) {
                byId.put(holder.id().identifier(), holder);
            }
        }
        return byId;
    }

    /**
     * Creates a cache of an entity instance.
     *
     * @param entityData The data used to create the entity.
     * @return A reloadable entity instance.
     */
    public static ReloadableCache<Entity> entity(CompoundTag entityData) {
        if (entityData == null || entityData.getString("id").isEmpty()) {
            throw new IllegalStateException("The provided entity data does not contain an entity ID! data=" + entityData);
        }
        return ReloadableCache.of(level -> {
            try {
                return EntityType.loadEntityRecursive(entityData, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
            }
            catch (Exception e) {
                throw new IllegalStateException("Encountered an error while constructing the target entity.", e);
            }
        });
    }

    /**
     * Creates a cache of a living entity instance.
     *
     * @param entityData The data used to create the entity.
     * @return A reloadable living entity instance.
     */
    public static ReloadableCache<LivingEntity> living(CompoundTag entityData) {
        final ReloadableCache<Entity> entityCache = entity(entityData);
        return ReloadableCache.of(level -> {
            if (entityCache.apply(level) instanceof LivingEntity living) {
                return living;
            }
            throw new IllegalStateException("Constructed entity was not a LivingEntity type. data=" + entityData);
        });
    }
}
