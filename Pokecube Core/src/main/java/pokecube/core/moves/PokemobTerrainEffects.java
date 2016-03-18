package pokecube.core.moves;

import static pokecube.core.utils.PokeType.ground;
import static pokecube.core.utils.PokeType.ice;
import static pokecube.core.utils.PokeType.poison;
import static pokecube.core.utils.PokeType.rock;
import static pokecube.core.utils.PokeType.steel;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment.ITerrainEffect;

public class PokemobTerrainEffects implements ITerrainEffect
{
    public static final int EFFECT_WEATHER_SAND     = 1;
    public static final int EFFECT_WEATHER_RAIN     = 2;
    public static final int EFFECT_WEATHER_HAIL     = 3;
    public static final int EFFECT_WEATHER_SUN      = 4;
    public static final int EFFECT_SPORT_MUD        = 5;
    public static final int EFFECT_SPORT_WATER      = 6;
    public static final int EFFECT_TERRAIN_GRASS    = 7;
    public static final int EFFECT_TERRAIN_ELECTRIC = 8;
    public static final int EFFECT_TERRAIN_MISTY    = 9;
    public static final int EFFECT_MIST             = 10;
    public static final int EFFECT_SPIKES           = 11;
    public static final int EFFECT_ROCKS            = 12;
    public static final int EFFECT_POISON           = 13;
    public static final int EFFECT_POISON2          = 14;
    public static final int EFFECT_WEBS             = 15;

    public static final int CLEAR_ENTRYEFFECTS = 16;

    public final long[] effects = new long[16];

    int chunkX;
    int chunkZ;
    int chunkY;

    Set<IPokemob> pokemon = new HashSet<IPokemob>();

    public PokemobTerrainEffects()
    {
    }

    public void addPokemon(IPokemob poke)
    {
        if (!pokemon.contains(poke)) pokemon.add(poke);
    }

    @Override
    public void bindToTerrain(int x, int y, int z)
    {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
    }

    public int countPokemon()
    {
        return pokemon.size();
    }

