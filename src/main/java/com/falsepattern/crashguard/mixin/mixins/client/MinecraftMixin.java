package com.falsepattern.crashguard.mixin.mixins.client;

import com.falsepattern.crashguard.CrashGuard;
import com.falsepattern.crashguard.CrashHandler;
import com.falsepattern.crashguard.util.GlUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@SideOnly(Side.CLIENT)
public abstract class MinecraftMixin {

    @Shadow(aliases = "func_71411_J") private void runGameLoop(){}

    @Shadow public abstract void freeMemory();

    @Shadow public abstract void displayGuiScreen(GuiScreen p_147108_1_);

    @Shadow volatile boolean running;

    @Shadow private boolean hasCrashed;

    @Shadow private CrashReport crashReporter;
    @Shadow private long field_83002_am;

    @Redirect(method = "run",
              at = @At(value = "FIELD",
                       opcode = Opcodes.GETFIELD,
                       target = "Lnet/minecraft/client/Minecraft;running:Z",
                       ordinal = 0),
              require = 1)
    private boolean customLoop(Minecraft ignored) {
        GlUtil.initialize();
        while (this.running) {
            if (!this.hasCrashed || this.crashReporter == null) {
                try {
                    CrashGuard.softCrash = false;
                    CrashGuard.crashHint = null;
                    runGameLoop();
                } catch (OutOfMemoryError outOfMemoryError) {
                    this.freeMemory();
                    this.displayGuiScreen(new GuiMemoryErrorScreen());
                    System.gc();
                } catch (ReportedException reportedException) {
                    CrashHandler.attemptRecoveryFromCrash(false, reportedException);
                } catch (Throwable t) {
                    CrashHandler.attemptRecoveryFromCrash(false, t);
                }
            } else {
                CrashHandler.attemptRecoveryFromCrash(true, new ReportedException(crashReporter));
                hasCrashed = false;
                crashReporter = null;
            }
        }
        return false;
    }

    @ModifyConstant(method = "runTick",
                    constant = @Constant(stringValue = "Manually triggered debug crash"),
                    require = 1)
    private String manualCrashCorrection(String str) {
        field_83002_am = -1L;
        return str;
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;)V",
            at = @At(value = "HEAD"),
            require = 1)
    private void resetBanList(WorldClient world, CallbackInfo ci) {
        if (world == null) CrashHandler.bannedTESRs.clear();
    }
}
