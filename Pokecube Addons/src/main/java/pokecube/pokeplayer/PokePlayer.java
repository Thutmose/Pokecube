package pokecube.pokeplayer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.pokeplayer.block.BlockTransformer;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

@Mod( // @formatter:off
        modid = PokePlayer.ID, 
        name = "Pokecube Mystery Dungeon", 
        version = PokePlayer.VERSION, 
        dependencies = PokePlayer.DEPSTRING, 
      //  guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", 
      //  updateJSON = PokePlayer.UPDATEURL, 
        acceptedMinecraftVersions = "*"
        )// @formatter:on
public class PokePlayer
{
    public static final String ID         = "pokeplayer";
    public static final String VERSION    = "@VERSION";
    public final static String DEPSTRING  = "required-after:pokecube@@POKECUBEVERSION";
    public final static String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/pokeplayer.json";

    @SidedProxy(clientSide = "pokecube.pokeplayer.client.ProxyClient", serverSide = "pokecube.pokeplayer.Proxy")
    public static Proxy        PROXY;

    @Instance(ID)
    public static PokePlayer   INSTANCE;

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
//        new EventsHandler(PROXY);
//        NetworkRegistry.INSTANCE.registerGuiHandler(this, PROXY);
//        PROXY.init();
//        PokecubeMod.packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class,
//                PokecubeCore.getMessageID(), Side.CLIENT);
//        PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
//                PokecubeCore.getMessageID(), Side.SERVER);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
//        PROXY.postInit();
//        PokecubeMod.core
//                .setEntityProvider(new EntityProviderPokeplayer((EntityProvider) PokecubeMod.core.getEntityProvider()));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        Block b = new BlockTransformer().setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks)
                .setUnlocalizedName("poketransformer");
        PokecubeItems.register(b, "poketransformer");
        GameRegistry.registerTileEntity(TileEntityTransformer.class, "poketransformer");
        if (e.getSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(Item.getItemFromBlock(b), 0,
                    new ModelResourceLocation("pokeplayer:poketransformer", "inventory"));
        }
    }
}
