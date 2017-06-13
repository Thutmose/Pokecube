package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.TerrainDamageSource;
import pokecube.core.moves.TerrainDamageSource.TerrainType;
import thut.api.maths.Vector3;

/** Manages interactions with materials for the pokemob. This is what is used to
 * make some mobs despawn in high light, or take damage from certain
 * materials. */
public class LogicInMaterials extends LogicBase
{
    final private EntityAnimal entity;
    final IPokemob             pokemob;
    final PokedexEntry         entry;
    Vector3                    v = Vector3.getNewVector();

    public LogicInMaterials(EntityAnimal entity)
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
        if (pokemob.getPokedexEntry().hatedMaterial != null)
        {
            String material = pokemob.getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("light"))
            {
                float value = 0.5f;
                if (entity.getEntityWorld().isDaytime() && !entity.getEntityWorld().isRemote
                        && !pokemob.getPokemonAIState(IMoveConstants.TAMED))
                {

                    value = Float.parseFloat(pokemob.getPokedexEntry().hatedMaterial[1]);
                    String action = pokemob.getPokedexEntry().hatedMaterial[2];
                    float f = entity.getBrightness();
                    if (f > value && entity.getEntityWorld().canSeeSky(entity.getPosition()))
                    {
                        if (action.equalsIgnoreCase("despawn"))
                        {
                            entity.setDead();
                        }
                        else if (action.equalsIgnoreCase("hurt") && Math.random() < 0.1)
                        {
                            entity.attackEntityFrom(DamageSource.ON_FIRE, 1);
                        }
                    }
                }
            }
            else if (material.equalsIgnoreCase("water"))
            {
                if (entity.isInWater() && entity.getRNG().nextInt(10) == 0)
                {
                    entity.attackEntityFrom(new TerrainDamageSource("material", TerrainType.MATERIAL), 1);
                }
            }
        }
    }

    @Override
    public void doLogic()
    {
    }
}
