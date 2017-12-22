/**
 *
 */
package pokecube.core.items;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
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
    private String watchName;

    public ItemPokedex()
    {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab != getCreativeTab()) return;
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 8));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        if (watchName == null) watchName = super.getUnlocalizedName(stack).replaceAll("pokedex", "pokewatch");
        return stack.getItemDamage() == 8 ? watchName : super.getUnlocalizedName(stack);
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (!world.isRemote) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(player), player.getEntityWorld());
        if (!player.isSneaking())
        {
            showGui(player, itemstack);
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

    // 1.11
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
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

        if (!playerIn.isSneaking()) showGui(playerIn, stack);
        return EnumActionResult.FAIL;
    }

    private void showGui(EntityPlayer player, ItemStack stack)
    {
        if (!PokecubeCore.isOnClientSide())
        {
            Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
            PacketHandler.sendTerrainToClient(player.getEntityWorld(), new ChunkPos(chunk.x, chunk.z),
                    (EntityPlayerMP) player);
            PacketDataSync.sendInitPacket(player, "pokecube-stats");
            PacketPokedex.sendSecretBaseInfoPacket(player, stack.getItemDamage() != 8);
            Entity entityHit = Tools.getPointedEntity(player, 16);
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            if (pokemob != null) PokecubePlayerDataHandler.getInstance().getPlayerData(player)
                    .getData(PokecubePlayerStats.class).inspect(player, pokemob);
        }
    }

}
