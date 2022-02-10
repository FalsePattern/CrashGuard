package com.falsepattern.crashguard.gui;

import com.falsepattern.crashguard.CrashGuard;
import com.falsepattern.crashguard.CrashHandler;
import com.falsepattern.crashguard.util.ModIdentifier;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.Util;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiCrash extends GuiScreen {
    private final boolean soft;
    private final boolean server;
    private final CrashReport crashReport;
    private final File crashReportFile;
    private final List<GuiText> texts = new ArrayList<>();
    private final Set<ModContainer> suspects;
    public GuiCrash(boolean soft, boolean server, CrashReport crashReport, File crashReportFile) {
        this.soft = soft;
        this.server = server;
        this.crashReport = crashReport;
        this.crashReportFile = crashReportFile;
        this.suspects = ModIdentifier.identifyFromStacktrace(crashReport.getCrashCause());
    }

    public void initGui() {
        this.buttonList.clear();
        texts.clear();

        int centerX = this.width / 2;
        int bottomY = Math.min((this.height / 10) * 9, this.height - 24);
        int y = Math.max(this.height / 4 - 40, 20);
        texts.add(new GuiText(true, (server ? "SERVER " : "CLIENT ") + "CRASHED!", centerX, y, 0xFF0000));
        y += 18;
        texts.add(new GuiText(true, "Description:", centerX, y, 0xFFFFFF));
        y += 15;
        texts.add(new GuiText(true, crashReport.getDescription(), centerX, y, 0xFFFF00));
        y += 18;
        if (suspects.size() != 0) {
            texts.add(new GuiText(true, "Suspected mod" + (suspects.size() > 1 ? "s:" : ":"), centerX, y, 0xFFFFFF));
            y += 18;
            if (CrashGuard.crashHint != null && CrashGuard.crashHint.length > 0) {
                for (val line: CrashGuard.crashHint) {
                    texts.add(new GuiText(true, line, centerX, y, 0xFFFF00));
                    y += 10;
                }
            }
            for (val suspect : suspects) {
                texts.add(new GuiText(true, suspect.getModId() + "@" + suspect.getVersion() + " (" + suspect.getName() + ")", centerX, y, 0xFFFF00));
                y += 10;
            }
        } else if (CrashGuard.crashHint != null && CrashGuard.crashHint.length > 0) {
            for (val line: CrashGuard.crashHint) {
                texts.add(new GuiText(true, line, centerX, y, 0xFFFF00));
                y += 10;
            }
        } else {
            texts.add(new GuiText(true, "Unknown cause. Check the crash report for any information.", centerX, y, 0xFFFF00));
        }

        val crashReport = new GuiButton(2, centerX - 100, bottomY, 98, 20, "Crash Report");
        val quit = new GuiButton(3, centerX + 2, bottomY, 98, 20, I18n.format("menu.quit"));
        bottomY -= 24;
        val toMenu = new GuiButton(1, centerX - 100, bottomY, I18n.format("gui.toMenu"));
        bottomY -= 24;
        val continuePlaying = new GuiButton(0, centerX - 100, bottomY, "Continue Playing");
        if (!soft) continuePlaying.enabled = false;
        if (crashReportFile == null) crashReport.enabled = false;
        buttonList.add(continuePlaying);
        buttonList.add(toMenu);
        buttonList.add(crashReport);
        buttonList.add(quit);
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                this.mc.displayGuiScreen(null);
                return;
            }
            case 1: {
                this.mc.displayGuiScreen(new GuiMainMenu());
                return;
            }
            case 2: {
                String s = crashReportFile.getAbsolutePath();

                if (Util.getOSType() == Util.EnumOS.OSX)
                {
                    try {
                        Runtime.getRuntime().exec(new String[] {"/usr/bin/open", s});
                        return;
                    } catch (IOException ignored) {
                    }
                }
                else if (Util.getOSType() == Util.EnumOS.WINDOWS)
                {
                    String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", s);

                    try {
                        Runtime.getRuntime().exec(s1);
                        return;
                    } catch (IOException ignored) {
                    }
                }

                boolean flag = false;

                try {
                    Class<?> oClass = Class.forName("java.awt.Desktop");
                    Object object = oClass.getMethod("getDesktop", new Class[0]).invoke(null);
                    oClass.getMethod("browse", new Class[] {URI.class}).invoke(object, crashReportFile.toURI());
                } catch (Throwable throwable) {
                    flag = true;
                }

                if (flag) {
                    Sys.openURL("file://" + s);
                }
                return;
            }
            case 3: {
                this.mc.shutdown();
                return;
            }
        }

    }

    protected void keyTyped(char p_73869_1_, int p_73869_2_) {
    }

    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        for (val text: texts) {
            text.draw(this, fontRendererObj);
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
