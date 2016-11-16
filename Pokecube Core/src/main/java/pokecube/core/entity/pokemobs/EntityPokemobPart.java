package pokecube.core.entity.pokemobs;

import java.util.HashMap;
import java.util.UUID;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.boss.EntityDragonPart;
import pokecube.core.entity.pokemobs.AnimalChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.Nature;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class EntityPokemobPart extends EntityDragonPart implements IPokemob
{
    public final IPokemob      parent;
    public final Vector3f      offset;
    public final AxisAlignedBB defaultBox;

    public EntityPokemobPart(IPokemob parent, String partName, Vector3f offset, Vector3f[] dimensions)
    {
        super((IEntityMultiPart) parent, partName, 1, 1);
        this.parent = parent;
        this.offset = offset;
        defaultBox = new AxisAlignedBB(dimensions[0].x, dimensions[0].y, dimensions[0].z, dimensions[1].x,
                dimensions[1].y, dimensions[1].z);
    }

    @Override
    public boolean addChange(int change)
    {
        return parent.addChange(change);
    }

    @Override
    public void addEVs(byte[] evsToAdd)
    {
        parent.addEVs(evsToAdd);
    }

    @Override
    public void addHappiness(int toAdd)
    {
        parent.addHappiness(toAdd);
    }

    @Override
    public boolean addOngoingEffect(Move_Base effect)
    {
        return parent.addOngoingEffect(effect);
    }

    @Override
    public void cancelEvolve()
    {
        parent.cancelEvolve();
    }

    @Override
    public boolean canEvolve(ItemStack stack)
    {
        return parent.canEvolve(stack);
    }

    @Override
    public boolean canUseDive()
    {
        return parent.canUseDive();
    }

    @Override
    public boolean canUseFly()
    {
        return parent.canUseFly();
    }

    @Override
    public boolean canUseSurf()
    {
        return parent.canUseSurf();
    }

    @Override
    public void displayMessageToOwner(ITextComponent message)
    {
        parent.displayMessageToOwner(message);
    }

    @Override
    public void eat(Entity eaten)
    {
        parent.eat(eaten);
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init)
    {
        return parent.evolve(delayed, init);
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init, ItemStack item)
    {
        return parent.evolve(delayed, init, item);
    }

    @Override
    public void exchangeMoves(int moveIndex0, int moveIndex1)
    {
        parent.exchangeMoves(moveIndex0, moveIndex1);
    }

    @Override
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        parent.executeMove(target, targetLocation, f);
    }

    @Override
    public Ability getAbility()
    {
        return parent.getAbility();
    }

    @Override
    public float getAttackStrength()
    {
        return parent.getAttackStrength();
    }

    @Override
    public int getBaseXP()
    {
        return parent.getBaseXP();
    }

    @Override
    public int getCatchRate()
    {
        return parent.getCatchRate();
    }

    @Override
    public int getChanges()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getDirectionPitch()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEvolutionTicks()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getEVs()
    {
        // TODO Auto-generated method stub
        return parent.getEVs();
    }

    @Override
    public int getExp()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExperienceMode()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExplosionState()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public EntityAIBase getGuardAI()
    {
        // TODO Auto-generated method stub
        return parent.getGuardAI();
    }

    @Override
    public int getHappiness()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BlockPos getHome()
    {
        // TODO Auto-generated method stub
        return parent.getHome();
    }

    @Override
    public float getHomeDistance()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getInterestedAngle(float f)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getIVs()
    {
        // TODO Auto-generated method stub
        return parent.getIVs();
    }

    @Override
    public int getLevel()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMove(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMoveIndex()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getMovementSpeed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String[] getMoves()
    {
        // TODO Auto-generated method stub
        return parent.getMoves();
    }

    @Override
    public PokemobMoveStats getMoveStats()
    {
        // TODO Auto-generated method stub
        return parent.getMoveStats();
    }

    @Override
    public Nature getNature()
    {
        // TODO Auto-generated method stub
        return parent.getNature();
    }

    @Override
    public HashMap<Move_Ongoing, Integer> getOngoingEffects()
    {
        // TODO Auto-generated method stub
        return parent.getOngoingEffects();
    }

    @Override
    public boolean getOnGround()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        // TODO Auto-generated method stub
        return parent.getOriginalOwnerUUID();
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        // TODO Auto-generated method stub
        return parent.getPokedexEntry();
    }

    @Override
    public Integer getPokedexNb()
    {
        // TODO Auto-generated method stub
        return parent.getPokedexNb();
    }

    @Override
    public AnimalChest getPokemobInventory()
    {
        // TODO Auto-generated method stub
        return parent.getPokemobInventory();
    }

    @Override
    public Team getPokemobTeam()
    {
        // TODO Auto-generated method stub
        return parent.getPokemobTeam();
    }

    @Override
    public boolean getPokemonAIState(int state)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ITextComponent getPokemonDisplayName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPokemonNickname()
    {
        // TODO Auto-generated method stub
        return parent.getPokemonNickname();
    }

    @Override
    public EntityLivingBase getPokemonOwner()
    {
        // TODO Auto-generated method stub
        return parent.getPokemonOwner();
    }

    @Override
    public int getPokemonUID()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getSexe()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getShakeAngle(float f, float f1)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SoundEvent getSound()
    {
        // TODO Auto-generated method stub
        return parent.getSound();
    }

    @Override
    public int getSpecialInfo()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getStatus()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public short getStatusTimer()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResourceLocation getTexture()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getTransformedTo()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PokeType getType1()
    {
        // TODO Auto-generated method stub
        return parent.getType1();
    }

    @Override
    public PokeType getType2()
    {
        // TODO Auto-generated method stub
        return parent.getType2();
    }

    @Override
    public EntityAIBase getUtilityMoveAI()
    {
        // TODO Auto-generated method stub
        return parent.getUtilityMoveAI();
    }

    @Override
    public Entity getWeapon(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getWeight()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasHomeArea()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void healStatus()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAncient()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEvolving()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isShadow()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isShiny()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isType(PokeType type)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void learn(String moveName)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public IPokemob levelUp(int level)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onMoveUse(MovePacket move)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void popFromPokecube()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeChanges(int changes)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnToPokecube()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAbility(Ability ability)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAncient(boolean toSet)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirectionPitch(float pitch)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEvolution(String evolution)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEvolutionTicks(int evolutionTicks)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEVs(byte[] evs)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setExplosionState(int i)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeldItem(ItemStack Item)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHome(int x, int y, int z, int distance)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHp(float min)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIVs(byte[] ivs)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLeaningMoveIndex(int num)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMove(int i, String moveName)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMoveIndex(int i)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNature(Nature nature)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOriginalOwnerUUID(UUID original)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPokemonNickname(String nickname)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPokemonOwner(EntityLivingBase e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSexe(byte sexe)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setShadow(boolean toSet)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setShiny(boolean shiny)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSize(float size)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSpecialInfo(int info)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean setStatus(byte status)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setStatusTimer(short timer)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setToHiddenAbility()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTraded(boolean traded)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTransformedTo(Entity to)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setType1(PokeType type1)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setType2(PokeType type2)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setWeapon(int index, Entity weapon)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void specificSpawnInit()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean traded()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ItemStack wildHeldItem()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRNGValue()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRNGValue(int value)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSubParts(EntityPokemobPart[] subParts)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean equals(Object o)
    {
        return o == parent ? true : super.equals(o);
    }

    @Override
    public boolean isPlayerOwned()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getAbilityIndex()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAbilityIndex(int index)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public IPokemob setExp(int exp, boolean notifyLevelUp)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPokemob setForSpawn(int exp)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getFlavourAmount(int index)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFlavourAmount(int index, int amount)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ItemStack getPokecube()
    {
        // TODO Auto-generated method stub
        return parent.getPokecube();
    }

    @Override
    public void setPokecube(ItemStack pokecube)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public UUID getPokemonOwnerID()
    {
        return parent.getPokemonOwnerID();
    }

    @Override
    public void setPokemonOwner(UUID id)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void readPokemobData(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public NBTTagCompound writePokemobData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPokemob megaEvolve(PokedexEntry forme)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getAttackCooldown()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAttackCooldown(int timer)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getLastMoveUsed()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStat(Stats stat, boolean modified)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBaseStat(Stats stat)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public StatModifiers getModifiers()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
