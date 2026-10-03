package dev.draconic.voidsea.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

/** State changed by Simulated's sea draw and offscreen shadow pass. */
public final class SeaRenderState implements AutoCloseable {
    private final int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
    private final int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
    private final int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
    private final int vertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
    private final int[] viewport = new int[4];
    private final boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
    private final int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
    private final int destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
    private final int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
    private final int destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final int equationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
    private final int equationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int[] textures = new int[12];
    private final int[] samplers = new int[12];
    private final int[] shaderTextures = new int[12];
    private final float[] shaderColor = RenderSystem.getShaderColor().clone();
    private final ShaderInstance shader = RenderSystem.getShader();
    private final Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
    private final VertexSorting sorting = RenderSystem.getVertexSorting();
    private final Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());

    public SeaRenderState() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        for (int i = 0; i < textures.length; i++) {
            RenderSystem.activeTexture(GL13.GL_TEXTURE0 + i);
            textures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            samplers[i] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
            shaderTextures[i] = RenderSystem.getShaderTexture(i);
        }
        RenderSystem.activeTexture(activeTexture);
    }

    @Override
    public void close() {
        RenderSystem.setProjectionMatrix(projection, sorting);
        RenderSystem.getModelViewStack().set(modelView);
        RenderSystem.applyModelViewMatrix();
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
        RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        RenderSystem.depthMask(depthMask);
        RenderSystem.depthFunc(depthFunction);
        if (depthTest) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
        RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
        RenderSystem.blendEquation(equationRgb);
        if (equationAlpha != equationRgb) GL20.glBlendEquationSeparate(equationRgb, equationAlpha);
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
        for (int i = 0; i < textures.length; i++) {
            RenderSystem.activeTexture(GL13.GL_TEXTURE0 + i);
            RenderSystem.bindTexture(textures[i]);
            GL33.glBindSampler(i, samplers[i]);
            RenderSystem.setShaderTexture(i, shaderTextures[i]);
        }
        RenderSystem.activeTexture(activeTexture);
        RenderSystem.setShader(() -> shader);
        GlStateManager._glUseProgram(program);
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(vertexArray);
    }
}
