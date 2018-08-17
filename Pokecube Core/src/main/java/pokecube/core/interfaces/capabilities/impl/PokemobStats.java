package pokecube.core.interfaces.capabilities.impl;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.LevelUpEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketSyncExp;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Matrix3;

public abstract class PokemobStats extends PokemobGenes
{
    private static final Method SETSIZE = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a",
            float.class, float.class);

    static
    {
        SETSIZE.setAccessible(true);
    }

    @Override
    public void addHappiness(int toAdd)
    {
        this.bonusHappiness += toAdd;
        this.dataManager.set(params.HAPPYDW, Integer.valueOf(bonusHappiness));
    }

    @Override
    public int getHappiness()
    {
        bonusHappiness = dataManager.get(params.HAPPYDW);
        bonusHappiness = Math.max(bonusHappiness, -getPokedexEntry().getHappiness());
        bonusHappiness = Math.min(bonusHappiness, 255 - getPokedexEntry().getHappiness());
        return bonusHappiness + getPokedexEntry().getHappiness();
    }

    @Override
    public StatModifiers getModifiers()
    {
        return modifiers;
    }

    @Override
    public String getPokemonNickname()
    {
        return getDataManager().get(params.NICKNAMEDW);
    }

    @Override
    public boolean isShadow()
    {
        boolean isShadow = getPokedexEntry().isShadowForme;
        if (isShadow && !wasShadow)
        {
            wasShadow = true;
        }
        return isShadow;
    }

    @Override
    public int getExp()
    {
        return getMoveStats().exp;
    }

    @Override
    public IPokemob setExp(int exp, boolean notifyLevelUp)
    {
        if (getEntity().isDead) return this;
        int old = getMoveStats().exp;
        getMoveStats().oldLevel = this.getLevel();
        int lvl100xp = Tools.maxXPs[getExperienceMode()];
        exp = Math.min(lvl100xp, exp);
        getMoveStats().exp = exp;
        int newLvl = Tools.xpToLevel(getExperienceMode(), exp);
        int oldLvl = Tools.xpToLevel(getExperienceMode(), old);
        IPokemob ret = this;
        if (oldLvl != newLvl)
        {
            // Fire event to allow others to interfere
            LevelUpEvent lvlup = new LevelUpEvent(this, newLvl, getMoveStats().oldLevel);
            MinecraftForge.EVENT_BUS.post(lvlup);
            if (!lvlup.isCanceled())
            {
                if (notifyLevelUp)
                {
                    updateHealth();
                    ItemStack held = getHeldItem();
                    if (!getEntity().isDead && (canEvolve(ItemStack.EMPTY) || canEvolve(held)))
                    {
                        levelUp(newLvl);
                        IPokemob evo = this.evolve(true, false, held);
                        if (evo != null) ret = evo;
                    }
                    ret.levelUp(newLvl);
                    if (getEntity().addedToChunk && ret.getPokemonOwner() instanceof EntityPlayer
                            && getEntity().getEntityWorld().getGameRules().getBoolean("doMobLoot")
                            && !getEntity().getEntityWorld().isRemote)
                    {
                        getEntity().getEntityWorld().spawnEntity(new EntityXPOrb(getEntity().getEntityWorld(),
                                getEntity().posX, getEntity().posY, getEntity().posZ, 1));
                    }
                }
            }
            else getMoveStats().exp = old;
        }
        PacketSyncExp.sendUpdate(ret);
        return ret;
    }

    @Override
    public void setPokemonNickname(String nickname)
    {
        if (!getEntity().isServerWorld())
        {
            if (!nickname.equals(getPokemonNickname()) && getEntity().addedToChunk)
            {
                PacketNickname.sendPacket(getEntity(), nickname);
            }
        }
        else
        {
            if (getPokedexEntry().getName().equals(nickname))
            {
                dataManager.set(params.NICKNAMEDW, "");
            }
            else
            {
                dataManager.set(params.NICKNAMEDW, nickname);
            }
        }
    }

    /** Returns 1st type.
     * 
     * @see PokeType
     * @return the byte type */
    @Override
    public PokeType getType1()
    {
        PokeType type = PokeType.getType(getDataManager().get(params.TYPE1DW));
        return type != PokeType.unknown ? type : getPokedexEntry().getType1();
    }

    /** Returns 2nd type.
     * 
     * @see PokeType
     * @return the byte type */
    @Override
    public PokeType getType2()
    {
        PokeType type = PokeType.getType(getDataManager().get(params.TYPE2DW));
        return type != PokeType.unknown ? type : getPokedexEntry().getType2();
    }

    @Override
    public void setType1(PokeType type1)
    {
        if (type1 == getType1()) return;
        String name = type1 == null || type1 == PokeType.unknown ? "" : type1.name;
        this.getDataManager().set(params.TYPE1DW, name);
    }

    @Override
    public void setType2(PokeType type2)
    {
        if (type2 == getType2()) return;
        String name = type2 == null || type2 == PokeType.unknown ? "" : type2.name;
        this.getDataManager().set(params.TYPE2DW, name);
    }

    @Override
    public int getRNGValue()
    {
        return personalityValue;
    }

    @Override
    public void setRNGValue(int value)
    {
        personalityValue = value;
    }

    @Override
    public IPokemob setForSpawn(int exp, boolean evolve)
    {
        int level = Tools.xpToLevel(getExperienceMode(), exp);
        getMoveStats().oldLevel = 0;
        getMoveStats().exp = exp;
        IPokemob ret = this.levelUp(level);
        ItemStack held = getHeldItem();
        if (evolve) while (ret.canEvolve(held))
        {
            IPokemob temp = ret.evolve(false, true, held);
            if (temp == null) break;
            ret = temp;
            ret.getMoveStats().exp = exp;
            ret.levelUp(level);
        }
        return ret;
    }

    @Override
    public void setSize(float size)
    {
        super.setSize(size);
        float a = 1, b = 1, c = 1;
        PokedexEntry entry = getPokedexEntry();
        if (entry != null)
        {
            a = entry.width * getSize();
            b = entry.height * getSize();
            c = entry.length * getSize();
        }

        getEntity().width = a;
        getEntity().height = b;
        this.length = c;

        if (a > 3 || b > 3 || c > 3)
        {
            getEntity().ignoreFrustumCheck = true;
        }
        try
        {
            SETSIZE.invoke(getEntity(), getEntity().width, getEntity().height);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        getEntity().setEntityBoundingBox(new AxisAlignedBB(getEntity().getEntityBoundingBox().minX,
                getEntity().getEntityBoundingBox().minY, getEntity().getEntityBoundingBox().minZ,
                getEntity().getEntityBoundingBox().minX + getEntity().width,
                getEntity().getEntityBoundingBox().minY + getEntity().height,
                getEntity().getEntityBoundingBox().minZ + getEntity().width));
        double max = Math.max(Math.max(a, b), c);
        World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, max);
        mainBox = new Matrix3(a, b, c);
    }

}
