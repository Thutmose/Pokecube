/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.io.IOException;
import java.util.Random;

import org.nfunk.jep.JEP;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.KillEvent;
import pokecube.core.events.LevelUpEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

/** @author Manchou */
public abstract class EntityStatsPokemob extends EntityTameablePokemob implements IEntityAdditionalSpawnData
{
    protected Ability     ability;

    double                moveSpeed;

    byte[]                ivs              = new byte[] { 0, 0, 0, 0, 0, 0 };
    protected Nature      nature           = Nature.HARDY;
    public int            oldLevel         = 0;
    public PokedexEntry   entry;

    /** The happiness value of the pokemob */
    protected int         bonusHappiness   = 0;

    boolean               wasShadow        = false;

    boolean               isAncient        = false;
    /** The higher this value, the more likely for mobs to range in colour. It
     * is very sensitive to the size of this number. */
    private double        colourDiffFactor = 0.25;

    /** Used for the random colour differences */
    int[]                 rgba             = { 255, 255, 255, 255 };

    /** Used for if there is a special texture */
    public boolean        shiny            = false;
    PokeType              type1, type2;
    private int           personalityValue = 0;
    private int           killCounter      = 0;
    private int           resetTick        = 0;
    private StatModifiers modifiers        = new StatModifiers();

    public EntityStatsPokemob(World world)
    {
        super(world);
    }

    @Override
    public void addEVs(byte[] evsToAdd)
    {
        byte[] evs = getEVs();
        for (int i = 0; i < 6; i++)
        {
            if (evs[i] + 128 + evsToAdd[i] <= 255 && evs[i] + 128 + evsToAdd[i] >= 0)
            {
                evs[i] = (byte) (evs[i] + evsToAdd[i]);
            }
            else
            {
                evs[i] = (byte) 127;
            }
        }

        int sum = 0;

        for (byte ev : evs)
        {
            sum += ev + 128;
        }

        if (sum < 510)
        {
            setEVs(evs);
        }
    }

