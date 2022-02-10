package com.falsepattern.crashguard.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

@SideOnly(Side.CLIENT)
public class GuiText {
    private final boolean centered;
    private final String text;
    private final int x;
    private final int y;
    private final int color;
    public GuiText(boolean centered, String text, int x, int y, int color) {
        this.centered = centered;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    void draw(Gui gui, FontRenderer font) {
        if (centered) {
            gui.drawCenteredString(font, text, x, y, color);
        } else {
            gui.drawString(font, text, x, y, color);
        }
    }
}
