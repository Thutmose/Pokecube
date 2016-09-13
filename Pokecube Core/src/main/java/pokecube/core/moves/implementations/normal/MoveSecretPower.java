package pokecube.core.moves.implementations.normal;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.TreeRemover;
import pokecube.core.moves.templates.Move_Utility;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class MoveSecretPower extends Move_Utility
{
    public static Map<String, Vector4> pendingBaseLocations = Maps.newHashMap();

    public MoveSecretPower()
    {
        super("secretpower");
    }

    @Override
    public void doWorldAction(IPokemob attacker, Vector3 location)
    {
        if (!PokecubeMod.pokemobsDamageBlocks) return;
        if (attacker.getPokemonAIState(IMoveConstants.ANGRY)) return;
        if (!(attacker.getPokemonOwner() instanceof EntityPlayerMP)) return;
        EntityPlayerMP owner = (EntityPlayerMP) attacker.getPokemonOwner();
        Block b = location.getBlock(owner.worldObj);
        if (!(Mod_Pokecube_Helper.getTerrain().contains(b) || TreeRemover.woodTypes.contains(b)))
        {
            TextComponentTranslation message = new TextComponentTranslation("pokemob.createbase.deny.wrongloc");
            owner.addChatMessage(message);
            location.setBlock(owner.worldObj, Blocks.GOLD_BLOCK.getDefaultState());
            return;
        }
        if (owner instanceof EntityPlayer)
        {
            BreakEvent evt = new BreakEvent(owner.getEntityWorld(), location.getPos(),
                    location.getBlockState(owner.getEntityWorld()), owner);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled())
            {
                TextComponentTranslation message = new TextComponentTranslation("pokemob.createbase.deny.noperms");
                owner.addChatMessage(message);
            }
        }
        pendingBaseLocations.put(attacker.getPokemonOwnerName(),
                new Vector4(location.x, location.y, location.z, owner.dimension));
        TextComponentTranslation message = new TextComponentTranslation("pokemob.createbase.confirm",
                location.set(location.getPos()));
        message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/pokebase confirm " + owner.posX + " " + owner.posY + " " + owner.posZ));
        owner.addChatMessage(message);
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        // TODO before super call, add in the needed stats/status/change effects
        // based on terrain.
        super.preAttack(packet);
    }

    @Override
    public IMoveAnimation getAnimation(IPokemob user)
    {
        // TODO make this return animations for the relevant attacks based on
        // location instead.
        return super.getAnimation();
    }
}
