package pokecube.adventures.client;

import static pokecube.adventures.handlers.BlockHandler.cloner;
import static pokecube.adventures.handlers.BlockHandler.leaf0;
import static pokecube.adventures.handlers.BlockHandler.leaf1;
import static pokecube.adventures.handlers.BlockHandler.log0;
import static pokecube.adventures.handlers.BlockHandler.log1;
import static pokecube.adventures.handlers.BlockHandler.plank0;
import static pokecube.adventures.handlers.BlockHandler.warppad;
import static pokecube.core.PokecubeItems.registerItemTexture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.berries.BerryPlantManager;
import pokecube.adventures.blocks.berries.BlockBerryCrop;
import pokecube.adventures.blocks.berries.BlockBerryLeaves;
import pokecube.adventures.blocks.berries.BlockBerryLog;
import pokecube.adventures.blocks.berries.BlockBerryWood;
import pokecube.adventures.client.gui.GUIBiomeSetter;
import pokecube.adventures.client.gui.GuiBag;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.adventures.client.gui.GuiTrainerEdit;
import pokecube.adventures.client.models.ModelDeoxysAtk;
import pokecube.adventures.client.models.ModelDeoxysDef;
import pokecube.adventures.client.models.ModelDeoxysSpd;
import pokecube.adventures.client.models.ModelMegaGallade;
import pokecube.adventures.client.models.ModelMegaGardevoir;
import pokecube.adventures.client.models.ModelMegaSalamence;
import pokecube.adventures.client.models.ModelMegaScizor;
import pokecube.adventures.client.render.blocks.RenderBerries;
import pokecube.adventures.client.render.entity.RenderTarget;
import pokecube.adventures.client.render.entity.RenderTrainer;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.villager.EntityTrader;
import pokecube.adventures.events.RenderHandler;
import pokecube.adventures.items.*;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.core.mod_Pokecube;
import pokecube.core.items.berries.TileEntityBerryFruit;
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

        ModelLoader.setCustomStateMapper(leaf0,
                (new StateMap.Builder()).withName(BlockBerryLeaves.VARIANT0).withSuffix("Leaves")
                        .ignore(new IProperty[] { BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE })
                        .build());
        ModelLoader.setCustomStateMapper(leaf1,
                (new StateMap.Builder()).withName(BlockBerryLeaves.VARIANT4).withSuffix("Leaves")
                        .ignore(new IProperty[] { BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE })
                        .build());

        ModelLoader.setCustomStateMapper(log0,
                (new StateMap.Builder()).withName(BlockBerryLog.VARIANT0).withSuffix("Wood").build());
        ModelLoader.setCustomStateMapper(log1,
                (new StateMap.Builder()).withName(BlockBerryLog.VARIANT4).withSuffix("Wood").build());

        ModelLoader.setCustomStateMapper(plank0,
                (new StateMap.Builder()).withName(BlockBerryWood.VARIANT).withSuffix("Plank").build());

        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:pechaPlank");
        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:oranPlank");
        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:leppaPlank");
        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:sitrusPlank");
        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:enigmaPlank");
        ModelBakery.addVariantName(Item.getItemFromBlock(plank0), "pokecube_adventures:nanabPlank");
        registerItemTexture(Item.getItemFromBlock(plank0), 0,
                new ModelResourceLocation("pokecube_adventures:pechaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 1,
                new ModelResourceLocation("pokecube_adventures:oranPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 2,
                new ModelResourceLocation("pokecube_adventures:leppaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 3,
                new ModelResourceLocation("pokecube_adventures:sitrusPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 4,
                new ModelResourceLocation("pokecube_adventures:enigmaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 5,
                new ModelResourceLocation("pokecube_adventures:nanabPlank", "inventory"));

        ModelBakery.addVariantName(Item.getItemFromBlock(leaf0), "pokecube_adventures:pechaLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf0), 0,
                new ModelResourceLocation("pokecube_adventures:pechaLeaves", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(leaf0), "pokecube_adventures:oranLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf0), 1,
                new ModelResourceLocation("pokecube_adventures:oranLeaves", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(leaf0), "pokecube_adventures:leppaLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf0), 2,
                new ModelResourceLocation("pokecube_adventures:leppaLeaves", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(leaf0), "pokecube_adventures:sitrusLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf0), 3,
                new ModelResourceLocation("pokecube_adventures:sitrusLeaves", "inventory"));

        ModelBakery.addVariantName(Item.getItemFromBlock(log0), "pokecube_adventures:pechaWood");
        registerItemTexture(Item.getItemFromBlock(log0), 0,
                new ModelResourceLocation("pokecube_adventures:pechaWood", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(log0), "pokecube_adventures:oranWood");
        registerItemTexture(Item.getItemFromBlock(log0), 1,
                new ModelResourceLocation("pokecube_adventures:oranWood", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(log0), "pokecube_adventures:leppaWood");
        registerItemTexture(Item.getItemFromBlock(log0), 2,
                new ModelResourceLocation("pokecube_adventures:leppaWood", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(log0), "pokecube_adventures:sitrusWood");
        registerItemTexture(Item.getItemFromBlock(log0), 3,
                new ModelResourceLocation("pokecube_adventures:sitrusWood", "inventory"));

        ModelBakery.addVariantName(Item.getItemFromBlock(leaf1), "pokecube_adventures:enigmaLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf1), 0,
                new ModelResourceLocation("pokecube_adventures:enigmaLeaves", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(leaf1), "pokecube_adventures:nanabLeaves");
        registerItemTexture(Item.getItemFromBlock(leaf1), 1,
                new ModelResourceLocation("pokecube_adventures:nanabLeaves", "inventory"));

        ModelBakery.addVariantName(Item.getItemFromBlock(log1), "pokecube_adventures:enigmaWood");
        registerItemTexture(Item.getItemFromBlock(log1), 0,
                new ModelResourceLocation("pokecube_adventures:enigmaWood", "inventory"));
        ModelBakery.addVariantName(Item.getItemFromBlock(log1), "pokecube_adventures:nanabWood");
        registerItemTexture(Item.getItemFromBlock(log1), 1,
                new ModelResourceLocation("pokecube_adventures:nanabWood", "inventory"));

        for(String ident: BerryPlantManager.toRegister.keySet())
        {
            Block crop = BerryPlantManager.toRegister.get(ident);
            StateMap map = (new StateMap.Builder()).ignore(new IProperty[] {BlockBerryCrop.AGE}).withSuffix("").build();
            registerItemTexture(Item.getItemFromBlock(crop), 0, new ModelResourceLocation(ident, "inventory"));
            ModelLoader.setCustomStateMapper(crop, map);
        }
        
    }

    @Override
    public void initClient()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTarget.class, new RenderTarget());
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class, new RenderTrainer());
        RenderingRegistry.registerEntityRenderingHandler(EntityLeader.class, new RenderTrainer());
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBerryFruit.class, new RenderBerries());

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
            return new GuiCloner(player.inventory, world, x, y, z);
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
