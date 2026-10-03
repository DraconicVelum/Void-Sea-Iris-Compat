package dev.draconic.voidsea.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import dev.simulated_team.simulated.content.end_sea.EndSeaRenderer;
import dev.simulated_team.simulated.content.end_sea.EndSeaShadowRenderer;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class SeaCompatRenderer {
    private static final ResourceLocation SEA_SHADER = ResourceLocation.fromNamespaceAndPath("simulated", "end_sea");
    private static PendingDraw pending;
    private static int shadowDepth;
    private static boolean replaying;

    private SeaCompatRenderer() { }

    public static boolean shadersEnabled() {
        return !Boolean.getBoolean("voidsea.disableCompat") && IrisApi.getInstance().isShaderPackInUse();
    }

    public static boolean isolatedPass() {
        return shadowDepth > 0 || replaying;
    }

    public static void enterShadowPass() { shadowDepth++; }
    public static void exitShadowPass() { shadowDepth--; }
    public static void clearPending() { pending = null; }

    public static boolean defer(Camera camera, GameRenderer renderer) {
        if (replaying || !shadersEnabled()) return false;
        if (IrisApi.getInstance().isRenderingShadowPass() || isolatedPass()
                || VeilLevelPerspectiveRenderer.isRenderingPerspective()) return true;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || EndSeaPhysicsData.of(minecraft.level) == null) return true;
        pending = new PendingDraw(camera, renderer, new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()), RenderSystem.getVertexSorting());
        return true;
    }

    public static void renderAfterIris() {
        PendingDraw draw = pending;
        pending = null;
        if (draw == null || !shadersEnabled() || IrisApi.getInstance().isRenderingShadowPass()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || EndSeaPhysicsData.of(minecraft.level) == null
                || EndSeaShadowRenderer.getShadowsFramebuffer() == null
                || VeilRenderSystem.renderer().getShaderManager().getShader(SEA_SHADER) == null) return;

        try (SeaRenderState ignored = new SeaRenderState()) {
            replaying = true;
            minecraft.getMainRenderTarget().bindWrite(true);
            RenderSystem.setProjectionMatrix(draw.projection(), draw.sorting());
            RenderSystem.getModelViewStack().set(draw.modelView());
            RenderSystem.applyModelViewMatrix();
            EndSeaRenderer.render(draw.camera(), draw.renderer());
        } finally {
            replaying = false;
        }
    }

    private record PendingDraw(Camera camera, GameRenderer renderer, Matrix4f modelView,
                               Matrix4f projection, VertexSorting sorting) { }
}
