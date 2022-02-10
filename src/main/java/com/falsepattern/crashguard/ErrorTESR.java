package com.falsepattern.crashguard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ErrorTESR extends TileEntitySpecialRenderer {
    public static final ErrorTESR instance = new ErrorTESR();
    private static int renderList = 0;
    static {
        instance.func_147497_a(TileEntityRendererDispatcher.instance);
    }
    private static final ResourceLocation woeisme = new ResourceLocation(Tags.MODID, "textures/items/woeisme.png");
    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTickTime) {
        double tickTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime() + partialTickTime;
        tickTime *= 10;
        GL11.glPushMatrix();
        bindTexture(woeisme);
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GL11.glRotated(tickTime, 1, 0, 0);
        GL11.glRotated(tickTime / Math.E, 0, 1, 0);
        GL11.glRotated(tickTime / Math.PI, 0, 0, 1);
        if (renderList == 0) {
            renderList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(renderList, GL11.GL_COMPILE);
            GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POLYGON_BIT | GL11.GL_LIGHTING_BIT);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_LIGHTING);
            Tessellator.instance.startDrawingQuads();
            Tessellator.instance.disableColor();
            Tessellator.instance.addVertexWithUV(-0.5, 0.5, 0, 0, 0);
            Tessellator.instance.addVertexWithUV(-0.5, -0.5, 0, 0, 1);
            Tessellator.instance.addVertexWithUV(0.5, -0.5, 0, 1, 1);
            Tessellator.instance.addVertexWithUV(0.5, 0.5, 0, 1, 0);
            Tessellator.instance.addVertexWithUV(0.5, 0, -0.5, 0, 0);
            Tessellator.instance.draw();
            GL11.glPopAttrib();
            GL11.glEndList();
        }
        GL11.glCallList(renderList);
        GL11.glPopMatrix();
    }
}
