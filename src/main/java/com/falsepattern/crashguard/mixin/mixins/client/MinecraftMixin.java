package com.falsepattern.crashguard.mixin.mixins.client;

import com.falsepattern.crashguard.GlUtil;
import com.falsepattern.crashguard.GuiCrash;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow(aliases = "func_71411_J") private void runGameLoop(){}

    @Shadow public abstract void freeMemory();

    @Shadow public abstract void displayGuiScreen(GuiScreen p_147108_1_);

    @Shadow public abstract CrashReport addGraphicsAndWorldToCrashReport(CrashReport p_71396_1_);

    @Shadow @Final private static Logger logger;

    @Shadow @Final public File mcDataDir;

    @Shadow public WorldClient theWorld;

    @Shadow public abstract void loadWorld(WorldClient p_71403_1_);

    @Shadow volatile boolean running;

    @Shadow private boolean hasCrashed;

    @Shadow private CrashReport crashReporter;

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
                    runGameLoop();
                } catch (OutOfMemoryError outOfMemoryError) {
                    this.freeMemory();
                    this.displayGuiScreen(new GuiMemoryErrorScreen());
                    System.gc();
                } catch (MinecraftError e) {
                    throw e;
                } catch (ReportedException reportedexception) {
                    this.freeMemory();
                    this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
                    logger.fatal("Reported exception thrown!", reportedexception);
                    attemptRecoveryFromCrash(false, reportedexception.getCrashReport());
                } catch (Throwable throwable1) {
                    this.freeMemory();
                    val crashReport = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
                    logger.fatal("Unreported exception thrown!", throwable1);
                    attemptRecoveryFromCrash(false, crashReport);
                }
            } else {
                this.freeMemory();
                attemptRecoveryFromCrash(true, crashReporter);
                hasCrashed = false;
                crashReporter = null;
            }
        }
        return false;
    }

    private void attemptRecoveryFromCrash(boolean serverCrash, CrashReport crashReport) {
        GlUtil.resetState();
        displayGuiScreen(new GuiCrash(serverCrash, crashReport, logCrash(crashReport)));
    }

    private File logCrash(CrashReport crashReport)
    {
        File file1 = new File(this.mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        System.out.println(crashReport.getCompleteReport());

        if (crashReport.getFile() != null) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getFile());
            return crashReport.getFile();
        } else if (crashReport.saveToFile(file2)) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            return file2;
        } else {
            System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            return null;
        }
    }
}
