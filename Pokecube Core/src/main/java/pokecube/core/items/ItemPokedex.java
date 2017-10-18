/**
 *
 */
package pokecube.core.items;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.commands.CommandTools;

/** @author Manchou */
public class ItemPokedex extends Item
{
    public ItemPokedex()
    {
        super();
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
            showGui(player);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
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
            if (worldIn.isRemote) CommandTools.sendMessage(playerIn, "pokedex.setteleport");
            Vector4 loc = new Vector4(playerIn);
            loc.y++;
            PokecubeSerializer.getInstance().setTeleport(loc, playerIn.getCachedUniqueIdString());
            PokecubeSerializer.getInstance().save();
            if (!worldIn.isRemote)
            {
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
        // else structureGenTest(playerIn, worldIn, pos, side);
        return EnumActionResult.FAIL;
    }

    public void structureGenTest(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side)
    {
        Block b = worldIn.getBlockState(pos).getBlock();
        // WorldServer world = (WorldServer) worldIn;
        int c = -5;
        int r = 60;
        System.out.println(side);
        if (b == Blocks.GOLD_BLOCK)
        {

        }
        else if (b == Blocks.DIAMOND_BLOCK)
        {
            for (int i = -r; i < c; i++)
                for (int j = 0; j < 100; j++)
                    for (int k = -r; k < c; k++)
                    {
                        BlockPos pos2 = new BlockPos(i, 15, k);
                        if ((pos2.getX() == -21 && pos2.getZ() == -21)) continue;
                        worldIn.setBlockState(new BlockPos(i, j, k),
                                j < 4 ? Blocks.GRASS.getDefaultState() : Blocks.AIR.getDefaultState());
                    }
        }
    }

    private void showGui(EntityPlayer player)
    {
        if (!PokecubeCore.isOnClientSide())
        {
            TerrainSegment s = TerrainManager.getInstance().getTerrainForEntity(player);
            PacketSyncTerrain.sendTerrain(player, s.chunkX, s.chunkY, s.chunkZ, s);
            PacketPokedex.sendSecretBaseInfoPacket(player);
            PacketDataSync.sendInitPacket(player, "pokecube-stats");
            player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, player.getEntityWorld(), 0, 0, 0);
            Entity entityHit = Tools.getPointedEntity(player, 16);
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityHit);
            if (pokemob != null) PokecubePlayerDataHandler.getInstance().getPlayerData(player)
                    .getData(PokecubePlayerStats.class).inspect(player, pokemob);
        }
    }

}
