package pokecube.adventures.client;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.adventures.client.gui.GUIBiomeSetter;
import pokecube.adventures.client.gui.GuiAFA;
import pokecube.adventures.client.gui.GuiBag;
import pokecube.adventures.client.gui.GuiCommander;
import pokecube.adventures.client.gui.GuiDaycare;
import pokecube.adventures.client.gui.cloner.GuiCloner;
import pokecube.adventures.client.gui.cloner.GuiExtractor;
import pokecube.adventures.client.gui.cloner.GuiSplicer;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer;
import pokecube.adventures.client.render.blocks.RenderAFA;
import pokecube.adventures.client.render.blocks.RenderCloner;
import pokecube.adventures.client.render.entity.RenderTarget;
import pokecube.adventures.client.render.entity.RenderTrainer;
import pokecube.adventures.client.render.entity.TrainerBeltRenderer;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.events.RenderHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.items.bags.ContainerBag;

public class ClientProxy extends CommonProxy
{
    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (guiID == PokecubeAdv.GUITRAINER_ID) { return new GuiEditTrainer(world.getEntityByID(x)); }
        if (guiID == PokecubeAdv.GUIBAG_ID)
        {
            ContainerBag cont = new ContainerBag(player.inventory);
            cont.gotoInventoryPage(x);
            return new GuiBag(cont);
        }
        if (guiID == PokecubeAdv.GUICOMMANDER_ID)
        {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityCommander) return new GuiCommander((TileEntityCommander) tile);
        }
        if (guiID == PokecubeAdv.GUICLONER_ID) { return new GuiCloner(player.inventory,
                (TileEntityCloner) world.getTileEntity(pos)); }
        if (guiID == PokecubeAdv.GUISPLICER_ID) { return new GuiSplicer(player.inventory,
                (IInventory) world.getTileEntity(pos)); }
        if (guiID == PokecubeAdv.GUIEXTRACTOR_ID) { return new GuiExtractor(player.inventory,
                (IInventory) world.getTileEntity(pos)); }
        if (guiID == PokecubeAdv.GUIBIOMESETTER_ID) { return new GUIBiomeSetter(player.getHeldItemMainhand()); }
        if (guiID == PokecubeAdv.GUIAFA_ID)
        {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityDaycare) return new GuiDaycare(player.inventory, (TileEntityDaycare) tile);
            return new GuiAFA(player.inventory, (TileEntityAFA) tile);
        }
        return null;
    }

    @Override
    public EntityPlayer getPlayer()
    {
        return getPlayer(null);
    }

    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (isOnClientSide())
        {
            if (playerName != null) { return getWorld().getPlayerEntityByName(playerName); }
            return Minecraft.getMinecraft().player;
        }
        return super.getPlayer(playerName);
    }

    @Override
    public World getWorld()
    {
        if (isOnClientSide()) { return Minecraft.getMinecraft().world; }
        return super.getWorld();
    }

    @Override
    public void initClient()
    {
        RenderHandler h = new RenderHandler();
        MinecraftForge.EVENT_BUS.register(h);
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public void initItemModels()
    {
    }

    @Override
    public void postinit()
    {
        Set<Render<? extends Entity>> added = Sets.newHashSet();
        for (Render<? extends Entity> render : Minecraft.getMinecraft().getRenderManager().getSkinMap().values())
        {
            if (render instanceof RenderLivingBase<?>)
            {
                RenderLivingBase<?> renderb = (RenderLivingBase<?>) render;
                List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                        renderb, "layerRenderers", "field_177097_h", "i");
                layerRenderers.add(new TrainerBeltRenderer(renderb));
                added.add(render);
            }
        }
        for (Render<? extends Entity> render : Minecraft.getMinecraft().getRenderManager().entityRenderMap.values())
        {
            /** Dont add twice to player renderer if it is in skinmap. */
            if (added.contains(render)) continue;
            if (render instanceof RenderLivingBase<?>)
            {
                RenderLivingBase<?> renderb = (RenderLivingBase<?>) render;
                List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                        renderb, "layerRenderers", "field_177097_h", "i");
                layerRenderers.add(new TrainerBeltRenderer(renderb));
            }
        }
    }

    @Override
    public void preinit()
    {
        super.preinit();
        OBJLoader.INSTANCE.addDomain(PokecubeAdv.ID);

        RenderingRegistry.registerEntityRenderingHandler(EntityTarget.class, new IRenderFactory<EntityLivingBase>()
        {
            @Override
            public Render<? super EntityLivingBase> createRenderFor(RenderManager manager)
            {
                return new RenderTarget<>(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainer.class, new IRenderFactory<EntityLiving>()
        {
            @Override
            public Render<? super EntityLiving> createRenderFor(RenderManager manager)
            {
                return new RenderTrainer<>(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityLeader.class, new IRenderFactory<EntityLiving>()
        {
            @Override
            public Render<? super EntityLiving> createRenderFor(RenderManager manager)
            {
                return new RenderTrainer<>(manager);
            }
        });

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAFA.class, new RenderAFA());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCloner.class, new RenderCloner());
    }
}
