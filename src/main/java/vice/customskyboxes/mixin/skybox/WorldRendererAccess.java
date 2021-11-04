package vice.customskyboxes.mixin.skybox;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccess {
    @Accessor
    TextureManager getTextureManager();

    @Accessor
    VertexFormat getSkyFormat();

    @Accessor
    VertexBuffer getSkyBuffer();

    @Accessor
    VertexBuffer getStarBuffer();

    @Accessor
    VertexBuffer getDarkBuffer();

    @Deprecated
    @Accessor("SUN_LOCATION")
    static ResourceLocation getSun() {
        throw new AssertionError();
    }

    @Deprecated
    @Accessor("MOON_LOCATION")
    static ResourceLocation getMoonPhases(){
        throw new AssertionError();
    }
}
