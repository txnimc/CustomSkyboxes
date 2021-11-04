package vice.customskyboxes.skyboxes;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import lombok.val;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.*;
import vice.customskyboxes.FabricSkyBoxesClient;
import vice.customskyboxes.skyboxes.textured.AnimatedSquareTexturedSkybox;
import vice.customskyboxes.skyboxes.textured.SingleSpriteAnimatedSquareTexturedSkybox;
import vice.customskyboxes.skyboxes.textured.SingleSpriteSquareTexturedSkybox;
import vice.customskyboxes.skyboxes.textured.SquareTexturedSkybox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.ResourceLocation;

public class SkyboxType<T extends AbstractSkybox> extends ForgeRegistryEntry<SkyboxType<? extends AbstractSkybox>>
{
    public static final IForgeRegistry<SkyboxType<? extends AbstractSkybox>> REGISTRY;

    public static final SkyboxType<MonoColorSkybox> MONO_COLOR_SKYBOX;
    public static final SkyboxType<SquareTexturedSkybox> SQUARE_TEXTURED_SKYBOX;
    public static final SkyboxType<SingleSpriteSquareTexturedSkybox> SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX;
    public static final SkyboxType<AnimatedSquareTexturedSkybox> ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final SkyboxType<SingleSpriteAnimatedSquareTexturedSkybox> SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final Codec<ResourceLocation> SKYBOX_ID_CODEC;

    private final BiMap<Integer, Codec<T>> codecBiMap;
    private final boolean legacySupported;
    private final String name;
    @Nullable
    private final Supplier<T> factory;
    @Nullable
    private final LegacyDeserializer<T> deserializer;

    private SkyboxType(BiMap<Integer, Codec<T>> codecBiMap, boolean legacySupported, String name, @Nullable Supplier<T> factory, @Nullable LegacyDeserializer<T> deserializer) {
        this.codecBiMap = codecBiMap;
        this.legacySupported = legacySupported;
        this.name = name;
        this.factory = factory;
        this.deserializer = deserializer;
    }

    public String getName() {
        return this.name;
    }

    public boolean isLegacySupported() {
        return this.legacySupported;
    }

    @NotNull
    public T instantiate() {
        return Objects.requireNonNull(Objects.requireNonNull(this.factory, "Can't instantiate from a null factory").get());
    }

    @Nullable
    public LegacyDeserializer<T> getDeserializer() {
        return this.deserializer;
    }

    public ResourceLocation createId(String namespace) {
        return this.createIdFactory().apply(namespace);
    }

    public Function<String, ResourceLocation> createIdFactory() {
        return (ns) -> new ResourceLocation(ns, this.getName().replace('-', '_'));
    }

    public Codec<T> getCodec(int schemaVersion) {
        return Objects.requireNonNull(this.codecBiMap.get(schemaVersion), String.format("Unsupported schema version '%d' for skybox type %s", schemaVersion, this.name));
    }

    private static <T> Class<T> c(Class<?> cls) { return (Class<T>)cls; }

    static {


        REGISTRY = new RegistryBuilder<SkyboxType<? extends AbstractSkybox>>()
                .setName(new ResourceLocation(FabricSkyBoxesClient.MODID, "skybox_type"))
                .setType(c(SkyboxType.class))
                .create();

        MONO_COLOR_SKYBOX = Builder.create(MonoColorSkybox.class, "monocolor")
                .legacySupported()
                .deserializer(LegacyDeserializer.MONO_COLOR_SKYBOX_DESERIALIZER)
                .factory(MonoColorSkybox::new)
                .add(2, MonoColorSkybox.CODEC)
                .buildAndRegister(FabricSkyBoxesClient.MODID);

        SQUARE_TEXTURED_SKYBOX = Builder.create(SquareTexturedSkybox.class, "square-textured")
                .deserializer(LegacyDeserializer.SQUARE_TEXTURED_SKYBOX_DESERIALIZER)
                .legacySupported()
                .factory(SquareTexturedSkybox::new)
                .add(2, SquareTexturedSkybox.CODEC)
                .buildAndRegister(FabricSkyBoxesClient.MODID);

        SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX = Builder.create(SingleSpriteSquareTexturedSkybox.class, "single-sprite-square-textured")
                .add(2, SingleSpriteSquareTexturedSkybox.CODEC)
                .build();

        ANIMATED_SQUARE_TEXTURED_SKYBOX = Builder.create(AnimatedSquareTexturedSkybox.class, "animated-square-textured")
                .add(2, AnimatedSquareTexturedSkybox.CODEC)
                .buildAndRegister(FabricSkyBoxesClient.MODID);

        SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX = Builder.create(SingleSpriteAnimatedSquareTexturedSkybox.class, "single-sprite-animated-square-textured")
                .add(2, SingleSpriteAnimatedSquareTexturedSkybox.CODEC)
                .buildAndRegister(FabricSkyBoxesClient.MODID);

        SKYBOX_ID_CODEC = Codec.STRING.xmap((s) -> {
            if (!s.contains(":")) {
                return new ResourceLocation(FabricSkyBoxesClient.MODID, s.replace('-', '_'));
            }
            return new ResourceLocation(s.replace('-', '_'));
        }, (id) -> {
            if (id.getNamespace().equals(FabricSkyBoxesClient.MODID)) {
                return id.getPath().replace('_', '-');
            }
            return id.toString().replace('_', '-');
        });
    }

    public static class Builder<T extends AbstractSkybox> {
        private String name;
        private final ImmutableBiMap.Builder<Integer, Codec<T>> builder = ImmutableBiMap.builder();
        private boolean legacySupported = false;
        private Supplier<T> factory;
        private LegacyDeserializer<T> deserializer;

        private Builder() {
        }

        public static <S extends AbstractSkybox> Builder<S> create(@SuppressWarnings("unused") Class<S> clazz, String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        public static <S extends AbstractSkybox> Builder<S> create(String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        protected Builder<T> legacySupported() {
            this.legacySupported = true;
            return this;
        }

        protected Builder<T> factory(Supplier<T> factory) {
            this.factory = factory;
            return this;
        }

        protected Builder<T> deserializer(LegacyDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Builder<T> add(int schemaVersion, Codec<T> codec) {
            Preconditions.checkArgument(schemaVersion >= 2, "schema version was lesser than 2");
            Preconditions.checkNotNull(codec, "codec was null");
            this.builder.put(schemaVersion, codec);
            return this;
        }

        public SkyboxType<T> build() {
            if (this.legacySupported) {
                Preconditions.checkNotNull(this.factory, "factory was null");
                Preconditions.checkNotNull(this.deserializer, "deserializer was null");
            }
            return new SkyboxType<>(this.builder.build(), this.legacySupported, this.name, this.factory, this.deserializer);
        }

        public SkyboxType<T> buildAndRegister(String namespace)
        {
            val type = build();
            type.setRegistryName(new ResourceLocation(namespace, this.name.replace('-', '_')));
            SkyboxType.REGISTRY.register(type);
            return type;
        }
    }
}
