package vice.customskyboxes.mixin.skybox;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.WorldRenderer;
import vice.customskyboxes.SkyboxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class SkyboxRenderMixin {
    /**
     * Contains the logic for when skyboxes should be rendered.
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(MatrixStack matrices, float tickDelta, CallbackInfo ci) {

        float total = SkyboxManager.getInstance().getTotalAlpha();
        SkyboxManager.getInstance().renderSkyboxes((WorldRendererAccess) this, matrices, tickDelta);
        if (total > SkyboxManager.MINIMUM_ALPHA) {
            ci.cancel();
        }
    }
}
