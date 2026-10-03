package dev.draconic.voidsea.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.draconic.voidsea.client.SeaCompatRenderer;
import dev.simulated_team.simulated.content.end_sea.EndSeaRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EndSeaRenderer.class, remap = false)
public abstract class EndSeaRendererMixin {
    @WrapMethod(method = "render")
    private static void voidsea$defer(Camera camera, GameRenderer renderer, Operation<Void> original) {
        if (!SeaCompatRenderer.defer(camera, renderer)) original.call(camera, renderer);
    }
}
