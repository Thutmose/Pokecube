package pokecube.core.ai.thread.logicRunnables;

import java.util.Calendar;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** Mostly does visuals updates, such as particle effects, checking that
 * shearing status is reset properly. It also resets stat modifiers when the mob
 * is out of combat. */
public class LogicMiscUpdate extends LogicBase
{
    public static int    EXITCUBEDURATION  = 40;
    private int          lastHadTargetTime = 0;
    private int[]        flavourAmounts    = new int[5];
    private PokedexEntry entry;
    private String       particle          = null;
    private int          particleIntensity = 80;
    private int          particleCounter   = 0;
    private boolean      reset             = false;
    private boolean      initHome          = false;
    private boolean      checkedEvol       = false;
    private int          pathTimer         = 0;
    Vector3              v                 = Vector3.getNewVector();

    public LogicMiscUpdate(IPokemob entity)
    {
        super(entity);
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        entry = pokemob.getPokedexEntry();
        Random rand = new Random(pokemob.getRNGValue());
        // check shearable state, this is to refresh to clients if needed.
        if (entity.ticksExisted % 20 == rand.nextInt(20) && entity instanceof IShearable)
        {
            ((IShearable) entity).isShearable(null, entity.getEntityWorld(), entity.getPosition());
        }

        if (!world.isRemote)
        {
            // Check that AI states are correct
            checkAIStates();
            // Check evolution
            checkEvolution();
            // Check and tick inventory
            checkInventory(world);

            // Randomly increase happiness for being outside of pokecube.
            if (Math.random() > 0.999 && pokemob.getPokemonAIState(IMoveConstants.TAMED))
            {
                HappinessType.applyHappiness(pokemob, HappinessType.TIME);
            }

            ItemStack pokecube = pokemob.getPokecube();
            ResourceLocation id = PokecubeItems.getCubeId(pokecube);
            PokecubeBehavior behaviour = IPokecube.BEHAVIORS.getValue(id);
            if (behaviour != null) behaviour.onUpdate(pokemob);
        }

        for (int i = 0; i < 5; i++)
        {
            flavourAmounts[i] = pokemob.getFlavourAmount(i);
        }
        for (int i = 0; i < flavourAmounts.length; i++)
        {
            if (flavourAmounts[i] > 0)
            {
                pokemob.setFlavourAmount(i, flavourAmounts[i] - 1);
            }
        }

        if (initHome)
        {
            initHome = false;
            if (pokemob.getHome() != null)
            {
                TileEntity te = world.getTileEntity(pokemob.getHome());
                if (te != null && te instanceof TileEntityNest)
                {
                    TileEntityNest nest = (TileEntityNest) te;
                    nest.addResident(pokemob);
                }
            }
        }

        if (!entity.getEntityWorld().isRemote) return;

        int id = pokemob.getTargetID();
        if (id >= 0 && entity.getAttackTarget() == null)
        {
            entity.setAttackTarget((EntityLivingBase) PokecubeMod.core.getEntityProvider().getEntity(world, id, false));
        }
        if (id < 0 && entity.getAttackTarget() != null)
        {
            entity.setAttackTarget(null);
        }

        // Particle stuff below here, WARNING, RESETTING RNG HERE
        rand = new Random();
        Vector3 particleLoc = Vector3.getNewVector().set(entity);
        boolean randomV = false;
        Vector3 particleVelo = Vector3.getNewVector();
        boolean pokedex = false;
        if (pokemob.isShadow())
        {
            particle = "portal";
            particleIntensity = 100;
        }
        particles:
        if (particle == null && entry.particleData != null)
        {
            pokedex = true;
            double intensity = Double.parseDouble(entry.particleData[1]);
            int val = (int) intensity;
            if (intensity < 1)
            {
                if (rand.nextDouble() <= intensity) val = 1;
            }
            if (val == 0) break particles;
            particle = entry.particleData[0];
            particleIntensity = val;
            if (entry.particleData.length > 2)
            {
                String[] args = entry.particleData[2].split(",");
                double dx = 0, dy = 0, dz = 0;
                if (args.length == 1)
                {
                    dy = Double.parseDouble(args[0]) * entity.height;
                }
                else
                {
                    dx = Double.parseDouble(args[0]);
                    dy = Double.parseDouble(args[1]);
                    dz = Double.parseDouble(args[2]);
                }
                particleLoc.addTo(dx, dy, dz);
            }
            if (entry.particleData.length > 3)
            {
                String[] args = entry.particleData[3].split(",");
                double dx = 0, dy = 0, dz = 0;
                if (args.length == 1)
                {
                    switch (args[0])
                    {
                    case "r":
                        randomV = true;
                        break;
                    case "v":
                        particleVelo.setToVelocity(entity);
                        break;
                    default:
                        break;
                    }
                }
                else
                {
                    dx = Double.parseDouble(args[0]);
                    dy = Double.parseDouble(args[1]);
                    dz = Double.parseDouble(args[2]);
                    particleVelo.set(dx, dy, dz);
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_MONTH) == 25 && calendar.get(Calendar.MONTH) == 11)
        {
            particle = "aurora";// Merry Xmas
            particleIntensity = 10;
        }
        if (pokemob.getPokemonAIState(IPokemob.MATING) && entity.ticksExisted % 10 == 0)
        {
            Vector3 heart = Vector3.getNewVector();
            for (int i = 0; i < 3; ++i)
            {
                heart.set(entity.posX + rand.nextFloat() * entity.width * 2.0F - entity.width,
                        entity.posY + 0.5D + rand.nextFloat() * entity.height,
                        entity.posZ + rand.nextFloat() * entity.width * 2.0F - entity.width);
                PokecubeMod.core.spawnParticle(entity.world, "heart", heart, null);
            }
        }
        int[] args = {};
        if (flavourAmounts[SWEET] > 0)
        {
            particle = "powder";
            args = new int[] { 0xF85888 };
        }
        if (flavourAmounts[BITTER] > 0)
        {
            particle = "powder";
            args = new int[] { 0x78C850 };
        }
        if (flavourAmounts[SPICY] > 0)
        {
            particle = "powder";
            args = new int[] { 0xFF0000 };
        }
        if (flavourAmounts[DRY] > 0)
        {
            particle = "powder";
            args = new int[] { 0x6890F0 };
        }
        if (flavourAmounts[SOUR] > 0)
        {
            particle = "powder";
            args = new int[] { 0x78C850 };
        }
        if (particle != null && particleCounter++ <= particleIntensity)
        {
            if (!pokedex)
            {
                float scale = entity.width * 2;
                Vector3 offset = Vector3.getNewVector().set(rand.nextDouble() - 0.5,
                        rand.nextDouble() + entity.height / 2, rand.nextDouble() - 0.5);
                offset.scalarMultBy(scale);
                particleLoc.addTo(offset);
            }
            if (randomV)
            {
                particleVelo.set(rand.nextDouble() - 0.5, rand.nextDouble() + entity.height / 2,
                        rand.nextDouble() - 0.5);
                particleVelo.scalarMultBy(0.25);
            }
            PokecubeMod.core.spawnParticle(entity.getEntityWorld(), particle, particleLoc, particleVelo, args);

        }
        particleCounter = 0;
        particle = null;
    }

    @Override
    public void doLogic()
    {
    }

    protected boolean getAIState(int state, int array)
    {
        return (array & state) != 0;
    }

    private void checkEvolution()
    {

        if (Tools.isSameStack(pokemob.getHeldItem(), PokecubeItems.getStack("everstone")))
        {
            pokemob.setTraded(false);
        }

        int num = pokemob.getEvolutionTicks();
        if (num > 0)
        {
            pokemob.setEvolutionTicks(pokemob.getEvolutionTicks() - 1);
        }
        if (!checkedEvol && pokemob.traded())
        {
            pokemob.evolve(true, false, pokemob.getHeldItem());
            checkedEvol = true;
            return;
        }
        boolean evolving = pokemob.getPokemonAIState(EVOLVING);
        if (evolving)
        {
            if (num <= 0)
            {
                pokemob.setPokemonAIState(EVOLVING, false);
                pokemob.setEvolutionTicks(-1);
            }
            if (num <= 50)
            {
                pokemob.evolve(false, false, pokemob.getEvolutionStack());
                pokemob.setPokemonAIState(EVOLVING, false);
                pokemob.setEvolutionTicks(-1);
            }
        }
        if (PokecubeSerializer.getInstance().getPokemob(pokemob.getPokemonUID()) == null)
            PokecubeSerializer.getInstance().addPokemob(pokemob);
    }

    private void checkAIStates()
    {

        int state = pokemob.getTotalAIState();

        // If angry and has no target, make it not angry.
        if (getAIState(IPokemob.ANGRY, state) && entity.getAttackTarget() == null)
        {
            pokemob.setPokemonAIState(ANGRY, false);
        }
        else if (entity.getAttackTarget() != null)
        {
            lastHadTargetTime = 100;
            reset = false;
        }
        // If not angry, decrement last had target time, and if that is 0 or
        // less, reset to no stat modifiers.
        if (!pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            lastHadTargetTime--;
            if (lastHadTargetTime <= 0 && !reset)
            {
                reset = true;
                for (Stats stat : Stats.values())
                    pokemob.getModifiers().getDefaultMods().setModifier(stat, 0);
                pokemob.getMoveStats().reset();
            }
        }

        if (getAIState(IMoveConstants.TAMED, state) && (pokemob.getPokemonOwnerID() == null))
        {
            pokemob.setPokemonAIState(IMoveConstants.TAMED, false);
        }
        if (pokemob.getLoveTimer() > 600)
        {
            pokemob.resetLoveStatus();
        }
        if (entity.ticksExisted > EXITCUBEDURATION && getAIState(EXITINGCUBE, state))
        {
            pokemob.setPokemonAIState(EXITINGCUBE, false);
        }
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING) && !entity.getNavigator().noPath())
        {
            entity.getNavigator().clearPathEntity();
        }
        if (pokemob.getPokemonAIState(IMoveConstants.PATHING) && entity.getNavigator().noPath() && pathTimer++ > 10)
        {
            pokemob.setPokemonAIState(IMoveConstants.PATHING, false);
            pathTimer = 0;
        }
    }

    private void checkInventory(World world)
    {
        for (int i = 0; i < pokemob.getPokemobInventory().getSizeInventory(); i++)
        {
            ItemStack stack;
            if (CompatWrapper.isValid(stack = pokemob.getPokemobInventory().getStackInSlot(i)))
            {
                stack.getItem().onUpdate(stack, world, entity, i, false);
            }
        }
    }
}
