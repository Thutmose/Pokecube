package pokecube.pokeplayer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.EntityProvider;
import pokecube.pokeplayer.block.BlockTransformer;
import pokecube.pokeplayer.network.EntityProviderPokeplayer;
import pokecube.pokeplayer.network.PacketDoActions;
import pokecube.pokeplayer.network.PacketTransform;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;
import thut.core.common.handlers.PlayerDataHandler;

@Mod( // @formatter:off
        modid = PokePlayer.ID, 
        name = "Pokecube Mystery Dungeon", 
        version = PokePlayer.VERSION, 
        dependencies = PokePlayer.DEPSTRING, 
        acceptedMinecraftVersions = "*"
        )// @formatter:on
public class PokePlayer
{
    public static final String ID          = "pokeplayer";
    public static final String VERSION     = "@VERSION";
    public final static String DEPSTRING   = "required-after:pokecube@@POKECUBEVERSION";
    public final static String UPDATEURL   = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/pokeplayer.json";

    @SidedProxy(clientSide = "pokecube.pokeplayer.client.ProxyClient", serverSide = "pokecube.pokeplayer.Proxy")
    public static Proxy        PROXY;

    @Instance(ID)
    public static PokePlayer   INSTANCE;

    static Block               transformer = new BlockTransformer()
            .setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks).setUnlocalizedName("poketransformer")
            .setRegistryName(ID, "poketransformer");

    public PokePlayer()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        new EventsHandler(PROXY);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, PROXY);
        PlayerDataHandler.dataMap.add(PokeInfo.class);
        PROXY.init();
        PokecubeMod.packetPipeline.registerMessage(PacketDoActions.class, PacketDoActions.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketDoActions.class, PacketDoActions.class,
                PokecubeCore.getMessageID(), Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketTransform.class, PacketTransform.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        PROXY.postInit();
        PokecubeMod.core
                .setEntityProvider(new EntityProviderPokeplayer((EntityProvider) PokecubeMod.core.getEntityProvider()));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> evt)
    {
        PokecubeItems.register(transformer, evt.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt)
    {
        Object registry = evt.getRegistry();
        PokecubeItems.register(new ItemBlock(transformer).setRegistryName(transformer.getRegistryName()), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(Item.getItemFromBlock(transformer), 0,
                    new ModelResourceLocation("pokeplayer:poketransformer", "inventory"));
        }
    }

    @SubscribeEvent
    public void registerTiles(RegistryEvent.Register<Block> evt)
    {
        GameRegistry.registerTileEntity(TileEntityTransformer.class, "poketransformer");
    }
}
