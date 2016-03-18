/**
 *
 */
package pokecube.core.client;

import static pokecube.core.PokecubeItems.pc;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.PokecubeItems.tradingtable;
import static pokecube.core.handlers.ItemHandler.leaf0;
import static pokecube.core.handlers.ItemHandler.leaf1;
import static pokecube.core.handlers.ItemHandler.log0;
import static pokecube.core.handlers.ItemHandler.log1;
import static pokecube.core.handlers.ItemHandler.plank0;

import java.util.BitSet;
import java.util.HashMap;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.CommonProxyPokecube;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryPlantManager;
import pokecube.core.blocks.berries.BlockBerryLeaves;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.blocks.healtable.TileHealTable;
import pokecube.core.blocks.pc.BlockPC;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.blocks.pokecubeTable.TileEntityPokecubeTable;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.client.gui.GuiChooseFirstPokemob;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.gui.blocks.GuiHealTable;
import pokecube.core.client.gui.blocks.GuiPC;
import pokecube.core.client.gui.blocks.GuiTMCreator;
import pokecube.core.client.gui.blocks.GuiTradingTable;
import pokecube.core.client.models.ModelPokemobEgg;
import pokecube.core.client.render.blocks.RenderPokecubeTable;
import pokecube.core.client.render.blocks.RenderTradingTable;
import pokecube.core.client.render.entity.RenderPokecube;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.client.render.entity.RenderProfessor;
import pokecube.core.client.render.particle.IParticle;
import pokecube.core.client.render.particle.ParticleFactory;
import pokecube.core.client.render.particle.ParticleHandler;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.maths.Vector3;

