package dev.draconic.voidsea.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.draconic.voidsea.client.SeaCompatRenderer;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "foundry.veil.impl.client.render.shader.program.ShaderProgramImpl$Wrapper", remap = false)
public abstract class VeilShaderWrapperMixin {
    @Shadow @Final private ShaderProgram program;

    @WrapMethod(method = "apply")
    private void voidsea$keepFramebuffer(Operation<Void> original) {
        // Match Veil's original apply, avoiding its Iris framebuffer redirection in our passes.
        if (SeaCompatRenderer.isolatedPass()) {
            program.bind();
            program.bindSamplers(0);
        } else {
            original.call();
        }
    }

    @WrapMethod(method = "clear")
    private void voidsea$keepFramebufferOnClear(Operation<Void> original) {
        if (SeaCompatRenderer.isolatedPass()) ShaderProgram.unbind(); else original.call();
    }
}
