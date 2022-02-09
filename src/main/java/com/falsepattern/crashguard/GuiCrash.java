package com.falsepattern.crashguard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
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

@SideOnly(Side.CLIENT)
public class GuiCrash extends GuiScreen {
    private final boolean server;
    private final CrashReport crashReport;
    private final File crashReportFile;
    private final List<GuiText> texts = new ArrayList<>();
    public GuiCrash(boolean server, CrashReport crashReport, File crashReportFile) {
        this.server = server;
        this.crashReport = crashReport;
        this.crashReportFile = crashReportFile;
    }

    public void initGui() {
        this.buttonList.clear();
        texts.clear();

        int centerX = this.width / 2;
        int bottomY = (this.height / 4) * 3;
        int y = Math.max(this.height / 4 - 40, 20);
        texts.add(new GuiText(true, (server ? "SERVER " : "CLIENT ") + "CRASHED!", centerX, y, 0xFF0000));
        y += 18;
        texts.add(new GuiText(true, "Description:", centerX, y, 0xFFFFFF));
        y += 15;
        texts.add(new GuiText(true, crashReport.getDescription(), centerX, y, 0xFFFF00));
        y += 18;
        val suspects = ModIdentifier.identifyFromStacktrace(crashReport.getCrashCause());
        if (suspects.size() != 0) {
            texts.add(new GuiText(true, "Suspected mod" + (suspects.size() > 1 ? "s:" : ":"), centerX, y, 0xFFFFFF));
            y += 18;
            for (val suspect : suspects) {
                texts.add(new GuiText(true, suspect.getModId() + "@" + suspect.getVersion() + " (" + suspect.getName() + ")", centerX, y, 0xFFFF00));
                y += 10;
            }
        } else {
            texts.add(new GuiText(true, "Unknown cause. Check the crash report for any information.", centerX, y, 0xFFFF00));
        }
        buttonList.add(new GuiButton(0, centerX - 100, bottomY, I18n.format("gui.toMenu")));
        bottomY += 24;
        {
            val button = new GuiButton(1, centerX - 100, bottomY, 98, 20, "Crash Report");
            if (crashReportFile == null) button.enabled = false;
            buttonList.add(button);
        }
        buttonList.add(new GuiButton(2, centerX + 2, bottomY, 98, 20, I18n.format("menu.quit")));
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiMainMenu());
        } else if (button.id == 1) {
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
        } else if (button.id == 2) {
            this.mc.shutdown();
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
}
