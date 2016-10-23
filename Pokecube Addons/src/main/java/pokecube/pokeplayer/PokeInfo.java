package pokecube.pokeplayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import pokecube.pokeplayer.EventsHandler.SendPacket;
import pokecube.pokeplayer.inventory.InventoryPlayerPokemob;
import thut.api.entity.IHungrymob;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

public class PokeInfo extends PlayerData
{
    private ItemStack             stack;
    private IPokemob              pokemob;
    public InventoryPlayerPokemob pokeInventory;
    public float                  originalHeight;
    public float                  originalWidth;
    public float                  originalHP;

    public PokeInfo()
    {
    }

    public void set(IPokemob pokemob, EntityPlayer player)
    {
        if (this.pokemob != null) resetPlayer(player);
        this.pokemob = pokemob;
        this.pokeInventory = new InventoryPlayerPokemob(this, player.worldObj);
        this.originalHeight = player.height;
        this.originalWidth = player.width;
        this.originalHP = player.getMaxHealth();
        ((Entity) pokemob).getEntityData().setBoolean("isPlayer", true);
        ((Entity) pokemob).getEntityData().setString("playerID", player.getUniqueID().toString());
        pokemob.setPokemonOwner(player);
        save(player);
    }

    public void resetPlayer(EntityPlayer player)
    {
        if (pokemob == null && !player.worldObj.isRemote) return;
        player.eyeHeight = player.getDefaultEyeHeight();
        player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(originalHP);
        float height = originalHeight;
        float width = originalWidth;
        player.setSize(width, height);
        setFlying(player, false);
        save(player);
        pokemob = null;
        stack = null;
        pokeInventory = null;
        if (!player.worldObj.isRemote)
        {
            new SendPacket(player);
        }
    }

    public void setPlayer(EntityPlayer player)
    {
        if (pokemob == null) return;
        pokemob.setSize(pokemob.getSize());
        float height = pokemob.getSize() * pokemob.getPokedexEntry().height;
        float width = pokemob.getSize() * pokemob.getPokedexEntry().width;
        player.eyeHeight = ((EntityLivingBase) pokemob).getEyeHeight();
        player.setSize(width, height);
        setFlying(player, true);
        save(player);
        if (!player.worldObj.isRemote)
        {
            new SendPacket(player);
        }
    }

    public void onUpdate(EntityPlayer player)
    {
        if (pokemob == null) return;
        EntityLivingBase poke = (EntityLivingBase) pokemob;
        poke.onUpdate();
        player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(poke.getMaxHealth());
        if (player.capabilities.isCreativeMode)
        {
            poke.setHealth(poke.getMaxHealth());
        }
        float health = poke.getHealth();
        EntityTools.copyEntityTransforms((EntityLivingBase) pokemob, player);
        player.setHealth(health);
        int num = ((IHungrymob) poke).getHungerTime();
        int max = PokecubeMod.core.getConfig().pokemobLifeSpan;
        num = Math.round(((max - num) * 20) / (float) max);
        if (player.capabilities.isCreativeMode) num = 20;
        player.getFoodStats().setFoodLevel(num);
        float height = pokemob.getSize() * pokemob.getPokedexEntry().height;
        float width = pokemob.getSize() * pokemob.getPokedexEntry().width;
        player.eyeHeight = ((EntityLivingBase) pokemob).getEyeHeight();
        player.setSize(width, height);
        updateFloating(player);
        updateFlying(player);
        updateSwimming(player);
    }

    public void clear()
    {
        pokemob = null;
        pokeInventory = null;
        stack = null;
    }

    public void save(EntityPlayer player)
    {
        if (!player.worldObj.isRemote)
            PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), getIdentifier());
    }

    private void setFlying(EntityPlayer player, boolean set)
    {
        if (pokemob == null) return;
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
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys())
        {
            player.fallDistance = 0;
        }
    }

    private void updateFloating(EntityPlayer player)
    {
        if (pokemob == null) return;
        if (!player.isSneaking() && pokemob.getPokedexEntry().floats() && !player.capabilities.isFlying)
        {
            double h = pokemob.getPokedexEntry().preferedHeight;
            Vec3d start = new Vec3d(player.posX, player.posY, player.posZ);
            Vec3d end = new Vec3d(player.posX, player.posY - h, player.posZ);

            RayTraceResult position = player.worldObj.rayTraceBlocks(start, end, true, true, false);

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
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.water)) player.setAir(300);
    }

    @Override
    public String getIdentifier()
    {
        return "pokeplayer-data";
    }

    @Override
    public String dataFileName()
    {
        return "PokePlayer";
    }

    @Override
    public boolean shouldSync()
    {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        if (pokemob != null)
        {
            ItemStack stack = PokecubeManager.pokemobToItem(pokemob);
            stack.writeToNBT(tag);
        }
        else if (stack != null)
        {
            stack.writeToNBT(tag);
        }
        tag.setFloat("h", originalHeight);
        tag.setFloat("w", originalWidth);
        tag.setFloat("hp", originalHP);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        stack = ItemStack.loadItemStackFromNBT(tag);
        originalHeight = tag.getFloat("h");
        originalWidth = tag.getFloat("w");
        originalHP = tag.getFloat("hp");
        if (originalHP <= 1) originalHP = 20;
    }

    public IPokemob getPokemob(World world)
    {
        if (pokemob == null && stack != null)
        {
            pokemob = PokecubeManager.itemToPokemob(stack, world);
            if (pokemob == null) stack = null;
        }
        return pokemob;
    }
}
