package net.darkhax.botanypots.common.impl;

import net.darkhax.bookshelf.common.api.function.CachedSupplier;
import net.darkhax.botanypots.common.impl.config.Config;
import net.darkhax.pricklemc.common.api.config.ConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class BotanyPotsMod {

    public static final String MOD_ID = "botanypots";
    public static final String MOD_NAME = "BotanyPots";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final Identifier BUILTIN_COMPONENT_ID = id("builtin_component_override");
    public static final ResourceKey<Recipe<?>> BUILTIN_COMPONENT_KEY = ResourceKey.create(Registries.RECIPE, BUILTIN_COMPONENT_ID);
    public static final CachedSupplier<Config> CONFIG = CachedSupplier.cache(() -> ConfigManager.load(MOD_ID, new Config()));
    public static WeakReference<MinecraftServer> SERVER = new WeakReference<>(null);

    public static Identifier id(String path) {
        return Identifier.tryBuild(MOD_ID, path);
    }

    public static void init() {
        CONFIG.get(); // Force init config file on main thread.
    }

    public static void updateServer(MinecraftServer server) {
        SERVER = new WeakReference<>(server);
    }

    public static MinecraftServer server() {
        return Objects.requireNonNull(SERVER.get(), "The server is not available.");
    }
}