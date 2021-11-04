package vice.customskyboxes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import vice.customskyboxes.resource.SkyboxResourceListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.transform.ErrorListener;

@Mod("customskyboxes")
public class FabricSkyBoxesClient {

    public static final String MODID = "customskyboxes";
    public static final Logger LOGGER = LogManager.getLogger();

    public FabricSkyBoxesClient() {
        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DistExecution::clientStart);
    }



    public static Logger getLogger() { return LOGGER; }
}

class DistExecution
{
    public static void clientStart()
    {
        SkyboxResourceListener reloadListener = new SkyboxResourceListener();

        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
            reloadableResourceManager.registerReloadListener(reloadListener);
        }
    }
}