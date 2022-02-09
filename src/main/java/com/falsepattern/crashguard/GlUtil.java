package com.falsepattern.crashguard;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class GlUtil {
    private static final FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(4);
    private static FloatBuffer setColorBuffer(float a, float r, float g, float b) {
        colorBuffer.put(0, a);
        colorBuffer.put(1, r);
        colorBuffer.put(2, g);
        colorBuffer.put(3, b);
        return colorBuffer;
    }

    private static void popAll(Runnable popMethod, int stackSize) {
        try {
            for (int i = 0; i < stackSize && GL11.glGetError() == 0; i++) {
                popMethod.run();
            }
        } catch (Throwable ignored) {}
    }

    private static int modelViewStackDepthDefault;
    private static int projectionStackDepthDefault;
    private static int textureStackDepthDefault;
    private static int colorMatrixStackDepthDefault;
    private static int nameStackDepthDefault;
    private static int attribStackSizeDefault;
    private static int clientAttribStackSizeDefault;

    public static void initialize() {
        modelViewStackDepthDefault = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        projectionStackDepthDefault = GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH);
        textureStackDepthDefault = GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH);
        colorMatrixStackDepthDefault = GL11.glGetInteger(ARBImaging.GL_COLOR_MATRIX_STACK_DEPTH);
        nameStackDepthDefault = GL11.glGetInteger(GL11.GL_NAME_STACK_DEPTH);
        attribStackSizeDefault = GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
        clientAttribStackSizeDefault = GL11.glGetInteger(GL11.GL_CLIENT_ATTRIB_STACK_DEPTH);
    }
    public static void resetState() {

        int modelViewStackDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        int projectionStackDepth = GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH);
        int textureStackDepth = GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH);
        int colorMatrixStackDepth = GL11.glGetInteger(ARBImaging.GL_COLOR_MATRIX_STACK_DEPTH);
        int nameStackDepth = GL11.glGetInteger(GL11.GL_NAME_STACK_DEPTH);
        int attribStackSize = GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
        int clientAttribStackSize = GL11.glGetInteger(GL11.GL_CLIENT_ATTRIB_STACK_DEPTH);

        // Clear matrix stack
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        popAll(GL11::glPopMatrix, modelViewStackDepth - modelViewStackDepthDefault);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        popAll(GL11::glPopMatrix, projectionStackDepth - projectionStackDepthDefault);
        GL11.glMatrixMode(GL11.GL_COLOR);
        popAll(GL11::glPopMatrix, colorMatrixStackDepth - colorMatrixStackDepthDefault);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        popAll(GL11::glPopMatrix, textureStackDepth - textureStackDepthDefault);
        popAll(GL11::glPopName, nameStackDepth - nameStackDepthDefault);

        // Clear attribute stacks
        popAll(GL11::glPopAttrib, attribStackSize - attribStackSizeDefault);
        popAll(GL11::glPopClientAttrib, clientAttribStackSize - clientAttribStackSizeDefault);

        // Reset texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Reset GL lighting
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, setColorBuffer(0.2F, 0.2F, 0.2F, 1.0F));
        for (int i = 0; i < 8; ++i) {
            GL11.glDisable(GL11.GL_LIGHT0 + i);
            GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, setColorBuffer(0.0F, 0.0F, 1.0F, 0.0F));

            if (i == 0) {
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
            } else {
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            }
        }
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        // Reset depth
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glDepthMask(true);

        // Reset blend mode
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
        OpenGlHelper.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        // Reset fog
        GL11.glDisable(GL11.GL_FOG);
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
        GL11.glFogf(GL11.GL_FOG_DENSITY, 1.0F);
        GL11.glFogf(GL11.GL_FOG_START, 0.0F);
        GL11.glFogf(GL11.GL_FOG_END, 0.0F);
        GL11.glFog(GL11.GL_FOG_COLOR, setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        if (GLContext.getCapabilities().GL_NV_fog_distance) GL11.glFogi(GL11.GL_FOG_MODE, NVFogDistance.GL_EYE_PLANE_ABSOLUTE_NV);

        // Reset polygon offset
        GL11.glPolygonOffset(0.0F, 0.0F);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        // Reset color logic
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_COPY);
        
        // Disable lightmap
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Reset texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_RGB, GL13.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_RGB, GL13.GL_CONSTANT);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_ALPHA, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_ALPHA, GL13.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_ALPHA, GL13.GL_CONSTANT);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1.0F);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1.0F);

        GL11.glDisable(GL11.GL_NORMALIZE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glColorMask(true, true, true, true);
        GL11.glClearDepth(1.0D);
        GL11.glLineWidth(1.0F);
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}
