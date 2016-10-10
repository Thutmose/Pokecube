package pokecube.core.ai.thread.logicRunnables;

import java.util.Calendar;
import java.util.Random;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class LogicMiscUpdate extends LogicBase
{
    private int                lastHadTargetTime = 0;
    private int[]              flavourAmounts    = new int[5];
    final private EntityAnimal entity;
    final IPokemob             pokemob;
    PokedexEntry               entry;
    String                     particle          = null;
    int                        particleIntensity = 80;
    int                        particleCounter   = 0;
    Vector3                    v                 = Vector3.getNewVector();

    public LogicMiscUpdate(EntityAnimal entity)
    {
        super((IPokemob) entity);
        this.entity = entity;
        pokemob = (IPokemob) entity;
        entry = pokemob.getPokedexEntry();
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        Random rand = new Random(pokemob.getRNGValue());
        // check shearable state, this is to refresh to clients if needed.
        if (entity.ticksExisted % 20 == rand.nextInt(20))
        {
            ((IShearable) pokemob).isShearable(null, entity.getEntityWorld(), entity.getPosition());
        }
        // If angry and has no target, make it not angry.
        if (pokemob.getPokemonAIState(IMoveConstants.ANGRY) && entity.getAttackTarget() == null)
        {
            pokemob.setPokemonAIState(ANGRY, false);
        }
        else if (entity.getAttackTarget() != null)
        {
            lastHadTargetTime = 100;
        }
        // If not angry, decrement last had target time, and if that is 0 or
        // less, reset to no stat modifiers.
        if (!pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            lastHadTargetTime--;
            if (lastHadTargetTime <= 0)
            {
                pokemob.setModifiers(PokecubeSerializer.intAsModifierArray(1717986918));
            }
        }

        // Particle stuff below here, WARNING, RESETTING RNG HERE
        rand = new Random();
        Vector3 particleLoc = Vector3.getNewVector().set(pokemob);
        if (pokemob.isShadow())
        {
            particle = "portal";
            particleIntensity = 100;
        }
        else if (particle == null && pokemob.getPokedexEntry().particleData != null)
        {
            particle = pokemob.getPokedexEntry().particleData[0];
            particleIntensity = Integer.parseInt(pokemob.getPokedexEntry().particleData[1]);
        }
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_MONTH) == 25 && calendar.get(Calendar.MONTH) == 11)
        {
            particle = "aurora";// Merry Xmas
            particleIntensity = 90;
        }
        if (pokemob.getPokemonAIState(IPokemob.MATING) && entity.ticksExisted % 10 == 0)
        {
            // System.out.println(pokemob);
            for (int i = 0; i < 3; ++i)
            {
                particleLoc.set(entity.posX + rand.nextFloat() * entity.width * 2.0F - entity.width,
                        entity.posY + 0.5D + rand.nextFloat() * entity.height,
                        entity.posZ + rand.nextFloat() * entity.width * 2.0F - entity.width);
                PokecubeMod.core.spawnParticle(entity.worldObj, "heart", particleLoc, null);
            }
        }
        for (int i = 0; i < 5; i++)
        {
            flavourAmounts[i] = pokemob.getFlavourAmount(i);
        }
        if (flavourAmounts[SWEET] > 0)
        {
            particle = "powder.pink";
        }
        if (flavourAmounts[BITTER] > 0)
        {
            particle = "powder.green";
        }
        if (flavourAmounts[SPICY] > 0)
        {
            particle = "powder.red";
        }
        if (flavourAmounts[DRY] > 0)
        {
            particle = "powder.blue";
        }
        if (flavourAmounts[SOUR] > 0)
        {
            particle = "powder.yellow";
        }
        for (int i = 0; i < flavourAmounts.length; i++)
        {
            if (flavourAmounts[i] > 0)
            {
                pokemob.setFlavourAmount(i, flavourAmounts[i] - 1);
            }
        }
        if (particle != null && particleCounter++ >= 100 - particleIntensity)
        {
            float scale = entity.width * 2;
            Vector3 offset = Vector3.getNewVector().set(rand.nextDouble() - 0.5, rand.nextDouble() + entity.height / 2,
                    rand.nextDouble() - 0.5);
            offset.scalarMultBy(scale);
            particleLoc.addTo(offset);
            PokecubeMod.core.spawnParticle(entity.worldObj, particle, particleLoc, null);
            particleCounter = 0;
        }
        particle = null;
    }

    @Override
    public void doLogic()
    {
    }
}
