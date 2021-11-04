package vice.customskyboxes.mixin.skybox;

import vice.customskyboxes.SkyboxManager;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(FogRenderer.class)
public class SunSkyColorMixin
{

   @ModifyConstant(
           method = "setupColor",
           //slice = @Slice(from = @At(value = "INVOKE", target = "Linet/minecraft/util/CubicSampler;gaussianSampleVec3()")),
           constant = @Constant(intValue = 4, ordinal = 0)
   )
   private static int renderSkyColor(int original)
   {
       if (SkyboxManager.renderSunriseAndSet)
           return original;
       else
       {
           SkyboxManager.renderSunriseAndSet = true;
           return Integer.MAX_VALUE;
       }
   }
}

