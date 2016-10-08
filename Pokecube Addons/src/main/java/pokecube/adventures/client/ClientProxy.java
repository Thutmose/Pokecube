package pokecube.adventures.client;

import static pokecube.adventures.handlers.BlockHandler.afa;
import static pokecube.adventures.handlers.BlockHandler.cloner;
import static pokecube.adventures.handlers.BlockHandler.siphon;
import static pokecube.adventures.handlers.BlockHandler.warppad;
import static pokecube.core.PokecubeItems.registerItemTexture;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.CommonProxy;
import pokecube.adventures.LegendaryConditions;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.client.gui.GUIBiomeSetter;
import pokecube.adventures.client.gui.GuiAFA;
import pokecube.adventures.client.gui.GuiBag;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.adventures.client.gui.GuiDaycare;
import pokecube.adventures.client.gui.GuiTrainerEdit;
import pokecube.adventures.client.render.blocks.RenderAFA;
import pokecube.adventures.client.render.blocks.RenderCloner;
import pokecube.adventures.client.render.entity.RenderTarget;
import pokecube.adventures.client.render.entity.RenderTrainer;
import pokecube.adventures.client.render.item.BadgeTextureHandler;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.events.RenderHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.api.maths.Vector3;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;

public class ClientProxy extends CommonProxy
{
    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        Entity entityHit = null;
        RayTraceResult objectClicked = ((Minecraft) PokecubeCore.getMinecraftInstance()).objectMouseOver;

        if (objectClicked != null)
        {
            if (objectClicked.getBlockPos() != null)
            {
            }
            entityHit = objectClicked.entityHit;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (guiID == PokecubeAdv.GUITRAINER_ID) { return new GuiTrainerEdit((EntityTrainer) entityHit); }
        if (guiID == PokecubeAdv.GUIBAG_ID)
        {
            ContainerBag cont = new ContainerBag(player.inventory);
            cont.gotoInventoryPage(x);
            return new GuiBag(cont, Vector3.getNewVector().set(x, y, z));
        }
        if (guiID == PokecubeAdv.GUICLONER_ID) { return new GuiCloner(player.inventory,
                (TileEntityCloner) world.getTileEntity(pos)); }
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
            return Minecraft.getMinecraft().thePlayer;
        }
        return super.getPlayer(playerName);
    }

    @Override
    public World getWorld()
    {
        if (isOnClientSide()) { return Minecraft.getMinecraft().theWorld; }
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
    public void preinit()
    {
        super.preinit();
        OBJLoader.INSTANCE.addDomain(PokecubeAdv.ID);

        Item item2 = Item.getItemFromBlock(cloner);
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeAdv.ID + ":reanimator", "inventory"));
        ModelLoader.setCustomModelResourceLocation(item2, 1,
                new ModelResourceLocation(PokecubeAdv.ID + ":splicer", "inventory"));

        item2 = Item.getItemFromBlock(afa);
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeAdv.ID + ":amplifier", "inventory"));
        ModelLoader.setCustomModelResourceLocation(item2, 1,
                new ModelResourceLocation(PokecubeAdv.ID + ":daycare", "inventory"));

        registerItemTexture(Item.getItemFromBlock(warppad), 0,
                new ModelResourceLocation("pokecube_adventures:warppad", "inventory"));

        registerItemTexture(Item.getItemFromBlock(siphon), 0,
                new ModelResourceLocation("pokecube_adventures:pokesiphon", "inventory"));

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
        BadgeTextureHandler.registerItemModels();
    }

    X3dModel                 bag1;
    X3dModel                 bag2;

    private ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_1.png");
    private ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_2.png");

    @Method(modid = "thut_wearables")
    @Override
    public void renderWearable(thut.wearables.EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        if (bag1 == null)
        {
            bag1 = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/worn/bag.x3d"));
            bag2 = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/worn/bag.x3d"));
        }
        int brightness = wearer.getBrightnessForRender(partialTicks);
        ResourceLocation pass1 = BAG_1;
        ResourceLocation pass2 = BAG_2;
        GL11.glPushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // First pass of render
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslated(0, -0.125, -0.6);
        GL11.glScaled(0.7, 0.7, 0.7);
        Minecraft.getMinecraft().renderEngine.bindTexture(pass1);
        bag1.renderAll();
        GL11.glPopMatrix();

        // Second pass with colour.
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslated(0, -0.125, -0.6);
        GL11.glScaled(0.7, 0.7, 0.7);
        Minecraft.getMinecraft().renderEngine.bindTexture(pass2);
        EnumDyeColor ret = EnumDyeColor.YELLOW;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            int damage = stack.getTagCompound().getInteger("dyeColour");
            ret = EnumDyeColor.byDyeDamage(damage);
        }
        Color colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
        int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
        for (IExtendedModelPart part : bag2.getParts().values())
        {
            part.setRGBAB(col);
        }
        bag2.renderAll();
        GL11.glColor3f(1, 1, 1);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }
}
