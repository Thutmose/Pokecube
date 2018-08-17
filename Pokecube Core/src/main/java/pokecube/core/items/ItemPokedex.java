/**
 *
 */
package pokecube.core.items;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.network.PacketHandler;
import thut.core.common.commands.CommandTools;

/** @author Manchou */
public class ItemPokedex extends Item
{
    public final boolean watch;

    public ItemPokedex(boolean watch, boolean registryname)
    {
        super();
        this.watch = watch;
        if (registryname)
        {
            this.setRegistryName(PokecubeMod.ID, watch ? "pokewatch" : "pokedex");
            this.setCreativeTab(PokecubeMod.creativeTabPokecube);
            this.setUnlocalizedName(this.getRegistryName().getResourcePath());
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand)
    {
        if (playerIn instanceof EntityPlayerMP)
        {
            Chunk chunk = playerIn.getEntityWorld().getChunkFromBlockCoords(playerIn.getPosition());
            PacketHandler.sendTerrainToClient(playerIn.getEntityWorld(), new ChunkPos(chunk.x, chunk.z),
                    (EntityPlayerMP) playerIn);
            PacketDataSync.sendInitPacket(playerIn, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket(playerIn, watch);
            Entity entityHit = target;
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            if (pokemob != null) PokecubePlayerDataHandler.getInstance().getPlayerData(playerIn)
                    .getData(PokecubePlayerStats.class).inspect(playerIn, pokemob);
            return true;
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if (!world.isRemote) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(player), player.getEntityWorld());
        if (!player.isSneaking())
        {
            showGui(player);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
        else
        {
            Vector3 hit = Tools.getPointedLocation(player, 6);
            if (hit != null)
            {
                Block block = hit.getBlockState(world).getBlock();
                if (block instanceof BlockHealTable)
                {
                    Vector4 loc = new Vector4(player);
                    TeleportHandler.setTeleport(loc, player.getCachedUniqueIdString());
                    if (!world.isRemote)
                    {
                        CommandTools.sendMessage(player, "pokedex.setteleport");
                        PacketDataSync.sendInitPacket(player, "pokecube-data");
                    }
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Vector3 hit = Vector3.getNewVector().set(pos);
        Block block = hit.getBlockState(worldIn).getBlock();
        if (block instanceof BlockHealTable)
        {
            Vector4 loc = new Vector4(playerIn);
            TeleportHandler.setTeleport(loc, playerIn.getCachedUniqueIdString());
            if (!worldIn.isRemote)
            {
                CommandTools.sendMessage(playerIn, "pokedex.setteleport");
                PacketDataSync.sendInitPacket(playerIn, "pokecube-data");
            }
            return EnumActionResult.SUCCESS;
        }

        if (playerIn.isSneaking() && !worldIn.isRemote)
        {
            ITextComponent message = CommandTools.makeTranslatedMessage("pokedex.locationinfo1", "green",
                    Database.spawnables.size());
            playerIn.sendMessage(message);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo2", "green",
                    Pokedex.getInstance().getEntries().size());
            playerIn.sendMessage(message);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo3", "green",
                    Pokedex.getInstance().getRegisteredEntries().size());
            playerIn.sendMessage(message);
        }

        if (!playerIn.isSneaking()) showGui(playerIn);
        return EnumActionResult.FAIL;
    }

    private void showGui(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP)
        {
            Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
            PacketHandler.sendTerrainToClient(player.getEntityWorld(), new ChunkPos(chunk.x, chunk.z),
                    (EntityPlayerMP) player);
            PacketDataSync.sendInitPacket(player, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket(player, watch);
            Entity entityHit = Tools.getPointedEntity(player, 16);
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            if (pokemob != null) PokecubePlayerDataHandler.getInstance().getPlayerData(player)
                    .getData(PokecubePlayerStats.class).inspect(player, pokemob);
        }
    }

}
