package pokecube.pokeplayer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public class PokeInfo
{
    public final IPokemob     pokemob;
    public final PokedexEntry entry;
    public float              originalHeight;
    public float              originalWidth;

    public PokeInfo(IPokemob pokemob, EntityPlayer player)
    {
        this.pokemob = pokemob;
        this.entry = pokemob.getPokedexEntry();
        this.originalHeight = 1.8f;
        this.originalWidth = 0.6f;
    }

    public void resetPlayer(EntityPlayer player)
    {
        player.eyeHeight = player.getDefaultEyeHeight();
        float height = originalHeight;
        float width = originalWidth;
        player.setSize(width, height);
        boolean fly = pokemob.getPokedexEntry().flys() || pokemob.getPokedexEntry().floats();
        if (fly && player.capabilities.allowFlying && player.worldObj.isRemote && !player.capabilities.isCreativeMode)
        {
            player.capabilities.allowFlying = false;
            player.sendPlayerAbilities();
        }
    }

    public void setPlayer(EntityPlayer player)
    {
        float height = pokemob.getSize() * entry.height;
        float width = pokemob.getSize() * entry.width;
        player.setSize(width, height);
        boolean fly = pokemob.getPokedexEntry().flys() || pokemob.getPokedexEntry().floats();
        if (fly && !player.capabilities.allowFlying && player.worldObj.isRemote)
        {
            player.capabilities.allowFlying = true;
            player.sendPlayerAbilities();
        }
    }

    public void onUpdate(EntityPlayer player)
    {
        EntityLivingBase poke = (EntityLivingBase) pokemob;
        poke.onUpdate();
        player.setHealth(poke.getHealth());
        PokePlayer.proxy.copyTransform((EntityLivingBase) pokemob, player);
        // TODO handle floating and swimming.
        if (pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys())
        {
            player.fallDistance = 0;
        }
    }
}
