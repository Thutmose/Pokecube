package pokecube.core.interfaces.capabilities.impl;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.LevelUpEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.utils.Tools;
import thut.api.maths.Matrix3;
import thut.lib.CompatWrapper;

public abstract class PokemobStats extends PokemobGenes
{
    @Override
    public void addHappiness(int toAdd)
    {
        this.bonusHappiness += toAdd;
        this.dataManager.set(params.HAPPYDW, Integer.valueOf(bonusHappiness));
    }

    @Override
    public int getExp()
    {
        return dataManager.get(params.EXPDW);
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
        return dataManager.get(params.NICKNAMEDW);
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
    public IPokemob setExp(int exp, boolean notifyLevelUp)
    {
        if (getEntity().isDead) return this;

        int old = dataManager.get(params.EXPDW);
        getMoveStats().oldLevel = this.getLevel();
        int lvl100xp = Tools.maxXPs[getExperienceMode()];
        exp = Math.min(lvl100xp, exp);

        dataManager.set(params.EXPDW, exp);
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
                updateHealth(newLvl);
                if (notifyLevelUp)
                {
                    ItemStack held = getHeldItem();
                    if (!getEntity().isDead && (canEvolve(CompatWrapper.nullStack) || canEvolve(held)))
                    {
                        levelUp(newLvl);
                        IPokemob evo = this.evolve(true, false, held);
                        ret = evo;
                    }
                    ret.levelUp(newLvl);
                    if (getEntity().addedToChunk && ret.getPokemonOwner() instanceof EntityPlayer
                            && getEntity().getEntityWorld().getGameRules().getBoolean("doMobLoot")
                            && !getEntity().getEntityWorld().isRemote)
                    {
                        getEntity().getEntityWorld().spawnEntityInWorld(new EntityXPOrb(getEntity().getEntityWorld(),
                                getEntity().posX, getEntity().posY, getEntity().posZ, 1));
                    }
                }
            }
        }
        return ret;
    }

    private void setMaxHealth(float maxHealth)
    {
        getEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
    }

    @Override
    public void setPokemonNickname(String nickname)
    {
        if (PokecubeCore.isOnClientSide())
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

    /** Handles health update.
     * 
     * @param level */
    private void updateHealth(int level)
    {
        float old = getEntity().getMaxHealth();
        float maxHealth = Tools.getHP(getPokedexEntry().getStatHP(), getIVs()[0], getEVs()[0], level);
        float health = getEntity().getHealth();

        if (maxHealth > old)
        {
            float damage = old - health;
            health = maxHealth - damage;

            if (health > maxHealth)
            {
                health = maxHealth;
            }
        }

        setMaxHealth(maxHealth);
        getEntity().setHealth(health);
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
        dataManager.set(params.EXPDW, exp);
        IPokemob ret = this.levelUp(level);
        ItemStack held = getHeldItem();
        if (evolve) while (ret.canEvolve(held))
        {
            IPokemob temp = ret.evolve(false, true, held);
            if (temp == null) break;
            ret = temp;
            ret.getDataManager().set(params.EXPDW, exp);
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
        getEntity().setSize(getEntity().width, getEntity().height);
        getEntity().setEntityBoundingBox(new AxisAlignedBB(getEntity().getEntityBoundingBox().minX,
                getEntity().getEntityBoundingBox().minY, getEntity().getEntityBoundingBox().minZ,
                getEntity().getEntityBoundingBox().minX + getEntity().width,
                getEntity().getEntityBoundingBox().minY + getEntity().height,
                getEntity().getEntityBoundingBox().minZ + getEntity().width));
        double max = Math.max(Math.max(a, b), c);
        World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, max);
        mainBox = new Matrix3(a, b, c);
    }

    @Override
    public void specificSpawnInit()
    {
        this.setHeldItem(this.wildHeldItem());
        setSpecialInfo(getPokedexEntry().defaultSpecial);
        getEntity().setHealth(getEntity().getMaxHealth());
    }

}