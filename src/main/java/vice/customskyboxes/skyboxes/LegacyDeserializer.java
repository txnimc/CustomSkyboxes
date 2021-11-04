package vice.customskyboxes.skyboxes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import vice.customskyboxes.FabricSkyBoxesClient;
import vice.customskyboxes.skyboxes.textured.SquareTexturedSkybox;
import vice.customskyboxes.util.JsonObjectWrapper;
import vice.customskyboxes.util.object.*;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import java.util.function.BiConsumer;

public class LegacyDeserializer<T extends AbstractSkybox> extends ForgeRegistryEntry<LegacyDeserializer<? extends AbstractSkybox>>
{
    public static final IForgeRegistry<LegacyDeserializer<? extends AbstractSkybox>> REGISTRY =
            new RegistryBuilder<LegacyDeserializer<? extends AbstractSkybox>>()
                    .setName(new ResourceLocation(FabricSkyBoxesClient.MODID, "legacy_skybox_deserializer"))
                    .setType(c(LegacyDeserializer.class))
                    .create();

    public static final LegacyDeserializer<MonoColorSkybox> MONO_COLOR_SKYBOX_DESERIALIZER = register(new LegacyDeserializer<>(LegacyDeserializer::decodeMonoColor, MonoColorSkybox.class), "mono_color_skybox_legacy_deserializer");
    public static final LegacyDeserializer<SquareTexturedSkybox> SQUARE_TEXTURED_SKYBOX_DESERIALIZER = register(new LegacyDeserializer<>(LegacyDeserializer::decodeSquareTextured, SquareTexturedSkybox.class), "square_textured_skybox_legacy_deserializer");
    private final BiConsumer<JsonObjectWrapper, AbstractSkybox> deserializer;

    private LegacyDeserializer(BiConsumer<JsonObjectWrapper, AbstractSkybox> deserializer, Class<T> clazz) {
        this.deserializer = deserializer;
    }

    public BiConsumer<JsonObjectWrapper, AbstractSkybox> getDeserializer() {
        return this.deserializer;
    }

    private static void decodeSquareTextured(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        decodeSharedData(wrapper, skybox);
        ((SquareTexturedSkybox) skybox).rotation = new Rotation(new Vector3f(0f, 0f, 0f), new Vector3f(wrapper.getOptionalArrayFloat("axis", 0, 0), wrapper.getOptionalArrayFloat("axis", 1, 0), wrapper.getOptionalArrayFloat("axis", 2, 0)), 1);
        ((SquareTexturedSkybox) skybox).blend = new Blend(wrapper.getOptionalBoolean("shouldBlend", false) ? "add" : null, 0, 0, 0);
        ((SquareTexturedSkybox) skybox).textures = new Textures(
                new Texture(wrapper.getJsonStringAsId("texture_north")),
                new Texture(wrapper.getJsonStringAsId("texture_south")),
                new Texture(wrapper.getJsonStringAsId("texture_east")),
                new Texture(wrapper.getJsonStringAsId("texture_west")),
                new Texture(wrapper.getJsonStringAsId("texture_top")),
                new Texture(wrapper.getJsonStringAsId("texture_bottom"))
        );
    }

    private static void decodeMonoColor(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        decodeSharedData(wrapper, skybox);
        ((MonoColorSkybox) skybox).color = new RGBA(wrapper.get("red").getAsFloat(), wrapper.get("blue").getAsFloat(), wrapper.get("green").getAsFloat());
    }

    private static void decodeSharedData(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        skybox.fade = new Fade(
                wrapper.get("startFadeIn").getAsInt(),
                wrapper.get("endFadeIn").getAsInt(),
                wrapper.get("startFadeOut").getAsInt(),
                wrapper.get("endFadeOut").getAsInt(),
                false
        );
        // alpha changing
        skybox.maxAlpha = wrapper.getOptionalFloat("maxAlpha", 1f);
        skybox.transitionSpeed = wrapper.getOptionalFloat("transitionSpeed", 1f);
        // rotation
        skybox.shouldRotate = wrapper.getOptionalBoolean("shouldRotate", false);
        // decorations
        skybox.decorations = Decorations.DEFAULT;
        // fog
        skybox.changeFog = wrapper.getOptionalBoolean("changeFog", false);
        skybox.fogColors = new RGBA(
                wrapper.getOptionalFloat("fogRed", 0f),
                wrapper.getOptionalFloat("fogGreen", 0f),
                wrapper.getOptionalFloat("fogBlue", 0f)
        );
        // environment specifications
        JsonElement element;
        element = wrapper.getOptionalValue("weather").orElse(null);
        if (element != null) {
            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    skybox.weather.add(jsonElement.getAsString());
                }
            } else if (JSONUtils.isStringValue(element)) {
                skybox.weather.add(element.getAsString());
            }
        }
        element = wrapper.getOptionalValue("biomes").orElse(null);
        processIds(element, skybox.biomes);
        element = wrapper.getOptionalValue("dimensions").orElse(null);
        processIds(element, skybox.worlds);
        element = wrapper.getOptionalValue("heightRanges").orElse(null);
        if (element != null) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement jsonElement : array) {
                JsonArray insideArray = jsonElement.getAsJsonArray();
                float low = insideArray.get(0).getAsFloat();
                float high = insideArray.get(1).getAsFloat();
                skybox.heightRanges.add(new HeightEntry(low, high));
            }
        }
    }

    private static void processIds(JsonElement element, List<ResourceLocation> list) {
        if (element != null) {
            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    list.add(new ResourceLocation(jsonElement.getAsString()));
                }
            } else if (JSONUtils.isStringValue(element)) {
                list.add(new ResourceLocation(element.getAsString()));
            }
        }
    }

    private static <T> Class<T> c(Class<?> cls) { return (Class<T>)cls; }

    private static <T extends AbstractSkybox> LegacyDeserializer<T> register(LegacyDeserializer<T> deserializer, String name) {
        deserializer.setRegistryName(new ResourceLocation(FabricSkyBoxesClient.MODID, name));
        LegacyDeserializer.REGISTRY.register(deserializer);
        return deserializer;
    }
}
