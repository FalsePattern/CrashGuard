package com.falsepattern.crashguard.mixin.mixins.client;

import com.falsepattern.crashguard.CrashGuard;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin {
    @Shadow private boolean isDrawing;

    @Shadow protected abstract void reset();

    @Inject(method = "draw",
            at = @At(value = "HEAD"),
            require = 1)
    private void softNotTessellating(CallbackInfoReturnable<Integer> cir) {
        if (!isDrawing) {
            CrashGuard.softCrash = true;
            CrashGuard.crashHint = new String[]{"Tessellator error", "Not tesselating!"};
            reset();
            throw new IllegalStateException("Not tesselating!");
        }
    }

    @Inject(method = "startDrawing",
            at = @At(value = "HEAD"),
            require = 1)
    private void softAlreadyTessellating(int p_78371_1_, CallbackInfo ci) {
        if (isDrawing) {
            CrashGuard.softCrash = true;
            CrashGuard.crashHint = new String[]{"Tessellator error", "Already Tesselating!"};
            isDrawing = false;
            reset();
            throw new IllegalStateException("Already tesselating!");
        }
    }
}
