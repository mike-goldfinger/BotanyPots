package net.darkhax.botanypots.common.impl;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonObject;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.darkhax.bookshelf.common.api.data.codecs.stream.StreamCodecs;
import net.darkhax.bookshelf.common.api.data.enchantment.EnchantmentLevel;
import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.bookshelf.common.api.util.DataHelper;
import net.darkhax.bookshelf.common.api.util.MathsHelper;
import net.darkhax.botanypots.common.api.context.BlockEntityContext;
import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class Helpers {

    public static final ResourceKey<LootTable> EMPTY_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("empty"));
    public static final Codec<Vector3f> VECTOR3F_CODEC = ExtraCodecs.VECTOR3F.xmap(Vector3f::new, v -> v);
    public static final StreamCodec<ByteBuf, Vector3f> VECTOR3F_STREAM = ByteBufCodecs.VECTOR3F.map(Vector3f::new, v -> v);
    public static final Codec<Block> BLOCK_CODEC = Identifier.CODEC.xmap(BuiltInRegistries.BLOCK::getValue, BuiltInRegistries.BLOCK::getKey);
    public static final StreamCodec<ByteBuf, Block> BLOCK_STREAM = Identifier.STREAM_CODEC.map(BuiltInRegistries.BLOCK::getValue, BuiltInRegistries.BLOCK::getKey);
    public static final Codec<Item> ITEM_CODEC = Identifier.CODEC.xmap(BuiltInRegistries.ITEM::getValue, BuiltInRegistries.ITEM::getKey);
    public static final StreamCodec<ByteBuf, Item> ITEM_STREAM = Identifier.STREAM_CODEC.map(BuiltInRegistries.ITEM::getValue, BuiltInRegistries.ITEM::getKey);
    public static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = Identifier.CODEC.xmap(BuiltInRegistries.ENTITY_TYPE::getValue, BuiltInRegistries.ENTITY_TYPE::getKey);
    public static final StreamCodec<ByteBuf, EntityType<?>> ENTITY_TYPE_STREAM = Identifier.STREAM_CODEC.map(BuiltInRegistries.ENTITY_TYPE::getValue, BuiltInRegistries.ENTITY_TYPE::getKey);
    public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
    /**
     * An ingredient that never matches. Bookshelf provides FalseIngredient.INSTANCE, but it only carries the Fabric
     * type key and fails to decode on NeoForge. This version carries the type key for both loaders.
     */
    public static final Supplier<Ingredient> FALSE_INGREDIENT = CachedSupplier.cache(() -> {
        final JsonObject obj = new JsonObject();
        obj.addProperty("neoforge:ingredient_type", "bookshelf:false");
        obj.addProperty("fabric:type", "bookshelf:false");
        obj.addProperty("tag", "bookshelf:false");
        return Ingredient.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow().getFirst();
    });

    /**
     * Syncs ingredients that must not be empty. Ingredients that resolve to no items on the server, for example tags
     * of mods that are not installed, are replaced with an ingredient that never matches.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> INGREDIENT_NON_EMPTY = StreamCodec.of(
            Ingredient.CONTENTS_STREAM_CODEC,
            buf -> {
                final Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                return ingredient.isEmpty() ? FALSE_INGREDIENT.get() : ingredient;
            }
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> OPTIONAL_INGREDIENT_STREAM = DataHelper.optionalStream(INGREDIENT_NON_EMPTY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStackTemplate>> OPTIONAL_ITEMSTACK_TEMPLATE_STREAM = DataHelper.optionalStream(ItemStackTemplate.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStack>> OPTIONAL_ITEMSTACK_STREAM = DataHelper.optionalStream(ItemStack.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<BlockPredicate>> OPTIONAL_BLOCK_PREDICATE = DataHelper.optionalStream(BlockPredicate.STREAM_CODEC);
    public static final StreamCodec<ByteBuf, TagKey<Block>> BLOCK_TAG = Identifier.STREAM_CODEC.map(rl -> TagKey.create(Registries.BLOCK, rl), TagKey::location);
    public static final TagKey<Enchantment> INCREASE_POT_GROWTH_TAG = TagKey.create(Registries.ENCHANTMENT, BotanyPotsMod.id("increase_pot_growth"));
    public static final TagKey<Enchantment> NEGATE_HARVEST_DAMAGE_TAG = TagKey.create(Registries.ENCHANTMENT, BotanyPotsMod.id("negate_harvest_damage"));

    public static final Supplier<Holder<Attribute>> GROWTH_MOD_ATTRIBUTE = CachedSupplier.cache(() -> BuiltInRegistries.ATTRIBUTE.get(BotanyPotsMod.id("growth")).orElseThrow());
    public static final String GROWTH_MODIFIER_KEY = "tooltip.botanypots.growth_modifier";
    public static final Supplier<Holder<Attribute>> YIELD_MOD_ATTRIBUTE = CachedSupplier.cache(() -> BuiltInRegistries.ATTRIBUTE.get(BotanyPotsMod.id("yield")).orElseThrow());
    public static final String YIELD_MODIFIER_KEY = "tooltip.botanypots.yield_modifier";


    public static Component modifierComponent(String key, float modifier) {
        if (modifier > 0) {
            return Component.translatable(key, "+" + MathsHelper.DECIMAL_2.format(modifier * 100d) + "%").withStyle(ChatFormatting.BLUE);
        }
        return Component.translatable(key, "-" + MathsHelper.DECIMAL_2.format(modifier * 100d) + "%").withStyle(ChatFormatting.RED);
    }

    public static Component indent(Component base) {
        return Component.literal("  ").append(base);
    }

    public static int getRequiredGrowthTicks(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        final float cropTime = crop.getRequiredGrowthTicks(context, level);
        float growthModifier = BotanyPotsMod.CONFIG.get().gameplay.global_growth_modifier;
        growthModifier += soil != null ? soil.getGrowthModifier(context, level) : 0f;
        growthModifier += efficiencyModifier(level.registryAccess(), context.getHarvestItem());
        if (context instanceof BlockEntityContext beContext && beContext.pot().getBlockState().getBlock() instanceof BotanyPotBlock potBlock) {
            growthModifier += potBlock.getGrowthModifier(context, level, crop, soil);
        }
        return Mth.floor(cropTime / growthModifier);
    }

    public static float getTotalYield(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        final float yieldScale = crop.getYieldScale(context, level);
        float yield = crop.getBaseYield(context, level);
        if (context instanceof BlockEntityContext beContext && beContext.pot().getBlockState().getBlock() instanceof BotanyPotBlock potBlock) {
            yield += (yieldScale * potBlock.getYieldModifier(context, level, crop, soil));
        }
        if (soil != null) {
            yield += (yieldScale * soil.getYieldModifier(context, level));
        }
        yield += (yieldScale * (float) getAttributeValue(YIELD_MOD_ATTRIBUTE.get(), context.getHarvestItem(), 0f));
        return yield;
    }

    public static int getLootRolls(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        return determineRollCount(getTotalYield(context, level, crop, soil), level.getRandom());
    }

    /**
     * Calculates the amount of rolls that should take place. When chance is over 100% there will be a guaranteed roll
     * for each multiple of 100% plus an additional random chance using the remainder. The amount of rolls determined
     * will never be negative.
     * <p>
     * For example, if the chance is 550% the user will have 5 guaranteed rolls and a 50% chance for a sixth roll.
     *
     * @param chance The chance for rolls to happen.
     * @param rng    An RNG source used to calculate percentages.
     * @return The amount of rolls that was determined. Can never be negative.
     */
    public static int determineRollCount(float chance, RandomSource rng) {
        int guaranteedRolls = (int) Math.floor(chance);
        final float remainingChance = chance % 1f;
        if (rng.nextFloat() < remainingChance) {
            guaranteedRolls++;
        }
        return Math.max(0, guaranteedRolls);
    }

    public static int chanceToInt(float chance) {
        return (int) Math.floor(chance * 100f);
    }

    public static MutableComponent withScale(MutableComponent base, float scale) {
        if (scale != 1f) {
            base.append(" (").append(Component.translatable("tooltip.botanypots.percent", chanceToInt(scale)).withStyle(scale > 1f ? ChatFormatting.GREEN : ChatFormatting.RED)).append(")");
        }
        return base;
    }

    public static float efficiencyModifier(RegistryAccess registryAccess, ItemStack stack) {
        float modifier = 0f;
        modifier += EnchantmentLevel.HIGHEST.get(INCREASE_POT_GROWTH_TAG, stack) * BotanyPotsMod.CONFIG.get().gameplay.efficiency_growth_modifier;
        modifier += (float) getAttributeValue(GROWTH_MOD_ATTRIBUTE.get(), stack, 0f);
        return modifier;
    }

    public static double getAttributeValue(Holder<Attribute> attribute, ItemStack stack, float defaultValue) {
        double value = 0d;
        final ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute() == attribute) {
                final double entryAmount = entry.modifier().amount();
                value += switch (entry.modifier().operation()) {
                    case ADD_VALUE -> entryAmount;
                    case ADD_MULTIPLIED_BASE -> entryAmount * defaultValue;
                    case ADD_MULTIPLIED_TOTAL -> entryAmount * value;
                };
            }
        }
        return value;
    }

    public static boolean contains(String input, String... matches) {
        for (String match : matches) {
            if (input.contains(match)) {
                return true;
            }
        }
        return false;
    }


    public static ItemStack addModifier(ItemStack stack, Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup group) {
        ItemAttributeModifiers component = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        component = component.withModifierAdded(attribute, modifier, group);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, component);
        return stack;
    }
}