    public void doEffect(EntityLivingBase entity)
    {
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            if (effects[EFFECT_WEATHER_HAIL] > 0 && !mob.isType(ice))
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(DamageSource.generic, damage);
            }
            if (effects[EFFECT_WEATHER_SAND] > 0 && !(mob.isType(rock) || mob.isType(steel) || mob.isType(ground)))
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(DamageSource.generic, damage);// TODO
                                                                      // terrain
                                                                      // damage
                                                                      // type
            }
            if (effects[EFFECT_TERRAIN_ELECTRIC] > 0 && mob.getOnGround())
            {
                if (mob.getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();
            }
            if (effects[EFFECT_TERRAIN_GRASS] > 0 && mob.getOnGround())
            {
                float thisHP = entity.getHealth();
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
            if (effects[EFFECT_TERRAIN_MISTY] > 0 && mob.getOnGround())
            {
                if (mob.getStatus() != IMoveConstants.STATUS_NON) mob.healStatus();
            }
        }
        else if (PokecubeMod.pokemobsDamagePlayers)
        {
            if (effects[EFFECT_WEATHER_HAIL] > 0)
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(DamageSource.generic, damage);
            }
            if (effects[EFFECT_WEATHER_SAND] > 0)
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(DamageSource.generic, damage);// TODO
                                                                      // terrain
                                                                      // damage
                                                                      // type
            }
            if (effects[EFFECT_TERRAIN_GRASS] > 0 && entity.onGround)
            {
                float thisHP = entity.getHealth();
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
        }
        dropDurations(entity);
    }

    @Override
    public void doEffect(EntityLivingBase entity, boolean firstEntry)
    {
        if (firstEntry)
        {
            doEntryEffect(entity);
        }
        else
        {
            doEffect(entity);
        }
    }

    public void doEntryEffect(EntityLivingBase entity)
    {
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            if (effects[EFFECT_POISON] > 0 && !mob.isType(poison) && !mob.isType(steel))
            {
                mob.setStatus(IMoveConstants.STATUS_PSN);
            }
            if (effects[EFFECT_POISON2] > 0 && !mob.isType(poison) && !mob.isType(steel))
            {
                mob.setStatus(IMoveConstants.STATUS_PSN2);
            }
            if (effects[EFFECT_SPIKES] > 0)
            {
                float thisHP = entity.getHealth();
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
            if (effects[EFFECT_ROCKS] > 0)
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                double mult = PokeType.getAttackEfficiency(rock, mob.getType1(), mob.getType2());
                entity.attackEntityFrom(DamageSource.generic, (float) (damage * mult));
            }
            if (effects[EFFECT_WEBS] > 0 && mob.getOnGround())
            {
                MovesUtils.handleStats2(mob, null, IMoveConstants.VIT, IMoveConstants.FALL);
            }
        }
    }

    private void dropDurations(Entity e)
    {
        long time = e.worldObj.getTotalWorldTime();
        boolean send = false;
        for (int i = 0; i < effects.length; i++)
        {
            if (effects[i] > 0)
            {
                long diff = effects[i] - time;
                if (diff > 0)
                {
                    effects[i] = effects[i] - 1;
                }
                else
                {
                    effects[i] = 0;
                    send = true;
                }
            }
        }
        if (send)
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(73));
                buffer.writeByte(PokecubeClientPacket.TERRAINEFFECTS);
                buffer.writeInt(chunkX);
                buffer.writeInt(chunkY);
                buffer.writeInt(chunkZ);
                for (int i = 0; i < 16; i++)
                {
                    buffer.writeLong(effects[i]);
                }
                PokecubeClientPacket packet = new PokecubeClientPacket(buffer);
                Vector3 v = Vector3.getNewVector().set(e);
                PokecubePacketHandler.sendToAllNear(packet, v, e.worldObj.provider.getDimension(), 64);
            }
        }
    }

    public long getEffect(int effect)
    {
        return effects[effect];
    }

    boolean hasEffects()
    {
        boolean ret = false;
        for (int i = 1; i < 16; i++)
        {
            if (effects[i] > 0) return true;
        }
        return ret;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
    }

    public void removePokemon(IPokemob poke)
    {
        pokemon.remove(poke);
    }

    /** Adds the effect, and removes any non-compatible effects if any
     * 
     * @param effect
     *            see the EFFECT_ variables owned by this class
     * @param duration
     *            how long this effect lasts, this counter is decreased every
     *            time a pokemob uses a move. */
    public void setEffect(int effect, long duration)
    {
        if (effect == EFFECT_WEATHER_HAIL)
        {
            effects[EFFECT_WEATHER_RAIN] = 0;
            effects[EFFECT_WEATHER_SUN] = 0;
            effects[EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == EFFECT_WEATHER_SUN)
        {
            effects[EFFECT_WEATHER_RAIN] = 0;
            effects[EFFECT_WEATHER_HAIL] = 0;
            effects[EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == EFFECT_WEATHER_RAIN)
        {
            effects[EFFECT_WEATHER_HAIL] = 0;
            effects[EFFECT_WEATHER_SUN] = 0;
            effects[EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == EFFECT_WEATHER_SAND)
        {
            effects[EFFECT_WEATHER_RAIN] = 0;
            effects[EFFECT_WEATHER_SUN] = 0;
            effects[EFFECT_WEATHER_HAIL] = 0;
        }
        if (effect == EFFECT_TERRAIN_ELECTRIC)
        {
            effects[EFFECT_TERRAIN_GRASS] = effects[EFFECT_TERRAIN_MISTY] = 0;
        }
        if (effect == EFFECT_TERRAIN_GRASS)
        {
            effects[EFFECT_TERRAIN_ELECTRIC] = effects[EFFECT_TERRAIN_MISTY] = 0;
        }
        if (effect == EFFECT_TERRAIN_MISTY)
        {
            effects[EFFECT_TERRAIN_GRASS] = effects[EFFECT_TERRAIN_ELECTRIC] = 0;
        }
        if (effect == CLEAR_ENTRYEFFECTS)
        {
            effects[EFFECT_POISON] = effects[EFFECT_POISON2] = effects[EFFECT_SPIKES] = effects[EFFECT_ROCKS] = effects[EFFECT_WEBS] = 0;
        }
        else effects[effect] = duration;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {

    }
}
