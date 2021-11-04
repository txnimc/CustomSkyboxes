package vice.customskyboxes.util.object;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.ResourceLocation;
import vice.customskyboxes.skyboxes.AbstractSkybox;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Conditions {
    public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", ImmutableList.of()).forGetter(Conditions::getBiomes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("worlds", ImmutableList.of()).forGetter(Conditions::getWorlds),
            Weather.CODEC.listOf().optionalFieldOf("weather", ImmutableList.of()).forGetter(Conditions::getWeathers),
            HeightEntry.CODEC.listOf().optionalFieldOf("heights", ImmutableList.of()).forGetter(Conditions::getHeights)
    ).apply(instance, Conditions::new));
    public static final Conditions NO_CONDITIONS = new Builder().build();
    private final List<ResourceLocation> biomes;
    private final List<ResourceLocation> worlds;
    private final List<Weather> weathers;
    private final List<HeightEntry> heights;

    public Conditions(List<ResourceLocation> biomes, List<ResourceLocation> worlds, List<Weather> weathers, List<HeightEntry> heights) {
        this.biomes = biomes;
        this.worlds = worlds;
        this.weathers = weathers;
        this.heights = heights;
    }

    public List<ResourceLocation> getBiomes() {
        return this.biomes;
    }

    public List<ResourceLocation> getWorlds() {
        return this.worlds;
    }

    public List<Weather> getWeathers() {
        return this.weathers;
    }

    public List<HeightEntry> getHeights() {
        return this.heights;
    }

    public static Conditions ofSkybox(AbstractSkybox skybox) {
        return new Builder()
                .biomes(skybox.getBiomes())
                .worlds(skybox.getWorlds())
                .weather(skybox.getWeather()
                        .stream()
                        .map(Weather::fromString)
                        .collect(Collectors.toSet()))
                .heights(skybox.getHeightRanges())
                .build();
    }

    public static class Builder {
        private final List<ResourceLocation> biomes = Lists.newArrayList();
        private final List<ResourceLocation> worlds = Lists.newArrayList();
        private final List<Weather> weathers = Lists.newArrayList();
        private final List<HeightEntry> heights = Lists.newArrayList();

        public Builder biomes(Collection<ResourceLocation> biomeIds) {
            this.biomes.addAll(biomeIds);
            return this;
        }

        public Builder worlds(Collection<ResourceLocation> worldIds) {
            this.worlds.addAll(worldIds);
            return this;
        }

        public Builder weather(Collection<Weather> weathers) {
            this.weathers.addAll(weathers);
            return this;
        }

        public Builder heights(Collection<HeightEntry> heights) {
            this.heights.addAll(heights);
            return this;
        }

        public Builder biomes(ResourceLocation... biomeIds) {
            return this.biomes(Lists.newArrayList(biomeIds));
        }

        public Builder worlds(ResourceLocation... worldIds) {
            return this.worlds(Lists.newArrayList(worldIds));
        }

        public Builder weather(Weather... weathers) {
            return this.weather(Lists.newArrayList(weathers));
        }

        public Builder heights(HeightEntry... heights) {
            return this.heights(Lists.newArrayList(heights));
        }

        public Conditions build() {
            return new Conditions(this.biomes, this.worlds, this.weathers, this.heights);
        }
    }
}
