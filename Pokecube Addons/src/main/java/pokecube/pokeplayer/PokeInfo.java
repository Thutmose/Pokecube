package pokecube.pokeplayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.pokeplayer.inventory.InventoryPlayerPokemob;
import thut.api.entity.IHungrymob;

public class PokeInfo
{
    public final IPokemob               pokemob;
    public final PokedexEntry           entry;
    public final InventoryPlayerPokemob pokeInventory;
    public float                        originalHeight;
    public float                        originalWidth;

    public PokeInfo(IPokemob pokemob, EntityPlayer player)
    {
        this.pokemob = pokemob;
        this.pokeInventory = new InventoryPlayerPokemob(this);
        this.entry = pokemob.getPokedexEntry();
        this.originalHeight = 1.8f;
        this.originalWidth = 0.6f;
        ((Entity) pokemob).getEntityData().setBoolean("isPlayer", true);
        ((Entity) pokemob).getEntityData().setString("playerID", player.getUniqueID().toString());
    }

    public void resetPlayer(EntityPlayer player)
    {
        player.eyeHeight = player.getDefaultEyeHeight();
        player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
        float height = originalHeight;
        float width = originalWidth;
        player.setSize(width, height);
        setFlying(player, false);
    }

    public void setPlayer(EntityPlayer player)
    {
        pokemob.setPokemonOwner(player);
        if (!((Entity) pokemob).getEntityData().hasKey("oldNickname"))
            ((Entity) pokemob).getEntityData().setString("oldNickname", pokemob.getPokemonNickname());
        pokemob.setPokemonNickname(player.getName());
        pokemob.setSize(pokemob.getSize());
        float height = pokemob.getSize() * entry.height;
        float width = pokemob.getSize() * entry.width;
        player.eyeHeight = ((EntityLivingBase) pokemob).getEyeHeight();
        player.setSize(width, height);
        setFlying(player, true);
    }

    public void onUpdate(EntityPlayer player)
    {
        EntityLivingBase poke = (EntityLivingBase) pokemob;
        poke.onUpdate();
        //TODO make sure this properly deals with creative mode stuff.
        player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(poke.getMaxHealth());
        float health = poke.getHealth();
        if(player.capabilities.isCreativeMode) health = poke.getMaxHealth();
        player.setHealth(health);
        PokePlayer.PROXY.copyTransform((EntityLivingBase) pokemob, player);
        int num = ((IHungrymob)poke).getHungerTime();
        int max = PokecubeMod.core.getConfig().pokemobLifeSpan;
        num = Math.round(((max - num) * 20) / (float)max);
        if(player.capabilities.isCreativeMode) num = 20;
        player.getFoodStats().setFoodLevel(num);
        updateFloating(player);
        updateFlying(player);
        updateSwimming(player);
    }

    public void save(EntityPlayer player)
    {
        if (pokemob != null)
        {
            NBTTagCompound poketag = new NBTTagCompound();
            poketag.setInteger("pokenum", pokemob.getPokedexNb());
            ((Entity) pokemob).writeToNBT(poketag);
            player.getEntityData().setTag("Pokemob", poketag);
        }
    }

    private void setFlying(EntityPlayer player, boolean set)
    {
        boolean fly = pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys();
        boolean check = set ? !player.capabilities.allowFlying : player.capabilities.allowFlying;
        if (fly && check && player.worldObj.isRemote && !player.capabilities.isCreativeMode)
        {
            player.capabilities.allowFlying = set;
            player.sendPlayerAbilities();
        }
    }

    private void updateFlying(EntityPlayer player)
    {
        if (pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys())
        {
            player.fallDistance = 0;
        }
    }

    private void updateFloating(EntityPlayer player)
    {
        if (!player.isSneaking() && pokemob.getPokedexEntry().floats() && !player.capabilities.isFlying)
        {
            double h = pokemob.getPokedexEntry().preferedHeight;
            Vec3 start = new Vec3(player.posX, player.posY, player.posZ);
            Vec3 end = new Vec3(player.posX, player.posY - h, player.posZ);

            MovingObjectPosition position = player.worldObj.rayTraceBlocks(start, end, true, true, false);

            if (position != null)
            {
                double d = position.hitVec.subtract(start).lengthVector();
                if (d < 0.9 * h) player.motionY += 0.1;
                else player.motionY = 0;
            }
            else
            {
                player.motionY *= 0.6;
            }
        }
    }

    private void updateSwimming(EntityPlayer player)
    {
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.water)) player.setAir(300);
    }

}
