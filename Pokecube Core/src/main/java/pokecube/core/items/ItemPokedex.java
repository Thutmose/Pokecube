/**
 *
 */
package pokecube.core.items;

import net.minecraft.block.Block;
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
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.House1;
import net.minecraft.world.gen.structure.StructureVillagePieces.WoodHut;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

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
        if (!world.isRemote) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(player), player.worldObj);
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
            playerIn.addChatMessage(message);
            message = CommandTools.makeTranslatedMessage("pokedex.locationinfo2", "green",
                    Pokedex.getInstance().getEntries().size());
            playerIn.addChatMessage(message);
        }

        if (!playerIn.isSneaking()) showGui(playerIn);
        // else structureGenTest(playerIn, worldIn, pos, side);
        return EnumActionResult.FAIL;
    }

    public void structureGenTest(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side)
    {
        Block b = worldIn.getBlockState(pos).getBlock();
        int c = -5;
        int r = 60;
        System.out.println(side);
        if (b == Blocks.GOLD_BLOCK)
        {
            String templateName = "pokemart";
            Template template = PokecubeTemplates.getTemplate(templateName);
            BlockPos size = template.getSize();
            int x = -20;
            int y = 64;
            int z = -20;
            StructureBoundingBox temp = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, size.getX(),
                    size.getY(), size.getZ(), side);
            BlockPos pos1 = new BlockPos(x, y, z);
            TemplateStructure component = new TemplateStructure(templateName, pos1, side);
            temp = component.getBoundingBox();
            component.offset = -2;
            StructureBoundingBox temp1 = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 9, 9, 6,
                    side);
            House1 house = new House1(null, 0, itemRand, temp1, side);
            StructureBoundingBox temp2 = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 4, 6, 5,
                    side);
            WoodHut house2 = new WoodHut(null, 0, itemRand, temp2, side);
            c = -10;
            r = 50;
            x = -r;
            z = -r;
            boolean others = false;
            System.out.println(temp);

            // for (int i = x; i < c; i++)
            // for (int k = z; k < c; k++)

            for (int i = temp.minX; i <= temp.maxX; i++)
                for (int k = temp.minZ; k <= temp.maxZ; k++)
                {
                    BlockPos pos2 = new BlockPos(i, 15, k);
                    if ((pos2.getX() == -21 && pos2.getZ() == -21)) continue;
                    StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(i, 1, k, 0, 0, 0, 1,
                            512, 1, side);
                    component.addComponentParts(worldIn, itemRand, box);
                    if (!others) continue;
                    worldIn.setBlockState(pos2, Blocks.STONE.getDefaultState());
                    house.addComponentParts(worldIn, itemRand, box);
                    worldIn.setBlockState(pos2.up(10), Blocks.STONE.getDefaultState());
                    house2.addComponentParts(worldIn, itemRand, box);
                }
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
        if (PokecubeCore.isOnClientSide())
        {
            player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, player.getEntityWorld(), 0, 0, 0);
        }
        else
        {
            TerrainSegment s = TerrainManager.getInstance().getTerrainForEntity(player);
            PacketSyncTerrain.sendTerrain(player, s.chunkX, s.chunkY, s.chunkZ, s);
            PacketPokedex.sendVillageInfoPacket(player);
            PacketDataSync.sendInitPacket(player, "pokecube-stats");
        }
    }

}
