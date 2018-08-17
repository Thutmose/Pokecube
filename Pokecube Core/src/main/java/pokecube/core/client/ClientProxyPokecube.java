/**
 *
 */
package pokecube.core.client;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
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
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.blocks.pokecubeTable.TileEntityPokecubeTable;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.TileEntityTMMachine;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.client.gui.GuiChooseFirstPokemob;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.client.gui.blocks.GuiHealTable;
import pokecube.core.client.gui.blocks.GuiPC;
import pokecube.core.client.gui.blocks.GuiTMCreator;
import pokecube.core.client.gui.blocks.GuiTradingTable;
import pokecube.core.client.gui.pokemob.GuiPokemobAI;
import pokecube.core.client.gui.pokemob.GuiPokemobRoutes;
import pokecube.core.client.gui.pokemob.GuiPokemobStorage;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.models.ModelPokemobEgg;
import pokecube.core.client.render.RenderMoves;
import pokecube.core.client.render.blocks.RenderPokecubeTable;
import pokecube.core.client.render.blocks.RenderTradingTable;
import pokecube.core.client.render.entity.RenderPokecube;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.client.render.entity.RenderProfessor;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.SyncConfig;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.CapabilityAnimation;
import thut.core.client.render.particle.IParticle;
import thut.core.client.render.particle.ParticleFactory;
import thut.core.client.render.particle.ParticleHandler;
import thut.core.common.config.Configure;