    @Override
    public void addHappiness(int toAdd)
    {
        this.bonusHappiness += toAdd;
        this.dataManager.set(HAPPYDW, Integer.valueOf(bonusHappiness));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
        // Follow Range - default 32.0D - min 0.0D - max 2048.0D
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32);
        // Knockback Resistance - default 0.0D - min 0.0D - max 1.0D
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(10);
        // Movement Speed - default 0.699D - min 0.0D - max Double.MAX_VALUE
        moveSpeed = 0.6f;
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(moveSpeed);
        // Attack Damage - default 2.0D - min 0.0D - max Doubt.MAX_VALUE
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, amount)) return false;
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (this.worldObj.isRemote)
        {
            return false;
        }
        else
        {
            this.entityAge = 0;

            if (source.isExplosion() && source.getEntity() instanceof IPokemob && isType(ghost)) { return false; }

            if (!(source.getEntity() instanceof IPokemob || source instanceof PokemobDamageSource)
                    && PokecubeMod.core.getConfig().onlyPokemobsDamagePokemobs)
                return false;

            if (source.getEntity() instanceof EntityPlayer && !(source instanceof PokemobDamageSource))
                amount *= PokecubeMod.core.getConfig().playerToPokemobDamageScale;

            if (this.getHealth() <= 0.0F)
            {
                return false;
            }
            else if (source.isFireDamage()
                    && this.isPotionActive(Potion.getPotionFromResourceLocation("fire_resistance")))
            {
                return false;
            }
            else
            {
                this.limbSwingAmount = 1.5F;
                boolean flag = true;

                if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0F)
                {
                    if (amount <= this.lastDamage) { return false; }

                    this.damageEntity(source, amount - this.lastDamage);
                    this.lastDamage = amount;
                    flag = false;
                }
                else
                {
                    this.lastDamage = amount;
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    this.damageEntity(source, amount);
                    this.hurtTime = this.maxHurtTime = 10;
                }

                this.attackedAtYaw = 0.0F;
                Entity entity = source.getEntity();

                if (entity != null)
                {
                    if (entity instanceof EntityLivingBase)
                    {
                        this.setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer)
                    {
                        this.recentlyHit = 100;
                        this.attackingPlayer = (EntityPlayer) entity;
                    }
                    else if (entity instanceof IEntityOwnable)
                    {
                        IEntityOwnable entitywolf = (IEntityOwnable) entity;

                        if (entitywolf.getOwner() != null)
                        {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag)
                {
                    this.worldObj.setEntityState(this, (byte) 2);

                    if (source != DamageSource.drown)
                    {
                        this.setBeenAttacked();
                    }

                    if (entity != null)
                    {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1
                                + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D)
                        {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.attackedAtYaw = (float) (MathHelper.atan2(d0, d1) * 180.0D / Math.PI - this.rotationYaw);
                        // Reduces knockback from distanced moves
                        if (source instanceof PokemobDamageSource)
                        {
                            if (!source.isProjectile())
                            {
                                this.knockBack(entity, amount, d1, d0);
                            }
                        }
                        else
                        {
                            this.knockBack(entity, amount, d1, d0);
                        }
                    }
                    else
                    {
                        this.attackedAtYaw = (int) (Math.random() * 2.0D) * 180;
                    }
                }

                if (this.getHealth() <= 0.0F)
                {
                    SoundEvent s = this.getDeathSound();

                    if (flag && s != null)
                    {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    this.onDeath(source);
                }
                else
                {
                    SoundEvent s1 = this.getHurtSound();

                    if (flag && s1 != null)
                    {
                        this.playSound(s1, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    @Override
    public Ability getAbility()
    {
        if (getPokemonAIState(MEGAFORME)) return getPokedexEntry().getAbility(0, this);
        return ability;
    }

    @Override
    public int getAbilityIndex()
    {
        return abilityIndex;
    }

    @Override
    public float getAttackStrength()
    {
        int ATT = getStat(Stats.ATTACK, true);
        int ATTSPE = getStat(Stats.SPATTACK, true);
        float mult = getPokedexEntry().isShadowForme ? 2 : 1;
        return mult * ((ATT + ATTSPE) / 6f);
    }

    @Override
    public int getBaseXP()
    {
        return getPokedexEntry().getBaseXP();
    }

    @Override
    public int getCatchRate()
    {
        return getPokedexEntry().isShadowForme ? 0 : isAncient() ? 0 : getPokedexEntry().getCatchRate();
    }

    @Override
    public byte[] getEVs()
    {
        int[] ints = new int[] { dataManager.get(EVS1DW), dataManager.get(EVS2DV) };
        byte[] evs = PokecubeSerializer.intArrayAsByteArray(ints);
        return evs;
    }

    @Override
    public int getExp()
    {
        return dataManager.get(EXPDW);
    }

    @Override
    public int getExperienceMode()
    {
        return getPokedexEntry().getEvolutionMode();
    }

    @Override
    public int getHappiness()
    {
        bonusHappiness = dataManager.get(HAPPYDW);
        bonusHappiness = Math.max(bonusHappiness, -getPokedexEntry().getHappiness());
        bonusHappiness = Math.min(bonusHappiness, 255 - getPokedexEntry().getHappiness());
        return bonusHappiness + getPokedexEntry().getHappiness();
    }

    @Override
    public byte[] getIVs()
    {
        return this.ivs;
    }

    @Override
    public int getLevel()
    {
        return Tools.xpToLevel(getExperienceMode(), getExp());
    }

    @Override
    public StatModifiers getModifiers()
    {
        return modifiers;
    }

    @Override
    public Nature getNature()
    {
        return nature;
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (entry == null)
        {
            if (getClass().getName().contains("GenericPokemob"))
            {
                String num = getClass().getSimpleName().replace("GenericPokemob", "").trim();
                entry = Database.getEntry(Integer.parseInt(num));
                if (entry != null && entry.getPokedexNb() > 0)
                {
                    init(entry.getPokedexNb());
                    return entry;
                }
            }
            System.out.println(this.getClass());
            Thread.dumpStack();
            this.setDead();
            return Database.missingno;
        }
        return entry.getForGender(getSexe());
    }

    @Override
    public Integer getPokedexNb()
    {
        return getPokedexEntry().getPokedexNb();
    }

    @Override
    public ITextComponent getPokemonDisplayName()
    {
        if (this.getPokemonNickname().isEmpty())
            return new TextComponentTranslation(getPokedexEntry().getUnlocalizedName());
        return new TextComponentString(this.getPokemonNickname());
    }

    @Override
    public String getPokemonNickname()
    {
        return dataManager.get(NICKNAMEDW);
    }

    @Override
    public int[] getRGBA()
    {
        return rgba;
    }

    @Override
    public PokeType getType1()
    {
        return type1 != null ? type1 : getPokedexEntry().getType1();
    }

    @Override
    public PokeType getType2()
    {
        return type2 != null ? type2 : getPokedexEntry().getType2();
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        getPokedexEntry();

        this.setRNGValue(rand.nextInt());

        setEVs(PokecubeSerializer.noEVs);
        setIVs(new byte[] { Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand),
                Tools.getRandomIV(rand), Tools.getRandomIV(rand), Tools.getRandomIV(rand) });
        if (this.isAncient())
        {
            setIVs(new byte[] { 31, 31, 31, 31, 31, 31 });
        }
        if (PokecubeCore.isOnClientSide()) this.setHealth(getMaxHealth());
        else this.setHealth(0);
        nature = Nature.values()[(byte) (new Random()).nextInt(25)];
        setRandomColour();
    }

    @Override
    public boolean isAncient()
    {
        return isAncient;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source)
    {
        if (source instanceof PokemobDamageSource)
        {
            Move_Base move = ((PokemobDamageSource) source).move;
            return PokeType.getAttackEfficiency(move.getType(((PokemobDamageSource) source).user), getType1(),
                    getType2()) <= 0;
        }

        return super.isEntityInvulnerable(source);
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
    public boolean isShiny()
    {
        if (shiny && !getPokedexEntry().hasShiny) shiny = false;
        return shiny;
    }

    @Override
    public boolean isType(PokeType steel)
    {
        return this.getType1() == steel || getType2() == steel;
    }

    /** This method gets called when the entity kills another one. */
    @Override
    public void onKillEntity(EntityLivingBase attacked)
    {
        if (worldObj.isRemote) return;
        IPokemob attacker = this;
        if (PokecubeCore.core.getConfig().nonPokemobExp && !(attacked instanceof IPokemob))
        {
            JEP parser = new JEP();
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex();
            parser.addVariable("h", 0);
            parser.addVariable("a", 0);
            parser.parseExpression(PokecubeCore.core.getConfig().nonPokemobExpFunction);
            parser.setVarValue("h", attacked.getMaxHealth());
            parser.setVarValue("a", attacked.getTotalArmorValue());
            int exp = (int) parser.getValue();
            if (parser.hasError()) exp = 0;
            attacker.setExp(attacker.getExp() + exp, true);
            return;
        }
        if (attacked instanceof IPokemob && attacked.getHealth() <= 0)
        {
            boolean giveExp = !((IPokemob) attacked).isShadow();
            if (killCounter == 0)
            {
                resetTick = ticksExisted;
            }
            killCounter++;
            if (resetTick < ticksExisted - 100)
            {
                killCounter = 0;
            }
            giveExp = giveExp && killCounter <= 5;
            boolean pvp = ((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED)
                    && (((IPokemob) attacked).getPokemonOwner() instanceof EntityPlayer);
            if (pvp && !PokecubeMod.core.getConfig().pvpExp)
            {
                giveExp = false;
            }
            if ((((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED)
                    && !PokecubeMod.core.getConfig().trainerExp))
            {
                giveExp = false;
            }
            KillEvent event = new KillEvent(attacker, (IPokemob) attacked, giveExp);
            MinecraftForge.EVENT_BUS.post(event);
            giveExp = event.giveExp;
            if (event.isCanceled())
            {

            }
            else if (giveExp)
            {
                attacker.setExp(attacker.getExp()
                        + Tools.getExp((float) (pvp ? PokecubeMod.core.getConfig().pvpExpMultiplier : 1),
                                ((IPokemob) attacked).getBaseXP(), ((IPokemob) attacked).getLevel()),
                        true);
                byte[] evsToAdd = Pokedex.getInstance().getEntry(((IPokemob) attacked).getPokedexNb()).getEVs();
                attacker.addEVs(evsToAdd);
            }
            Entity targetOwner = ((IPokemob) attacked).getPokemonOwner();
            displayMessageToOwner(new TextComponentTranslation("pokemob.action.faint.enemy",
                    ((IPokemob) attacked).getPokemonDisplayName()));
            if (targetOwner instanceof EntityPlayer && attacker.getPokemonOwner() != targetOwner
                    && !PokecubeMod.pokemobsDamagePlayers)
            {
                ((EntityCreature) attacker).setAttackTarget((EntityLivingBase) targetOwner);
            }
            else
            {
                ((EntityCreature) attacker).setAttackTarget(null);
            }
            if (this.getPokedexEntry().isFood(((IPokemob) attacked).getPokedexEntry())
                    && this.getPokemonAIState(HUNTING))
            {
                ((EntityHungryPokemob) this).eat(getAttackTarget());
                ((EntityPokemob) attacked).wasEaten = true;
                this.setPokemonAIState(HUNTING, false);
                getNavigator().clearPathEntity();
            }
        }
        else
        {
            ((EntityCreature) attacker).setAttackTarget(null);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (Math.random() > 0.999 && this.getPokemonAIState(IMoveConstants.TAMED))
        {
            HappinessType.applyHappiness(this, HappinessType.TIME);
        }
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void readSpawnData(ByteBuf data)
    {
        PacketBuffer buffer = new PacketBuffer(data);
        try
        {
            this.readPokemobData(buffer.readNBTTagCompoundFromBuffer());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setAbility(Ability ability)
    {
        if (this.ability != null && this.ability != ability) this.ability.destroy();
        this.ability = ability;
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        this.abilityIndex = ability;
        if (abilityIndex > 2 || abilityIndex < 0) abilityIndex = 0;
    }

    @Override
    public void setAncient(boolean ancient)
    {
        isAncient = ancient;
    }

    @Override
    public void setEVs(byte[] evs)
    {
        int[] ints = PokecubeSerializer.byteArrayAsIntArray(evs);
        dataManager.set(EVS1DW, ints[0]);
        dataManager.set(EVS2DV, ints[1]);
    }

    @Override
    public IPokemob setExp(int exp, boolean notifyLevelUp)
    {
        if (this.isDead) return this;

        int old = dataManager.get(EXPDW);
        oldLevel = this.getLevel();
        int lvl100xp = Tools.maxXPs[getExperienceMode()];
        exp = Math.min(lvl100xp, exp);

        dataManager.set(EXPDW, exp);
        int newLvl = Tools.xpToLevel(getExperienceMode(), exp);
        int oldLvl = Tools.xpToLevel(getExperienceMode(), old);
        IPokemob ret = this;
        if (oldLvl != newLvl)
        {
            // Fire event to allow others to interfere
            LevelUpEvent lvlup = new LevelUpEvent(this, newLvl, oldLevel);
            MinecraftForge.EVENT_BUS.post(lvlup);
            if (!lvlup.isCanceled())
            {
                updateHealth(newLvl);
                if (notifyLevelUp)
                {
                    ItemStack held = getHeldItemMainhand();
                    if (!this.isDead && (canEvolve(null) || canEvolve(held)))
                    {
                        levelUp(newLvl);
                        IPokemob evo = this.evolve(true, false, held);
                        ret = evo;
                    }
                    ret.levelUp(newLvl);
                    if (this.addedToChunk && ret.getPokemonOwner() instanceof EntityPlayer
                            && worldObj.getGameRules().getBoolean("doMobLoot") && !worldObj.isRemote)
                    {
                        worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, 1));
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public void setIVs(byte[] ivs)
    {
        this.ivs = ivs;
    }

    private void setMaxHealth(float maxHealth)
    {
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
    }

    @Override
    public void setNature(Nature nature)
    {
        this.nature = nature;
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        if (newEntry == null || newEntry == entry) return this;
        IPokemob ret = this;
        entry = newEntry;
        if (newEntry.getPokedexNb() != getPokedexNb())
        {
            ret = megaEvolve(newEntry);
        }
        if (worldObj != null) ret.setSize(ret.getSize());
        if (worldObj != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketChangeForme.sendPacketToNear((Entity) ret, newEntry, 128);
        }
        return ret;
    }

    @Override
    public void setPokemonNickname(String nickname)
    {
        if (PokecubeCore.isOnClientSide())
        {
            if (!nickname.equals(getPokemonNickname()) && addedToChunk)
            {
                PacketNickname.sendPacket(this, nickname);
            }
        }
        else
        {
            if (getPokedexEntry().getName().equals(nickname))
            {
                dataManager.set(NICKNAMEDW, "");
            }
            else
            {
                dataManager.set(NICKNAMEDW, nickname);
            }
        }
    }

    void setRandomColour()
    {
        Random r = new Random();
        int first = r.nextInt(3);
        byte red = 127, green = 127, blue = 127;
        if (first == 0)
        {
            int min = 0;
            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = green < 63 ? 63 : 0;

            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
        }
        if (first == 1)
        {
            int min = 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = green < 63 ? 63 : 0;

            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
        }
        if (first == 2)
        {
            int min = 0;
            blue = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = blue < 63 ? 63 : 0;

            red = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);
            min = red < 63 ? 63 : 0;

            green = (byte) Math.max(Math.min(((5 - Math.abs(colourDiffFactor * r.nextGaussian())) * 32), 127), min);

        }
        rgba[0] = red + 128;
        rgba[1] = green + 128;
        rgba[2] = blue + 128;
    }

    @Override
    public void setRGBA(int... colours)
    {
        for (int i = 0; i < colours.length && i < rgba.length; i++)
        {
            rgba[i] = colours[i];
        }
    }

    @Override
    public void setShadow(boolean shadow)
    {
        PokedexEntry entry = getPokedexEntry();
        if (entry.isShadowForme && !shadow)
        {
            this.setPokedexEntry(entry);
        }
        else if (shadow && !entry.isShadowForme && entry.shadowForme != null)
        {
            this.setPokedexEntry(entry.shadowForme);
        }
        if (shadow && !wasShadow)
        {
            wasShadow = true;
        }
    }

    @Override
    public void setShiny(boolean shiny)
    {
        this.shiny = shiny;
    }

    @Override
    public void setToHiddenAbility()
    {
        this.abilityIndex = 2;
        this.setAbility(getPokedexEntry().getHiddenAbility(this));
    }

    @Override
    public void setType1(PokeType type1)
    {
        this.type1 = type1;
    }

    @Override
    public void setType2(PokeType type2)
    {
        this.type2 = type2;
    }

    @Override
    public int getStat(Stats stat, boolean modified)
    {
        return getModifiers().getStat(this, stat, modified);
    }

    @Override
    public int getBaseStat(Stats stat)
    {
        if (stat.ordinal() > 5) return 1;
        return getPokedexEntry().getStats()[stat.ordinal()];
    }

    /** Handles health update.
     * 
     * @param level */
    private void updateHealth(int level)
    {
        float old = getMaxHealth();
        float maxHealth = Tools.getHP(getPokedexEntry().getStatHP(), getIVs()[0], getEVs()[0], level);
        float health = getHealth();

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
        setHealth(health);
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        PacketBuffer buffer = new PacketBuffer(data);
        NBTTagCompound tag = writePokemobData();
        buffer.writeNBTTagCompoundToBuffer(tag);
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
    public IPokemob setForSpawn(int exp)
    {
        int level = Tools.xpToLevel(getExperienceMode(), exp);
        this.oldLevel = 0;
        dataManager.set(EXPDW, exp);
        EntityPokemobBase ret = (EntityPokemobBase) this.levelUp(level);
        ItemStack held = getHeldItemMainhand();
        while (ret.canEvolve(held))
        {
            IPokemob temp = ret.evolve(false, true, held);
            if (temp == null) break;
            ret = (EntityPokemobBase) temp;
            ret.dataManager.set(EXPDW, exp);
            ret.levelUp(level);
        }
        return ret;
    }

}
