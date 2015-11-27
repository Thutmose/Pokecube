package pokecube.adventures.client;

import static pokecube.adventures.handlers.BlockHandler.cloner;
import static pokecube.adventures.handlers.BlockHandler.warppad;
import static pokecube.core.PokecubeItems.registerItemTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.client.gui.GUIBiomeSetter;
import pokecube.adventures.client.gui.GuiBag;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.adventures.client.gui.GuiTrainerEdit;
import pokecube.adventures.client.render.entity.RenderTarget;
import pokecube.adventures.client.render.entity.RenderTrainer;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.events.RenderHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.core.mod_Pokecube;
import thut.api.maths.Vector3;

public class ClientProxy extends CommonProxy
{

    public static KeyBinding bag;

    @Override
    public void preinit()
    {
        super.preinit();

        registerItemTexture(Item.getItemFromBlock(cloner), 0,
                new ModelResourceLocation("pokecube_adventures:cloner", "inventory"));

        registerItemTexture(Item.getItemFromBlock(warppad), 0,
                new ModelResourceLocation("pokecube_adventures:warppad", "inventory"));
        
    }

    @Override
    public void initClient()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTarget.class, new RenderTarget());
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class, new RenderTrainer());
        RenderingRegistry.registerEntityRenderingHandler(EntityLeader.class, new RenderTrainer());

        RenderHandler h = new RenderHandler();
        MinecraftForge.EVENT_BUS.register(h);
        FMLCommonHandler.instance().bus().register(h);

        // if(!(Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(EntityPlayer.class)
        // instanceof RenderPlayerPokemob))
        // {
        // Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(EntityPlayer.class,
        // new RenderPlayerPokemob());
        // }

        try
        {
            Class c = Class.forName("vazkii.botania.client.core.helper.RenderHelper");
            RenderHandler.BOTANIA = true;
        }
        catch (ClassNotFoundException e)
        {

        }

        ClientRegistry.registerKeyBinding(bag = new KeyBinding("Open Bag", 23, "Pokecube"));
    }

    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (isOnClientSide())
        {
            if (playerName != null)
            {
                return getWorld().getPlayerEntityByName(playerName);
            }
            else
            {
                return Minecraft.getMinecraft().thePlayer;
            }
        }
        else
        {
            return super.getPlayer(playerName);
        }
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public World getWorld()
    {
        if (isOnClientSide())
        {
            return Minecraft.getMinecraft().theWorld;
        }
        else
        {
            return super.getWorld();
        }
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        EntityPlayer entityPlayer = mod_Pokecube.getPlayer(null);
        TileEntity tileEntity = null;
        Entity entityHit = null;
        MovingObjectPosition objectClicked = ((Minecraft) mod_Pokecube.getMinecraftInstance()).objectMouseOver;

        if (objectClicked != null)
        {
            if (objectClicked.getBlockPos() != null)
                tileEntity = entityPlayer.worldObj.getTileEntity(objectClicked.getBlockPos());
            entityHit = objectClicked.entityHit;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (guiID == PokecubeAdv.GUITRAINER_ID)
        {
            return new GuiTrainerEdit((EntityTrainer) entityHit);
        }
        else if (guiID == PokecubeAdv.GUIBAG_ID)
        {
            ContainerBag cont = new ContainerBag(player.inventory);
            return new GuiBag(cont, Vector3.getNewVectorFromPool().set(x, y, z));
        }
        else if (guiID == PokecubeAdv.GUICLONER_ID)
        {
            return new GuiCloner(player.inventory, (TileEntityCloner) world.getTileEntity(pos));
        }
        else if (guiID == 5) { return new GUIBiomeSetter(player.getHeldItem()); }
        return null;
    }

    @Override
    public EntityPlayer getPlayer()
    {
        return getPlayer(null);
    }

}