@SideOnly(Side.CLIENT)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientProxyPokecube extends CommonProxyPokecube
{
    private static BitSet                     models      = new BitSet();
    static boolean                            init        = true;
    static boolean                            first       = true;

    public static KeyBinding                  nextMob;
    public static KeyBinding                  nextMove;
    public static KeyBinding                  previousMob;
    public static KeyBinding                  previousMove;
    public static KeyBinding                  mobBack;
    public static KeyBinding                  mobAttack;
    public static KeyBinding                  mobStance;
    public static KeyBinding                  mobMegavolve;
    public static KeyBinding                  noEvolve;
    public static KeyBinding                  mobMove1;
    public static KeyBinding                  mobMove2;
    public static KeyBinding                  mobMove3;
    public static KeyBinding                  mobMove4;
    public static KeyBinding                  mobUp;
    public static KeyBinding                  mobDown;
    public static KeyBinding                  throttleUp;
    public static KeyBinding                  throttleDown;
    public static KeyBinding                  arrangeGui;

    private HashMap<ResourceLocation, Object> cubeRenders = new HashMap<ResourceLocation, Object>();

    public ClientProxyPokecube()
    {
        if (first)
        {
            EventsHandlerClient hndlr = new EventsHandlerClient();
            MinecraftForge.EVENT_BUS.register(hndlr);
        }
        first = false;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);

        if (guiID == Config.GUIPOKECENTER_ID) { return new GuiHealTable(player.inventory); }
        if (guiID == Config.GUIDISPLAYPOKECUBEINFO_ID) { return null; }
        if (guiID == Config.GUIDISPLAYTELEPORTINFO_ID) { return null; }
        Entity entityHit = Tools.getPointedEntity(player, 16);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
        if (guiID == Config.GUIPOKEDEX_ID)
        {
            if (pokemob != null) return new GuiPokedex(pokemob, player);
            return new GuiPokedex(null, player);
        }
        if (guiID == Config.GUIPOKEWATCH_ID) { return new GuiPokeWatch(player, pokemob == null ? -1 : 1); }
        if (guiID == Config.GUIPOKEMOB_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new GuiPokemob(player.inventory, e);
        }
        if (guiID == Config.GUIPOKEMOBAI_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new GuiPokemobAI(player.inventory, e);
        }
        if (guiID == Config.GUIPOKEMOBSTORE_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new GuiPokemobStorage(player.inventory, e);
        }
        if (guiID == Config.GUIPOKEMOBROUTE_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new GuiPokemobRoutes(player.inventory, e);
        }
        if (guiID == Config.GUITRADINGTABLE_ID)
        {
            TileEntityTradingTable tile = (TileEntityTradingTable) world.getTileEntity(pos);
            return new GuiTradingTable(player.inventory, tile);
        }
        if (guiID == Config.GUITMTABLE_ID)
        {
            TileEntityTMMachine tile = (TileEntityTMMachine) world.getTileEntity(pos);
            return new GuiTMCreator(new ContainerTMCreator(tile, player.inventory));
        }
        if (guiID == Config.GUIPC_ID)
        {
            TileEntityPC tile = (TileEntityPC) world.getTileEntity(pos);
            ContainerPC pc = new ContainerPC(player.inventory, tile);
            return new GuiPC(pc);
        }

        if (guiID == Config.GUICHOOSEFIRSTPOKEMOB_ID) { return new GuiChooseFirstPokemob(null); }
        return null;
    }

    @Override
    public String getFolderName()
    {
        if (FMLClientHandler.instance().getClient().world != null)
            return FMLClientHandler.instance().getClient().world.provider.getSaveFolder();
        return "";
    }

    @Override
    public IThreadListener getMainThreadListener()
    {
        if (isOnClientSide()) { return Minecraft.getMinecraft(); }
        return super.getMainThreadListener();
    }

    @Override
    public ISnooperInfo getMinecraftInstance()
    {
        if (isOnClientSide()) { return Minecraft.getMinecraft(); }
        return super.getMinecraftInstance();
    }

    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null) { return super.getPlayer(playerName); }
        return Minecraft.getMinecraft().player;
    }

    @Override
    public World getWorld()
    {
        if (FMLCommonHandler.instance()
                .getEffectiveSide() == Side.CLIENT) { return FMLClientHandler.instance().getWorldClient(); }
        return super.getWorld();
    }

    @Override
    public void initItemModels()
    {
        OBJLoader.INSTANCE.addDomain(PokecubeMod.ID.toLowerCase(java.util.Locale.ENGLISH));
        Item item2 = Item.getItemFromBlock(PokecubeItems.tableBlock);
        ModelLoader.setCustomModelResourceLocation(item2, 0,
                new ModelResourceLocation(PokecubeMod.ID + ":pokecube_table", "inventory"));
    }

    @Override
    public void initBlockModels()
    {
        StateMap map;
        map = (new StateMap.Builder())
                .ignore(new IProperty[] { BerryManager.type, BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE }).build();
        ModelLoader.setCustomStateMapper(BerryManager.berryLeaf, map);
    }

    @Override
    public void initClient()
    {
        super.initClient();
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor()
        {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex)
            {
                PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
                if (entry != null)
                {
                    int colour = entry.getType1().colour;
                    if (tintIndex == 0 || entry.getType2() == null) { return colour; }
                    colour = entry.getType2().colour;
                    return colour;
                }
                return 0xffff00;

            }
        }, PokecubeItems.pokemobEgg);
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public void toggleSound(SoundEvent sound, BlockPos location)
    {
        if (sound != null)
        {
            ISound old = Minecraft.getMinecraft().renderGlobal.mapSoundPositions.remove(location);
            if (old != null)
            {
                Minecraft.getMinecraft().getSoundHandler().stopSound(old);
            }
            else
            {
                ISound isound = new PositionedSoundRecord(sound.getRegistryName(), SoundCategory.RECORDS, 2, 1, true, 0,
                        AttenuationType.LINEAR, location.getX() + 0.5f, location.getY() + 0.5f, location.getZ() + 0.5f);
                Minecraft.getMinecraft().getSoundHandler().playSound(isound);
                Minecraft.getMinecraft().renderGlobal.mapSoundPositions.put(location, isound);
            }
        }
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
        RenderingRegistry.registerEntityRenderingHandler(EntityMoveUse.class, new IRenderFactory<EntityMoveUse>()
        {
            @Override
            public Render<? super EntityMoveUse> createRenderFor(RenderManager manager)
            {
                return new RenderMoves(manager);
            }
        });
    }

    @Override
    public void registerKeyBindings()
    {
        ClientRegistry.registerKeyBinding(nextMob = new KeyBinding("key.pokemob.next", Keyboard.KEY_RIGHT, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(previousMob = new KeyBinding("key.pokemob.prev", Keyboard.KEY_LEFT, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(nextMove = new KeyBinding("key.pokemob.move.next", Keyboard.KEY_DOWN, "Pokecube"));
        ClientRegistry.registerKeyBinding(
                previousMove = new KeyBinding("key.pokemob.move.prev", Keyboard.KEY_UP, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobBack = new KeyBinding("key.pokemob.recall", Keyboard.KEY_R, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobAttack = new KeyBinding("key.pokemob.attack", Keyboard.KEY_G, "Pokecube"));
        ClientRegistry.registerKeyBinding(
                mobStance = new KeyBinding("key.pokemob.stance", Keyboard.KEY_BACKSLASH, "Pokecube"));
        ClientRegistry.registerKeyBinding(
                mobMegavolve = new KeyBinding("key.pokemob.megaevolve", Keyboard.KEY_M, "Pokecube"));
        ClientRegistry.registerKeyBinding(noEvolve = new KeyBinding("key.pokemob.b", Keyboard.KEY_B, "Pokecube"));

        ClientRegistry
                .registerKeyBinding(mobMove1 = new KeyBinding("key.pokemob.move.1", Keyboard.KEY_NONE, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(mobMove2 = new KeyBinding("key.pokemob.move.2", Keyboard.KEY_NONE, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(mobMove3 = new KeyBinding("key.pokemob.move.3", Keyboard.KEY_NONE, "Pokecube"));
        ClientRegistry
                .registerKeyBinding(mobMove4 = new KeyBinding("key.pokemob.move.4", Keyboard.KEY_NONE, "Pokecube"));

        ClientRegistry.registerKeyBinding(mobUp = new KeyBinding("key.pokemob.up", Keyboard.KEY_NONE, "Pokecube"));
        ClientRegistry.registerKeyBinding(mobDown = new KeyBinding("key.pokemob.down", Keyboard.KEY_NONE, "Pokecube"));

        ClientRegistry
                .registerKeyBinding(throttleUp = new KeyBinding("key.pokemob.speed.up", Keyboard.KEY_NONE, "Pokecube"));
        ClientRegistry.registerKeyBinding(
                throttleDown = new KeyBinding("key.pokemob.speed.down", Keyboard.KEY_NONE, "Pokecube"));

        ClientRegistry.registerKeyBinding(
                arrangeGui = new KeyBinding("key.pokemob.arrangegui", Keyboard.KEY_NONE, "Pokecube"));
    }

    @Override
    public void registerPokecubeRenderer(ResourceLocation cubeId, Render renderer, Object mod)
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

    @Override
    public void registerPokemobModel(String name, ModelBase model, Object mod)
    {
        if (Database.getEntry(name) == null)
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addModel(name.toLowerCase(Locale.ENGLISH) + annotation.modid(), model);
        }
        else
        {
            Mod annotation = mod.getClass().getAnnotation(Mod.class);
            RenderPokemobs.addModel(Database.getEntry(name).getName().toLowerCase(Locale.ENGLISH) + annotation.modid(),
                    model);
            int number = Database.getEntry(name).getPokedexNb();
            if (models.get(number))
            {
                String modid = annotation.modid();
                if (!modid.equals(PokecubeMod.defaultMod)) return;
            }
            models.set(number);
        }
    }

    @Override
    public void registerPokemobRenderer(String name, IRenderFactory renderer, Object mod)
    {
        if (Database.getEntry(name) == null)
        {
            PokecubeMod.log(Level.SEVERE, "Attempted to register renderer for unknown mob: " + name,
                    new IllegalArgumentException());
        }
        else
        {
            PokedexEntry entry = Database.getEntry(name);
            Class<? extends Entity> c = PokecubeCore.instance.getEntityClassForEntry(entry);

            if (PokecubeMod.debug) PokecubeMod.log("Registering Renderer for " + entry + " " + name + " " + c + " "
                    + renderer.createRenderFor(Minecraft.getMinecraft().getRenderManager()));

            /** Register this for when the rendermanager is refreshed */
            RenderingRegistry.registerEntityRenderingHandler(c, renderer);
            /** Register this here for when just updating renderer at runtime
             * (say from reloading models) */
            Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(c,
                    renderer.createRenderFor(Minecraft.getMinecraft().getRenderManager()));
        }
    }

    @Override
    public void registerClass(Class<? extends EntityLiving> clazz, PokedexEntry entry)
    {
        super.registerClass(clazz, entry);
        // Register the pokemob class as having animations
        CapabilityAnimation.registerAnimateClass(clazz);
    }

    @Override
    public void registerRenderInformation()
    {
        super.registerRenderInformation();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPokecubeTable.class, new RenderPokecubeTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTradingTable.class, new RenderTradingTable());
        MinecraftForge.EVENT_BUS.register(new GuiDisplayPokecubeInfo());
        new GuiInfoMessages();
    }

    @Override
    public void spawnParticle(World world, String par1Str, Vector3 location, Vector3 velocity, int... args)
    {
        if (world == null || Minecraft.getMinecraft().player == null) return;
        if (world.provider.getDimension() != Minecraft.getMinecraft().player.dimension) return;
        if (velocity == null) velocity = Vector3.empty;
        IParticle particle2 = ParticleFactory.makeParticle(par1Str, location, velocity, args);
        ParticleHandler.Instance().addParticle(location, particle2);
    }

    @Override
    public StatisticsManager getManager(UUID player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) return super.getManager(player);
        if (player == null || player.equals(Minecraft.getMinecraft().player.getUniqueID()))
            return Minecraft.getMinecraft().player.getStatFileWriter();
        return super.getManager(player);
    }

    @Override
    public void handshake(boolean revert)
    {
        setValues(revert);
        if (revert)
        {
            ((PokecubeCore) PokecubeMod.core).currentConfig = ((PokecubeCore) PokecubeMod.core).config;
        }
        else if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            ((PokecubeCore) PokecubeMod.core).currentConfig = ((PokecubeCore) PokecubeMod.core).config_client;
        }
    }

    private void setValues(boolean revert)
    {
        Config config = ((PokecubeCore) PokecubeMod.core).config;
        Config config_client = ((PokecubeCore) PokecubeMod.core).config_client;
        for (Field field : Config.class.getDeclaredFields())
        {
            SyncConfig c = field.getAnnotation(SyncConfig.class);
            Configure conf = field.getAnnotation(Configure.class);
            /** client stuff doesn't need to by synced, clients will use the
             * dummy config while on servers. */
            if (conf != null && c == null)
            {
                try
                {
                    if (revert)
                    {
                        config.updateField(field, field.get(config_client));
                    }
                    else
                    {
                        config_client.updateField(field, field.get(config));
                    }
                }
                catch (Exception e)
                {
                    // PokecubeMod.log(Level.SEVERE, "Error copying " +
                    // field.getName(), e);
                }
            }
        }
    }

    public EntityPlayer getPlayer(UUID player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) return super.getPlayer(player);
        if (Minecraft.getMinecraft().player != null
                && (player == null || player.equals(Minecraft.getMinecraft().player.getUniqueID())))
            return Minecraft.getMinecraft().player;
        return super.getPlayer(player);
    }
}
