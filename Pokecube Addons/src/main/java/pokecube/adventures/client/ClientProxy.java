package pokecube.adventures.client;

import static pokecube.adventures.handlers.BlockHandler.afa;
import static pokecube.adventures.handlers.BlockHandler.cloner;
import static pokecube.adventures.handlers.BlockHandler.warppad;
import static pokecube.core.PokecubeItems.registerItemTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.LegendaryConditions;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.client.gui.GUIBiomeSetter;
import pokecube.adventures.client.gui.GuiAFA;
import pokecube.adventures.client.gui.GuiBag;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.adventures.client.gui.GuiTrainerEdit;
import pokecube.adventures.client.render.blocks.RenderAFA;
import pokecube.adventures.client.render.entity.RenderTarget;
import pokecube.adventures.client.render.entity.RenderTrainer;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.events.RenderHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import thut.api.maths.Vector3;

public class ClientProxy extends CommonProxy
{

    public static KeyBinding bag;

    @Override
    public void preinit()
    {
        super.preinit();
        OBJLoader.instance.addDomain(PokecubeAdv.ID.toLowerCase());

        registerItemTexture(Item.getItemFromBlock(cloner), 0,
                new ModelResourceLocation("pokecube_adventures:cloner", "inventory"));

        registerItemTexture(Item.getItemFromBlock(afa), 0,
                new ModelResourceLocation("pokecube_adventures:afa", "inventory"));

        registerItemTexture(Item.getItemFromBlock(warppad), 0,
                new ModelResourceLocation("pokecube_adventures:warppad", "inventory"));

        StateMap map = (new StateMap.Builder()).withName(LegendaryConditions.spawner1.TYPE).withSuffix("_spawner")
                .build();
        ModelLoader.setCustomStateMapper(LegendaryConditions.spawner1, map);
        Item item = Item.getItemFromBlock(LegendaryConditions.spawner1);

        for (int i = 0; i < LegendaryConditions.spawner1.types.size(); i++)
        {
            ModelBakery.registerItemVariants(item, new ResourceLocation("pokecube_adventures:" + i + "_spawner"));
            PokecubeItems.registerItemTexture(item, i,
                    new ModelResourceLocation("pokecube_adventures:" + i + "_spawner", "inventory"));
        }

        //TODO in 1.9, this will need to be commented back in.
//        RenderingRegistry.registerEntityRenderingHandler(EntityTarget.class, new IRenderFactory<Entity>()
//        {
//            @Override
//            public Render<? super Entity> createRenderFor(RenderManager manager)
//            {
//                return new RenderTarget(manager);
//            }
//        });
//        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class, new IRenderFactory<Entity>()
//        {
//            @Override
//            public Render<? super Entity> createRenderFor(RenderManager manager)
//            {
//                return new RenderTrainer(manager);
//            }
//        });
//        RenderingRegistry.registerEntityRenderingHandler(EntityLeader.class, new IRenderFactory<Entity>()
//        {
//            @Override
//            public Render<? super Entity> createRenderFor(RenderManager manager)
//            {
//                return new RenderTrainer(manager);
//            }
//        });
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAFA.class, new RenderAFA());
    }

    @Override
    public void initClient()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTarget.class,
                new RenderTarget(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class,
                new RenderTrainer(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityLeader.class,
                new RenderTrainer(Minecraft.getMinecraft().getRenderManager()));

        RenderHandler h = new RenderHandler();
        MinecraftForge.EVENT_BUS.register(h);

        try
        {
            Class.forName("vazkii.botania.client.core.helper.RenderHelper");
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
        Entity entityHit = null;
        MovingObjectPosition objectClicked = ((Minecraft) mod_Pokecube.getMinecraftInstance()).objectMouseOver;

        if (objectClicked != null)
        {
            if (objectClicked.getBlockPos() != null)
            {
            }
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
        else if (guiID == PokecubeAdv.GUIBIOMESETTER_ID)
        {
            return new GUIBiomeSetter(player.getHeldItem());
        }
        else if (guiID == PokecubeAdv.GUIAFA_ID) { return new GuiAFA(player.inventory,
                (TileEntityAFA) world.getTileEntity(pos)); }
        return null;
    }

    @Override
    public EntityPlayer getPlayer()
    {
        return getPlayer(null);
    }

}
