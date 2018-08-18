package pokecube.core.moves;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.ai.thread.aiRunnables.combat.AIFindTarget;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.TerrainDamageSource.TerrainType;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment.ITerrainEffect;

public class PokemobTerrainEffects implements ITerrainEffect
{
    public static final int                 EFFECT_WEATHER_SAND     = 1;
    public static final int                 EFFECT_WEATHER_RAIN     = 2;
    public static final int                 EFFECT_WEATHER_HAIL     = 3;
    public static final int                 EFFECT_WEATHER_SUN      = 4;
    public static final int                 EFFECT_SPORT_MUD        = 5;
    public static final int                 EFFECT_SPORT_WATER      = 6;
    public static final int                 EFFECT_TERRAIN_GRASS    = 7;
    public static final int                 EFFECT_TERRAIN_ELECTRIC = 8;
    public static final int                 EFFECT_TERRAIN_MISTY    = 9;
    public static final int                 EFFECT_MIST             = 10;
    public static final int                 EFFECT_SPIKES           = 11;
    public static final int                 EFFECT_ROCKS            = 12;
    public static final int                 EFFECT_POISON           = 13;
    public static final int                 EFFECT_POISON2          = 14;
    public static final int                 EFFECT_WEBS             = 15;

    public static final int                 CLEAR_ENTRYEFFECTS      = 16;

    public static final TerrainDamageSource HAILDAMAGE              = new TerrainDamageSource("terrain.hail",
            TerrainType.TERRAIN);
    public static final TerrainDamageSource SANDSTORMDAMAGE         = new TerrainDamageSource("terrain.sandstorm",
            TerrainType.TERRAIN);

    public final long[]                     effects                 = new long[16];

    int                                     chunkX;
    int                                     chunkZ;
    int                                     chunkY;

    Set<IPokemob>                           pokemon                 = new HashSet<IPokemob>();

    public PokemobTerrainEffects()
    {
    }

    @Override
    public void bindToTerrain(int x, int y, int z)
    {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
    }

    public void doEffect(EntityLivingBase entity)
    {
        if (entity.getEntityWorld().getTotalWorldTime() % (2 * PokecubeMod.core.getConfig().attackCooldown) != 0)
            return;
        if (!AIFindTarget.validTargets.apply(entity)) return;
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (effects[EFFECT_WEATHER_HAIL] > 0 && !mob.isType(PokeType.getType("ice")))
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(HAILDAMAGE, damage);
            }
            if (effects[EFFECT_WEATHER_SAND] > 0 && !(mob.isType(PokeType.getType("rock"))
                    || mob.isType(PokeType.getType("steel")) || mob.isType(PokeType.getType("ground"))))
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(SANDSTORMDAMAGE, damage);
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
        else if (PokecubeMod.core.getConfig().pokemobsDamagePlayers)
        {
            if (effects[EFFECT_WEATHER_HAIL] > 0)
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(HAILDAMAGE, damage);
            }
            if (effects[EFFECT_WEATHER_SAND] > 0)
            {
                float thisMaxHP = entity.getMaxHealth();
                int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(SANDSTORMDAMAGE, damage);
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
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (effects[EFFECT_POISON] > 0 && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel")))
            {
                mob.setStatus(IMoveConstants.STATUS_PSN);
            }
            if (effects[EFFECT_POISON2] > 0 && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel")))
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
                double mult = PokeType.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob.getType2());
                entity.attackEntityFrom(DamageSource.GENERIC, (float) (damage * mult));
            }
            if (effects[EFFECT_WEBS] > 0 && mob.getOnGround())
            {
                MovesUtils.handleStats2(mob, null, IMoveConstants.VIT, IMoveConstants.FALL);
            }
        }
    }

    private void dropDurations(Entity e)
    {
        long time = e.getEntityWorld().getTotalWorldTime();
        boolean send = false;
        for (int i = 0; i < effects.length; i++)
        {
            if (effects[i] > 0)
            {
                long diff = effects[i] - time;
                if (diff < 0)
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
                PacketSyncTerrain.sendTerrainEffects(e, chunkX, chunkY, chunkZ, this);
            }
        }
    }

    public long getEffect(int effect)
    {
        return effects[effect];
    }

    public boolean hasEffects()
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

    @SideOnly(Side.CLIENT)
    public void renderTerrainEffects(RenderFogEvent event)
    {
        if (this.hasEffects())
        {
            int time = Minecraft.getMinecraft().player.ticksExisted;
            Vector3 direction = Vector3.getNewVector().set(0, -1, 0);
            float tick = (float) (time + event.getRenderPartialTicks()) / 2f;
            if (effects[EFFECT_WEATHER_RAIN] > 0)
            {
                GlStateManager.color(0, 0, 1, 1);
                renderEffect(direction, tick);
            }
            if (effects[EFFECT_WEATHER_HAIL] > 0)
            {
                GlStateManager.color(1, 1, 1, 1);
                renderEffect(direction, tick);
            }
            if (effects[EFFECT_WEATHER_SAND] > 0)
            {
                GlStateManager.color(220 / 255f, 209 / 255f, 192 / 255f, 1);
                direction.set(0, 0, 1);
                renderEffect(direction, tick);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderEffect(Vector3 direction, float tick)
    {
        GlStateManager.disableTexture2D();
        Vector3 temp = Vector3.getNewVector();
        Vector3 temp2 = Vector3.getNewVector();
        Random rand = new Random(Minecraft.getMinecraft().player.ticksExisted / 200);
        GlStateManager.translate(-direction.x * 8, -direction.y * 8, -direction.z * 8);
        for (int i = 0; i < 1000; i++)
        {
            GL11.glBegin(GL11.GL_QUADS);
            temp.set(rand.nextFloat() - 0.5, rand.nextFloat() - 0.5, rand.nextFloat() - 0.5);
            temp.scalarMultBy(16);
            temp.addTo(temp2.set(direction).scalarMultBy(tick));
            temp.y = temp.y % 16;
            temp.x = temp.x % 16;
            temp.z = temp.z % 16;
            double size = 0.02;
            GL11.glVertex3d(temp.x, temp.y + size, temp.z);
            GL11.glVertex3d(temp.x - size, temp.y - size, temp.z - size);
            GL11.glVertex3d(temp.x - size, temp.y + size, temp.z - size);
            GL11.glVertex3d(temp.x, temp.y - size, temp.z);
            GL11.glEnd();
        }
        GlStateManager.enableTexture2D();
    }

    @Override
    public String getIdenitifer()
    {
        return "pokemobEffects";
    }
}
