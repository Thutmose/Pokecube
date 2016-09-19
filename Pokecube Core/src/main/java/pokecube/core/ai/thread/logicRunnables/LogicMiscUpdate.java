package pokecube.core.ai.thread.logicRunnables;

import java.util.Random;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class LogicMiscUpdate extends LogicBase
{
    private int                lastHadTargetTime = 0;
    final private EntityAnimal entity;
    final IPokemob             pokemob;
    PokedexEntry               entry;
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
        if (entity.getHealth() < 1)
        {
            entity.attackEntityFrom(DamageSource.generic, 100);
        }
        if (entity.ticksExisted % 20 == rand.nextInt(20))
        {
            ((IShearable) pokemob).isShearable(null, entity.getEntityWorld(), entity.getPosition());
        }
        if (pokemob.getPokemonAIState(IMoveConstants.ANGRY) && entity.getAttackTarget() == null)
        {
            pokemob.setPokemonAIState(ANGRY, false);
        }
        else if (entity.getAttackTarget() != null)
        {
            lastHadTargetTime = 100;
        }
        if (!pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            lastHadTargetTime--;
            if (lastHadTargetTime == 0)
            {
                pokemob.setModifiers(PokecubeSerializer.intAsModifierArray(1717986918));
            }
        }
    }

    @Override
    public void doLogic()
    {
    }
}
