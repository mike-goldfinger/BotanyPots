package net.darkhax.botanypots.common.impl.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import net.darkhax.botanypots.common.api.function.ReloadableCache;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Since Minecraft 1.21.2 recipes are no longer sent to the client. Botany pot recipes are needed on the client for
 * rendering and JEI, so the loaders are asked to synchronize them. This class holds the received recipes.
 */
public final class SyncedRecipes {

    private static ListMultimap<RecipeType<?>, RecipeHolder<?>> recipes = ImmutableListMultimap.of();

    /**
     * Gets the recipe types that need to be synchronized to the client.
     *
     * @return The recipe types to sync.
     */
    public static Set<RecipeType<?>> syncedTypes() {
        return Set.of(
                net.darkhax.botanypots.common.api.data.recipes.soil.Soil.TYPE.get(),
                net.darkhax.botanypots.common.api.data.recipes.crop.Crop.TYPE.get(),
                net.darkhax.botanypots.common.api.data.recipes.interaction.PotInteraction.TYPE.get(),
                net.darkhax.botanypots.common.api.data.recipes.fertilizer.Fertilizer.TYPE.get()
        );
    }

    /**
     * Replaces the client recipes with a newly received set of recipes.
     *
     * @param received The received recipes. Recipes of unrelated types will be ignored.
     */
    public static void update(Collection<RecipeHolder<?>> received) {
        final Set<RecipeType<?>> types = syncedTypes();
        final ListMultimap<RecipeType<?>, RecipeHolder<?>> byType = ArrayListMultimap.create();
        for (RecipeHolder<?> holder : received) {
            final RecipeType<?> type = holder.value().getType();
            if (types.contains(type)) {
                byType.put(type, holder);
            }
        }
        recipes = ImmutableListMultimap.copyOf(byType);
        ReloadableCache.markClientReload();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> List<RecipeHolder<T>> byType(RecipeType<T> type) {
        return (List<RecipeHolder<T>>) (List<?>) recipes.get(type);
    }

    public static <T extends Recipe<?>> List<RecipeHolder<T>> byType(Supplier<RecipeType<T>> type) {
        return byType(type.get());
    }
}
