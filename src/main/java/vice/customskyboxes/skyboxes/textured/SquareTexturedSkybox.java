package vice.customskyboxes.skyboxes.textured;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import vice.customskyboxes.mixin.skybox.WorldRendererAccess;
import vice.customskyboxes.skyboxes.AbstractSkybox;
import vice.customskyboxes.skyboxes.SkyboxType;
import vice.customskyboxes.util.object.*;
import net.minecraft.client.renderer.texture.TextureManager;

public class SquareTexturedSkybox extends TexturedSkybox
{
    public static Codec<SquareTexturedSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DefaultProperties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getDefaultProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.NO_CONDITIONS).forGetter(AbstractSkybox::getConditions),
            Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations),
            Blend.CODEC.optionalFieldOf("blend", Blend.DEFAULT).forGetter(TexturedSkybox::getBlend),
            Textures.CODEC.fieldOf("textures").forGetter(s -> s.textures)
    ).apply(instance, SquareTexturedSkybox::new));

    public Textures textures;

    public SquareTexturedSkybox() {
    }

    public SquareTexturedSkybox(DefaultProperties properties, Conditions conditions, Decorations decorations, Blend blend, Textures textures) {
        super(properties, conditions, decorations, blend);
        this.textures = textures;
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.SQUARE_TEXTURED_SKYBOX;
    }

    @Override
    public void renderSkybox(WorldRendererAccess worldRendererAccess, MatrixStack matrices, float tickDelta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        TextureManager textureManager = worldRendererAccess.getTextureManager();

        for (int i = 0; i < 6; ++i) {
            // 0 = bottom
            // 1 = north
            // 2 = south
            // 3 = top
            // 4 = east
            // 5 = west
            Texture tex = this.textures.byId(i);
            matrices.pushPose();

            textureManager.bind(tex.getTextureId());

            if (i == 1) {
                matrices.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            } else if (i == 2) {
                matrices.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                matrices.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            } else if (i == 3) {
                matrices.mulPose(Vector3f.XP.rotationDegrees(180.0F));
                matrices.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            } else if (i == 4) {
                matrices.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                matrices.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            } else if (i == 5) {
                matrices.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
                matrices.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            }

            Matrix4f matrix4f = matrices.last().pose();
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(tex.getMinU(), tex.getMinV()).color(1f, 1f, 1f, alpha).endVertex();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(tex.getMinU(), tex.getMaxV()).color(1f, 1f, 1f, alpha).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(tex.getMaxU(), tex.getMaxV()).color(1f, 1f, 1f, alpha).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(tex.getMaxU(), tex.getMinV()).color(1f, 1f, 1f, alpha).endVertex();
            tessellator.end();
            matrices.popPose();
        }
    }
}
