package vice.customskyboxes.resource;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.util.Unit;
import net.minecraftforge.resource.IResourceType;
import org.jetbrains.annotations.NotNull;
import vice.customskyboxes.FabricSkyBoxesClient;
import vice.customskyboxes.SkyboxManager;
import vice.customskyboxes.skyboxes.AbstractSkybox;
import vice.customskyboxes.skyboxes.SkyboxType;
import vice.customskyboxes.util.JsonObjectWrapper;
import vice.customskyboxes.util.object.internal.Metadata;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

public class SkyboxResourceListener implements ISelectiveResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().setLenient().create();
    private static final JsonObjectWrapper objectWrapper = new JsonObjectWrapper();


    private static AbstractSkybox parseSkyboxJson(ResourceLocation id) {
        AbstractSkybox skybox;
        Metadata metadata;
        FabricSkyBoxesClient.LOGGER.info("parseSkyboxJson for " + id);

        try {
            metadata = Metadata.CODEC.decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        } catch (RuntimeException e) {
            FabricSkyBoxesClient.getLogger().warn("Skipping invalid skybox " + id.toString(), e);
            FabricSkyBoxesClient.getLogger().warn(objectWrapper.toString());
            return null;
        }

        FabricSkyBoxesClient.LOGGER.info("decoded metadata for " + id);

        SkyboxType<? extends AbstractSkybox> type = SkyboxType.REGISTRY.getValue(metadata.getType());

        Preconditions.checkNotNull(type, "Unknown skybox type: " + metadata.getType().getPath().replace('_', '-'));
        if (metadata.getSchemaVersion() == 1)
        {
            Preconditions.checkArgument(type.isLegacySupported(), "Unsupported schema version '1' for skybox type " + type.getName());
            FabricSkyBoxesClient.getLogger().debug("Using legacy deserializer for skybox " + id.toString());
            skybox = type.instantiate();
            //noinspection ConstantConditions
            type.getDeserializer().getDeserializer().accept(objectWrapper, skybox);
        }
        else
        {
            FabricSkyBoxesClient.LOGGER.info("getSchemaVersion for " + id);

            skybox = type.getCodec(metadata.getSchemaVersion())
                    .decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject())
                    .getOrThrow(false, System.err::println).getFirst();
        }

        FabricSkyBoxesClient.LOGGER.info("returning skybox for " + id);
        return skybox;
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> resourcePredicate)
    {
        FabricSkyBoxesClient.LOGGER.info("onResourceManagerReload");

        SkyboxManager skyboxManager = SkyboxManager.getInstance();

        // clear registered skyboxes on reload
        skyboxManager.clearSkyboxes();

        // load new skyboxes
        Collection<ResourceLocation> resources = manager.listResources("sky", (string) -> string.endsWith(".json"));

        for (ResourceLocation id : resources) {

            IResource resource;
            try {
                resource = manager.getResource(id);
                try {
                    JsonObject json = GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
                    objectWrapper.setFocusedObject(json);
                    AbstractSkybox skybox = SkyboxResourceListener.parseSkyboxJson(id);
                    if (skybox != null) {
                        skyboxManager.addSkybox(skybox);
                    }
                } finally {
                    try {
                        resource.close();
                    } catch (IOException e) {
                        FabricSkyBoxesClient.getLogger().error("Error closing resource " + id.toString());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                FabricSkyBoxesClient.getLogger().error("Error reading skybox " + id.toString());
                e.printStackTrace();
            }
        }

    }
}
