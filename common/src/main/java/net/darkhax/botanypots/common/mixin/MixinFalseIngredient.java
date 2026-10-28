package net.darkhax.botanypots.common.mixin;

import net.darkhax.bookshelf.common.impl.data.ingredient.FalseIngredient;
import net.darkhax.botanypots.common.impl.Helpers;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bookshelf 26.1.2.15 creates FalseIngredient.INSTANCE with only the Fabric type key, which fails to decode on
 * NeoForge. It is used whenever an empty ingredient is received over the network, for example by the either
 * ingredient when one of its tags is empty. This replaces it with a version that works on both loaders.
 */
@Mixin(value = FalseIngredient.class, remap = false)
public class MixinFalseIngredient {

    @Inject(method = "lambda$static$0", at = @At("HEAD"), cancellable = true, require = 0)
    private static void botanypots$createInstance(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Helpers.FALSE_INGREDIENT.get());
    }
}
