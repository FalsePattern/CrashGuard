package com.falsepattern.crashguard;

import com.falsepattern.crashguard.gui.GuiCrash;
import com.falsepattern.crashguard.util.GlUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CrashHandler {
    public static final List<Class<? extends TileEntitySpecialRenderer>> bannedTESRs = new ArrayList<>();

    public static void attemptRecoveryFromCrash(boolean serverCrash, Throwable t) {
        freeMemory();
        CrashGuard.LOG.fatal("Unreported exception thrown!", t);
        val crashReport = new CrashReport("Unexpected error", t);
        internalCrashHandler(serverCrash, crashReport);
    }
    public static void attemptRecoveryFromCrash(boolean serverCrash, ReportedException reportedException) {
        freeMemory();
        CrashGuard.LOG.fatal("Reported exception thrown!", reportedException);
        internalCrashHandler(serverCrash, reportedException.getCrashReport());
    }

    private static void freeMemory() {
        if (!CrashGuard.softCrash) Minecraft.getMinecraft().freeMemory();
    }

    private static void internalCrashHandler(boolean serverCrash, CrashReport crashReport) {
        GlUtil.resetState();
        Minecraft.getMinecraft().displayGuiScreen(new GuiCrash(CrashGuard.softCrash, serverCrash, crashReport, logCrash(crashReport)));
        CrashGuard.softCrash = false;
    }

    private static File logCrash(CrashReport crashReport)
    {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "crash-reports");
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
