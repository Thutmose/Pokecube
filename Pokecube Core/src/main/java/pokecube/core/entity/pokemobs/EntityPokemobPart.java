package pokecube.core.entity.pokemobs;

import java.util.UUID;
import java.util.Vector;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.Nature;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

public class EntityPokemobPart extends MultiPartEntityPart implements IPokemob
{
    public final IPokemob      parent;
    public final Vector3f      offset;
    public final AxisAlignedBB defaultBox;

    public EntityPokemobPart(World world)
    {
        super(null, null, 0, 0);
        this.parent = null;
        this.offset = null;
        this.defaultBox = null;
    }

    public EntityPokemobPart(IPokemob parent, String partName, Vector3f offset, Vector3f[] dimensions)
    {
        super((IEntityMultiPart) parent, partName, 1, 1);
        this.parent = parent;
        this.offset = offset;
        defaultBox = new AxisAlignedBB(dimensions[0].x, dimensions[0].y, dimensions[0].z, dimensions[1].x,
                dimensions[1].y, dimensions[1].z);
    }

    @Override
    public boolean getPokemonAIState(int state)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addOngoingEffect(Move_Base effect)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init, ItemStack stack)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void exchangeMoves(int moveIndex0, int moveIndex1)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void executeMove(Entity target, Vector3 targetLocation, float f)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getAttackCooldown()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getLastMoveUsed()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransformedTo(Entity to)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getMoveIndex()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String[] getMoves()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PokemobMoveStats getMoveStats()
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
    public EntityAIBase getUtilityMoveAI()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void learn(String moveName)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttackCooldown(int timer)
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
    public float getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addEVs(byte[] evsToAdd)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHappiness(int toAdd)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Ability getAbility()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAbility(Ability ability)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAbilityIndex(int index)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getAbilityIndex()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getEVs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getExp()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getIVs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatModifiers getModifiers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Nature getNature()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNature(Nature nature)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelEvolve()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void displayMessageToOwner(ITextComponent message)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack getPokecube()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPokemobTeam()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPokemonNickname()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityLivingBase getPokemonOwner()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID getPokemonOwnerID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPlayerOwned()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setOriginalOwnerUUID(UUID original)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPokecube(ItemStack pokecube)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPokemobTeam(String team)
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
    public void setPokemonOwner(UUID id)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addChange(int change)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void eat(Entity eaten)
    {
        // TODO Auto-generated method stub

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
    public int getExplosionState()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public EntityAIBase getGuardAI()
    {
        // TODO Auto-generated method stub
        return null;
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
        return null;
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
    public double getMovementSpeed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getOnGround()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AnimalChest getPokemobInventory()
    {
        // TODO Auto-generated method stub
        return null;
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
    public SoundEvent getSound()
    {
        // TODO Auto-generated method stub
        return null;
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
    public IPokemob levelUp(int level)
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
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void popFromPokecube()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void returnToPokecube()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirectionPitch(float pitch)
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
    public IPokemob setExp(int exp, boolean notifyLevelUp)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPokemob setForSpawn(int exp, boolean evolve)
    {
        // TODO Auto-generated method stub
        return null;
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
    public void setSexe(byte sexe)
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
    public void specificSpawnInit()
    {
        // TODO Auto-generated method stub

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
    public void setEvolutionStack(ItemStack stack)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ItemStack getEvolutionStack()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMoves(String[] moves)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean eatsBerries()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean filterFeeder()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getHungerCooldown()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHungerTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isCarnivore()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isElectrotroph()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHerbivore()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLithotroph()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPhototroph()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean neverHungry()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void noEat(Entity e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHungerCooldown(int hungerCooldown)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHungerTime(int hungerTime)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object getChild(IBreedingMob male)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getLover()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLoveTimer()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector<IBreedingMob> getMalesForBreeding()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void mateWith(IBreedingMob male)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetLoveStatus()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLover(Entity lover)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLoveTimer(int value)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean tryToBreed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean floats()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean flys()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getBlockPathWeight(IBlockAccess world, Vector3 location)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getFloatHeight()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector3 getMobSizes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean fits(IBlockAccess world, Vector3 location, Vector3 directionFrom)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getPathTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean swims()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canMate(EntityAnimal entityAnimal)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
