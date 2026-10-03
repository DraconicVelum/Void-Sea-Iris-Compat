package dev.draconic.voidsea.mixin;

import dev.draconic.voidsea.client.SeaCompatRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IrisRenderingPipeline.class, remap = false)
public abstract class IrisRenderingPipelineMixin {
    @Inject(method = "beginLevelRendering", at = @At("HEAD"))
    private void voidsea$begin(CallbackInfo ci) {
        SeaCompatRenderer.clearPending();
    }

    @Inject(method = "finalizeLevelRendering", at = @At("TAIL"))
    private void voidsea$renderSea(CallbackInfo ci) {
        SeaCompatRenderer.renderAfterIris();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void voidsea$reload(CallbackInfo ci) {
        SeaCompatRenderer.clearPending();
    }

    @Inject(method = "shouldOverrideShaders", at = @At("HEAD"), cancellable = true)
    private void voidsea$vanillaOffscreenShader(CallbackInfoReturnable<Boolean> cir) {
        if (SeaCompatRenderer.isolatedPass()) cir.setReturnValue(false);
    }
}
