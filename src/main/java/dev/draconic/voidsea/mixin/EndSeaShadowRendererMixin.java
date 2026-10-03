package dev.draconic.voidsea.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.draconic.voidsea.client.SeaCompatRenderer;
import dev.draconic.voidsea.client.SeaRenderState;
import dev.simulated_team.simulated.content.end_sea.EndSeaShadowRenderer;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EndSeaShadowRenderer.class, remap = false)
public abstract class EndSeaShadowRendererMixin {
    @WrapMethod(method = "renderShadowMap")
    private static void voidsea$isolate(VeilRenderLevelStageEvent.Stage stage, LevelRenderer renderer,
            MultiBufferSource.BufferSource buffers, MatrixStack matrices, Matrix4fc frustumMatrix,
            Matrix4fc projection, int tick, DeltaTracker delta, Camera camera, Frustum frustum,
            Operation<Void> original) {
        if (!SeaCompatRenderer.shadersEnabled()) {
            original.call(stage, renderer, buffers, matrices, frustumMatrix, projection, tick, delta, camera, frustum);
            return;
        }
        if (IrisApi.getInstance().isRenderingShadowPass()) return;
        if (stage != VeilRenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        try (SeaRenderState ignored = new SeaRenderState()) {
            SeaCompatRenderer.enterShadowPass();
            try {
                // Iris can leave native depth testing disabled while vanilla's cache says enabled.
                RenderSystem.enableDepthTest();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                RenderSystem.depthMask(true);
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                original.call(stage, renderer, buffers, matrices, frustumMatrix, projection, tick, delta, camera, frustum);
            } finally {
                SeaCompatRenderer.exitShadowPass();
            }
        }
    }
}
