package pokecube.pokeplayer;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.pokeplayer.Proxy.PokeInfo;
import pokecube.pokeplayer.block.BlockTransformer;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageClient;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageClient.MessageHandlerClient;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageServer;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageServer.MessageHandlerServer;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

@Mod( // @formatter:off
        modid = PokePlayer.ID, 
        name = "Pokecube Mystery Dungeon", 
        version = PokePlayer.version, 
        dependencies = PokePlayer.DEPSTRING, 
      //  guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", 
      //  updateJSON = PokePlayer.UPDATEURL, 
        acceptedMinecraftVersions = PokePlayer.MCVERSIONS
        )// @formatter:on
public class PokePlayer
{
    public static final String ID         = "pokeplayer";
    public static final String version    = "@VERSION";
    public final static String MCVERSIONS = "@MCVERSION";
    public final static String DEPSTRING  = "required-after:pokecube@@POKECUBEVERSION";
    public final static String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/pokeplayer.json";

    @SidedProxy(clientSide = "pokecube.pokeplayer.client.ProxyClient", serverSide = "pokecube.pokeplayer.Proxy")
    public static Proxy        proxy;

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent event)
    {

    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(this);
        proxy.init();
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
                PokecubeCore.getMessageID(), Side.SERVER);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        proxy.postInit();
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

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            IPokemob pokemob = proxy.getPokemob(event.player);
            if (pokemob != null)
            {
                ((Entity) pokemob).onUpdate();
                proxy.copyTransform((EntityLivingBase) pokemob, event.player);
                if (pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys())
                {
                    event.player.fallDistance = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        if (!evt.player.worldObj.isRemote)
        {
            new SendPacket(evt.player);
        }
    }

    public static class SendPacket
    {
        final EntityPlayer player;

        public SendPacket(EntityPlayer player)
        {
            this.player = player;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event)
        {
            if (event.player == player)
            {
                proxy.getPokemob(player);
                boolean pokemob = player.getEntityData().getBoolean("isPokemob");
                PokeInfo info = proxy.playerMap.get(player.getUniqueID());
                if (info == null) pokemob = false;
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(2));
                MessageClient message = new MessageClient(buffer);
                buffer.writeByte(MessageClient.SETPOKE);
                buffer.writeBoolean(pokemob);
                if (pokemob)
                {
                    buffer.writeFloat(info.originalHeight);
                    buffer.writeFloat(info.originalWidth);
                    buffer.writeNBTTagCompoundToBuffer(player.getEntityData().getCompoundTag("Pokemob"));
                }
                PokecubeMod.packetPipeline.sendToDimension(message, player.dimension);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}