@SideOnly(Side.CLIENT)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientProxyPokecube extends CommonProxyPokecube
{
    private static BitSet models = new BitSet();
    static boolean        init   = true;
    static boolean        first  = true;

    public static KeyBinding nextMob;

    public static KeyBinding nextMove;

    public static KeyBinding previousMob;

    public static KeyBinding previousMove;

    public static KeyBinding mobBack;

    public static KeyBinding mobAttack;

    public static KeyBinding mobStance;

    public static KeyBinding mobMegavolve;

    public static KeyBinding mobMove1;

    public static KeyBinding mobMove2;

    public static KeyBinding mobMove3;

    public static KeyBinding mobMove4;
    private HashMap<Integer, Object> cubeRenders = new HashMap<Integer, Object>();
    public ClientProxyPokecube()
    {
        if (first) instance = this;
        first = false;
        EventsHandlerClient hndlr = new EventsHandlerClient();
        MinecraftForge.EVENT_BUS.register(hndlr);
        MinecraftForge.EVENT_BUS.register(this);
    }
    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        Entity entityHit = null;

        if (PokecubeCore.isOnClientSide())
        {
            MovingObjectPosition objectClicked = ((Minecraft) PokecubeCore.getMinecraftInstance()).objectMouseOver;

            if (objectClicked != null)
            {
                entityHit = objectClicked.entityHit;
            }
        }
        BlockPos pos = new BlockPos(x, y, z);

        if (guiID == Config.GUIPOKECENTER_ID)
        {
            TileEntity tile_entity = world.getTileEntity(pos);

            if (tile_entity instanceof TileHealTable) { return new GuiHealTable(player.inventory,
                    (TileHealTable) tile_entity); }
        }
        else
        {

            if (guiID == Config.GUIDISPLAYPOKECUBEINFO_ID)
            {
                return null;
            }
            else
            {

                if (guiID == Config.GUIDISPLAYTELEPORTINFO_ID)
                {
                    return null;
                }
                else
                {

                    if (guiID == Config.GUIPOKEDEX_ID)
                    {
                        if (entityHit instanceof IPokemob) return new GuiPokedex((IPokemob) entityHit, player);
                        else return new GuiPokedex(null, player);
                    }
                    else
                    {

                        if (guiID == Config.GUIPOKEMOB_ID)
                        {
                            EntityPokemob e = (EntityPokemob) world.getEntityByID(x);
                            return new GuiPokemob(player.inventory, e);
                        }
                        else
                        {

                            if (guiID == Config.GUITRADINGTABLE_ID)
                            {
                                TileEntityTradingTable tile = (TileEntityTradingTable) world.getTileEntity(pos);
                                boolean tmc = world.getBlockState(pos).getValue(BlockTradingTable.TMC);
                                if (!tmc) return new GuiTradingTable(player.inventory, tile);
                                else return new GuiTMCreator(new ContainerTMCreator(tile, player.inventory));
                            }
                            else
                            {

                                if (guiID == Config.GUIPC_ID)
                                {
                                    TileEntityPC tile = (TileEntityPC) world.getTileEntity(pos);
                                    ContainerPC pc = new ContainerPC(player.inventory, tile);
                                    return new GuiPC(pc);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (guiID == Config.GUICHOOSEFIRSTPOKEMOB_ID)
        {
            boolean fixed = false;
            return new GuiChooseFirstPokemob(null, fixed);
        }
        return null;
    }
    @Override
    public String getFolderName()
    {
        if (FMLClientHandler.instance().getClient().theWorld != null)
            return FMLClientHandler.instance().getClient().theWorld.provider.getSaveFolder();
        return "";
    }
    @Override
    public IThreadListener getMainThreadListener()
    {
        if (isOnClientSide())
        {
            return Minecraft.getMinecraft();
        }
        else
        {
            return super.getMainThreadListener();
        }
    }
    @Override
    public ISnooperInfo getMinecraftInstance()
    {
        if (isOnClientSide())
        {
            return Minecraft.getMinecraft();
        }
        else
        {
            return super.getMinecraftInstance();
        }
    }
    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null)
        {
            try
            {
                UUID.fromString(playerName);
                return getWorld().getPlayerEntityByUUID(UUID.fromString(playerName));
            }
            catch (Exception e)
            {

            }
            return getWorld().getPlayerEntityByName(playerName);
        }
        else
        {
            return Minecraft.getMinecraft().thePlayer;
        }
    }

    @Override
    public World getWorld()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            return FMLClientHandler.instance().getWorldClient();
        }
        else
        {
            return super.getWorld();
        }
    }
    @Override
    public void initClient()
    {
        super.initClient();

        ResourceLocation pokecenterloop = new ResourceLocation("pokecube:sounds/pokecenterloop.ogg");
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(pokecenterloop);
            res.getInputStream().close();
        }
        catch (Exception e)
        {
            TileHealTable.noSound = true;
        }
    }
    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }
    @Override
    public boolean isSoundPlaying(Vector3 location)
    {
        try
        {
            BlockPos num = new BlockPos(location.intX(), location.intY(), location.intZ());
            Object sound = Minecraft.getMinecraft().renderGlobal.mapSoundPositions.get(num);
            return sound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying((ISound) sound);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void preInit(FMLPreInitializationEvent evt)
    {
        super.preInit(evt);

        RenderingRegistry.registerEntityRenderingHandler(EntityProfessor.class, new IRenderFactory<EntityLiving>()
        {
            @Override
            public Render<? super EntityLiving> createRenderFor(RenderManager manager)
            {
                return new RenderProfessor<>(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityPokecube.class, new IRenderFactory<EntityLiving>()
        {
            @Override
            public Render<? super EntityLiving> createRenderFor(RenderManager manager)
            {
                return new RenderPokecube<>(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityPokemob.class, new IRenderFactory<EntityLivingBase>()
        {
            @Override
            public Render<? super EntityLivingBase> createRenderFor(RenderManager manager)
            {
                return RenderPokemobs.getInstance(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityPokemobEgg.class, new IRenderFactory<Entity>()
        {
            @Override
            public Render<? super Entity> createRenderFor(RenderManager manager)
            {
                return new RenderLiving(manager, new ModelPokemobEgg(), 0.25f)
                {
                    @Override
                    protected ResourceLocation getEntityTexture(Entity egg)
                    {
                        return new ResourceLocation(PokecubeMod.ID + ":textures/egg.png");
                    }
                };
            }
        });

        Item tm = PokecubeItems.getItem("tm");

        for (int i = 0; i < 19; i++)
        {
            ModelBakery.registerItemVariants(tm, new ResourceLocation("pokecube:tm" + i));
            PokecubeItems.registerItemTexture(tm, i, new ModelResourceLocation("pokecube:tm" + i, "inventory"));
        }
        ModelBakery.registerItemVariants(tm, new ResourceLocation("pokecube:rarecandy"));
        PokecubeItems.registerItemTexture(tm, 20, new ModelResourceLocation("pokecube:rarecandy", "inventory"));

        ModelBakery.registerItemVariants(tm, new ResourceLocation("pokecube:emerald_shard"));
        PokecubeItems.registerItemTexture(tm, 19, new ModelResourceLocation("pokecube:emerald_shard", "inventory"));

        StateMap map = (new StateMap.Builder()).ignore(new IProperty[] { BlockPC.FACING }).build();
        ModelLoader.setCustomStateMapper(pc, map);

        map = (new StateMap.Builder()).ignore(new IProperty[] { BlockTradingTable.FACING, BlockTradingTable.TMC })
                .build();
        ModelLoader.setCustomStateMapper(tradingtable, map);
        registerItemTexture(Item.getItemFromBlock(tradingtable), 0,
                new ModelResourceLocation("pokecube:tradingtable", "inventory"));

        OBJLoader.instance.addDomain(PokecubeMod.ID.toLowerCase());
        Item item2 = Item.getItemFromBlock(PokecubeItems.tableBlock);
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeMod.ID + ":pokecube_table", "inventory"));

        item2 = Item.getItemFromBlock(PokecubeItems.getBlock("pc"));
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeMod.ID + ":pc_base", "inventory"));
        ModelLoader.setCustomModelResourceLocation(item2, 8,
                new ModelResourceLocation(PokecubeMod.ID + ":pc_top", "inventory"));

        item2 = Item.getItemFromBlock(PokecubeItems.getBlock("tradingtable"));
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeMod.ID + ":tradingtable", "inventory"));

        ModelLoader.setCustomStateMapper(leaf0,
                (new StateMap.Builder()).withName(BlockBerryLeaves.VARIANT0).withSuffix("Leaves")
                        .ignore(new IProperty[] { BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE }).build());
        ModelLoader.setCustomStateMapper(leaf1,
                (new StateMap.Builder()).withName(BlockBerryLeaves.VARIANT4).withSuffix("Leaves")
                        .ignore(new IProperty[] { BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE }).build());

        ModelLoader.setCustomStateMapper(log0,
                (new StateMap.Builder()).withName(BlockBerryLog.VARIANT0).withSuffix("Wood").build());
        ModelLoader.setCustomStateMapper(log1,
                (new StateMap.Builder()).withName(BlockBerryLog.VARIANT4).withSuffix("Wood").build());

        ModelLoader.setCustomStateMapper(plank0,
                (new StateMap.Builder()).withName(BlockBerryWood.VARIANT).withSuffix("Plank").build());

        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:pechaPlank"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:oranPlank"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:leppaPlank"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:sitrusPlank"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:enigmaPlank"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(plank0), new ResourceLocation("pokecube:nanabPlank"));
        registerItemTexture(Item.getItemFromBlock(plank0), 0,
                new ModelResourceLocation("pokecube:pechaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 1,
                new ModelResourceLocation("pokecube:oranPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 2,
                new ModelResourceLocation("pokecube:leppaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 3,
                new ModelResourceLocation("pokecube:sitrusPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 4,
                new ModelResourceLocation("pokecube:enigmaPlank", "inventory"));
        registerItemTexture(Item.getItemFromBlock(plank0), 5,
                new ModelResourceLocation("pokecube:nanabPlank", "inventory"));

        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf0), new ResourceLocation("pokecube:pechaLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf0), 0,
                new ModelResourceLocation("pokecube:pechaLeaves", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf0), new ResourceLocation("pokecube:oranLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf0), 1,
                new ModelResourceLocation("pokecube:oranLeaves", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf0), new ResourceLocation("pokecube:leppaLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf0), 2,
                new ModelResourceLocation("pokecube:leppaLeaves", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf0), new ResourceLocation("pokecube:sitrusLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf0), 3,
                new ModelResourceLocation("pokecube:sitrusLeaves", "inventory"));

        ModelBakery.registerItemVariants(Item.getItemFromBlock(log0), new ResourceLocation("pokecube:pechaWood"));
        registerItemTexture(Item.getItemFromBlock(log0), 0,
                new ModelResourceLocation("pokecube:pechaWood", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(log0), new ResourceLocation("pokecube:oranWood"));
        registerItemTexture(Item.getItemFromBlock(log0), 1,
                new ModelResourceLocation("pokecube:oranWood", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(log0), new ResourceLocation("pokecube:leppaWood"));
        registerItemTexture(Item.getItemFromBlock(log0), 2,
                new ModelResourceLocation("pokecube:leppaWood", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(log0), new ResourceLocation("pokecube:sitrusWood"));
        registerItemTexture(Item.getItemFromBlock(log0), 3,
                new ModelResourceLocation("pokecube:sitrusWood", "inventory"));

        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf1), new ResourceLocation("pokecube:enigmaLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf1), 0,
                new ModelResourceLocation("pokecube:enigmaLeaves", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(leaf1), new ResourceLocation("pokecube:nanabLeaves"));
        registerItemTexture(Item.getItemFromBlock(leaf1), 1,
                new ModelResourceLocation("pokecube:nanabLeaves", "inventory"));

        ModelBakery.registerItemVariants(Item.getItemFromBlock(log1), new ResourceLocation("pokecube:enigmaWood"));
        registerItemTexture(Item.getItemFromBlock(log1), 0,
                new ModelResourceLocation("pokecube:enigmaWood", "inventory"));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(log1), new ResourceLocation("pokecube:nanabWood"));
        registerItemTexture(Item.getItemFromBlock(log1), 1,
                new ModelResourceLocation("pokecube:nanabWood", "inventory"));

        for (String ident : BerryPlantManager.cropsToRegister.keySet())
        {
            Block crop = BerryPlantManager.cropsToRegister.get(ident);
            map = (new StateMap.Builder()).ignore(new IProperty[] { BlockCrops.AGE }).withSuffix("").build();
            registerItemTexture(Item.getItemFromBlock(crop), 0, new ModelResourceLocation(ident, "inventory"));
            ModelLoader.setCustomStateMapper(crop, map);
        }

        ItemTextureHandler.registerMegaStoneItemModels();

    }

    @Override
    public void registerKeyBindings()
    {
        ClientRegistry.registerKeyBinding(nextMob = new KeyBinding("Next Pokemob", Keyboard.KEY_RIGHT, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(previousMob = new KeyBinding("Previous Pokemob", Keyboard.KEY_LEFT, "Pokecube"));
        ClientRegistry.registerKeyBinding(nextMove = new KeyBinding("Next Move", Keyboard.KEY_DOWN, "Pokecube"));
        ClientRegistry.registerKeyBinding(previousMove = new KeyBinding("Previous Move", Keyboard.KEY_UP, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobBack = new KeyBinding("Pokemob Back", Keyboard.KEY_R, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobAttack = new KeyBinding("Pokemob Attack", Keyboard.KEY_G, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(mobStance = new KeyBinding("Pokemob Stance", Keyboard.KEY_BACKSLASH, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobMegavolve = new KeyBinding("Mega Evolve", Keyboard.KEY_M, "Pokecube"));

        ClientRegistry.registerKeyBinding(mobMove1 = new KeyBinding("Move 1", Keyboard.KEY_Y, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobMove2 = new KeyBinding("Move 2", Keyboard.KEY_U, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobMove3 = new KeyBinding("Move 3", Keyboard.KEY_H, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobMove4 = new KeyBinding("Move 4", Keyboard.KEY_J, "Pokecube"));
    }

    @Override
    public void registerPokecubeRenderer(int cubeId, Render renderer, Object mod)
    {
        if (!RenderPokecube.pokecubeRenderers.containsKey(cubeId))
        {
            RenderPokecube.pokecubeRenderers.put(cubeId, renderer);
            cubeRenders.put(cubeId, mod);
        }
        else
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            if (annotation.modid().equals(PokecubeMod.defaultMod))
            {
                RenderPokecube.pokecubeRenderers.put(cubeId, renderer);
                cubeRenders.put(cubeId, mod);
            }
        }
    }

    /** Used to register a model for the pokemob
     * 
     * @param nb
     *            - the pokedex number
     * @param model
     *            - the model */
    @Override
    public void registerPokemobModel(int nb, ModelBase model, Object mod)
    {
        registerPokemobModel(Database.getEntry(nb).getName(), model, mod);
    }

    @Override
    public void registerPokemobModel(String name, ModelBase model, Object mod)
    {
        if (Database.getEntry(name) == null)
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addModel(name + annotation.modid(), model);
        }
        else
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addModel(Database.getEntry(name) + annotation.modid(), model);
            int number = Database.getEntry(name).getPokedexNb();
            if (models.get(number))
            {
                String modid = annotation.modid();
                if (!modid.equals(PokecubeMod.defaultMod)) return;
            }
            models.set(number);
        }
    }

    /** Used to register a custom renderer for the pokemob
     * 
     * @param nb
     *            - the pokedex number
     * @param renderer
     *            - the renderer */
    @Override
    public void registerPokemobRenderer(int nb, Render renderer, Object mod)
    {
        Mod annotation = mod.getClass().getAnnotation(Mod.class);
        RenderPokemobs.addCustomRenderer(Database.getEntry(nb).getName() + annotation.modid(), renderer);
    }

    @Override
    public void registerPokemobRenderer(String name, Render renderer, Object mod)
    {
        if (Database.getEntry(name) == null)
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addCustomRenderer(name + annotation.modid(), renderer);
        }
        else
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addCustomRenderer(Database.getEntry(name).getName() + annotation.modid(), renderer);
        }
    }

    @Override
    public void registerRenderInformation()
    {
        super.registerRenderInformation();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPokecubeTable.class, new RenderPokecubeTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTradingTable.class, new RenderTradingTable());

        MinecraftForge.EVENT_BUS.register(new GuiDisplayPokecubeInfo());
        MinecraftForge.EVENT_BUS.register(new GuiTeleport());
        new GuiInfoMessages();
    }

    @Override
    public void spawnParticle(String par1Str, Vector3 location, Vector3 velocity)
    {
        if (velocity == null) velocity = Vector3.empty;
        IParticle particle = ParticleFactory.makeParticle(par1Str, velocity);
        ParticleHandler.Instance().addParticle(location, particle);
    }
}
