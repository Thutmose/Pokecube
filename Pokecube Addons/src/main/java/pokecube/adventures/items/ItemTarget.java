package pokecube.adventures.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.adventures.events.TeamEventsHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.Vector4;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class ItemTarget extends Item
{
    public ItemTarget()
    {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer entityplayer, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        Vector3 hit = Vector3.getNewVector().set(pos);
        Block block = hit.getBlock(world);
        int meta = stack.getItemDamage();

        if (entityplayer.isSneaking() && !world.isRemote && meta == 0)
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(pos, entityplayer.dimension);
            String team = TeamManager.getInstance().getLandOwner(c);
            String playerTeam = world.getScoreboard().getPlayersTeam(entityplayer.getName()).getRegisteredName();
            if (team != null && team.equalsIgnoreCase(playerTeam) && TeamManager.getInstance()
                    .isAdmin(entityplayer.getName(), world.getScoreboard().getPlayersTeam(entityplayer.getName())))
            {
                ChunkCoordinate blockLoc = new ChunkCoordinate(pos, entityplayer.dimension);
                if (TeamManager.getInstance().isPublic(blockLoc))
                {
                    entityplayer.addChatMessage(new ChatComponentText("Set Block to Team Only"));
                    TeamManager.getInstance().unsetPublic(blockLoc);
                }
                else
                {
                    entityplayer.addChatMessage(new ChatComponentText("Set Block to Public Use"));
                    TeamManager.getInstance().setPublic(blockLoc);
                }
            }
            return true;
        }

        if (meta == 1 && block instanceof BlockWarpPad && !world.isRemote)
        {

            TileEntityWarpPad pad = (TileEntityWarpPad) hit.getTileEntity(world);
            if (entityplayer.isSneaking() && stack.hasTagCompound() && pad.canEdit(entityplayer))
            {
                pad.link = new Vector4(stack.getTagCompound().getCompoundTag("link"));
                entityplayer.addChatMessage(new ChatComponentText("linked pad to " + pad.link));
            }
            else
            {
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound linkTag = new NBTTagCompound();
                Vector4 link = new Vector4(hit.x, hit.y + 1, hit.z, entityplayer.dimension);
                link.writeToNBT(linkTag);
                stack.getTagCompound().setTag("link", linkTag);
                entityplayer.addChatMessage(new ChatComponentText("Saved location " + link));
            }
        }
        if (meta == 2 && !world.isRemote)
        {

            if (entityplayer.isSneaking())
            {

            }
            else
            {

            }
        }
        if (meta == 3 && entityplayer.isSneaking() && !world.isRemote)
        {
            if (stack.hasTagCompound())
            {
                if (!stack.getTagCompound().hasKey("pos1x"))
                {
                    hit.writeToNBT(stack.getTagCompound(), "pos1");
                    entityplayer.addChatMessage(new ChatComponentText("First Position " + hit));
                }
                else
                {
                    String s = stack.getTagCompound().getString("biome");
                    BiomeType type = BiomeType.getBiome(s);
                    TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(entityplayer);

                    Vector3 pos1 = Vector3.readFromNBT(stack.getTagCompound(), "pos1");
                    stack.getTagCompound().removeTag("pos1x");
                    Vector3 pos2 = hit;

                    double xMin = Math.min(pos1.x, pos2.x);
                    double yMin = Math.min(pos1.y, pos2.y);
                    double zMin = Math.min(pos1.z, pos2.z);
                    double xMax = Math.max(pos1.x, pos2.x);
                    double yMax = Math.max(pos1.y, pos2.y);
                    double zMax = Math.max(pos1.z, pos2.z);
                    double x, y, z;

                    for (x = xMin; x <= xMax; x++)
                        for (y = yMin; y <= yMax; y++)
                            for (z = zMin; z <= zMax; z++)
                            {
                                pos1.set(x, y, z);
                                t = TerrainManager.getInstance().getTerrian(world, pos1);
                                t.setBiome(pos1, type.getType());
                            }
                    try
                    {
                        entityplayer.addChatMessage(
                                new ChatComponentText("Second Position " + hit + ", setting all in between to " + s));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
        return super.onItemUse(stack, entityplayer, world, pos, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        int meta = itemstack.getItemDamage();

        Vector3 p = Vector3.getNewVector().set(player, false);
        Vector3 d = Vector3.getNewVector().set(player.getLookVec());

        List<Entity> e = p.allEntityLocationExcluding(2, 1, d, p, world, player);

        for (Object o : e)
        {
            if (o instanceof IPokemob)
            {
                IPokemob poke = (IPokemob) o;
                PokedexEntry entry = poke.getPokedexEntry();
                if (poke.getPokemonOwner() != player) continue;

                if (entry.getName().equalsIgnoreCase("deoxys"))
                {
                    poke.changeForme("deoxys speed");
                }
                if (entry.getName().equalsIgnoreCase("deoxys speed"))
                {
                    poke.changeForme("deoxys attack");
                }
                if (entry.getName().equalsIgnoreCase("deoxys attack"))
                {
                    poke.changeForme("deoxys defense");
                }
                if (entry.getName().equalsIgnoreCase("deoxys defense"))
                {
                    poke.changeForme("deoxys");
                }
            }
        }
        if (e != null && !e.isEmpty()) return itemstack;

        if (world.isRemote)
        {

            if (meta == 0 && player.isSneaking())
            {
                TeamEventsHandler.shouldRenderVolume = !TeamEventsHandler.shouldRenderVolume;
            }

            if (meta == 2)
            {
                // WorldTerrain t =
                // TerrainManager.getInstance().getTerrain(world);
                // player.addChatMessage(new ChatComponentText("There are
                // "+t.chunks.size()+" loaded terrain segments on your
                // client"));
            }
            if (meta == 3 && world.isRemote && !player.isSneaking())
            {
                player.openGui(PokecubeAdv.instance, 5, player.worldObj, 0, 0, 0);
            }
            return itemstack;
        }

        if (player.isSneaking() && meta == 10)
        {
            TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(player);
            t.refresh(world);
        }
        else if (meta == 1)
        {

        }
        else if (meta == 2)
        {

        }
        else if (meta == 3)
        {

        }
        else if (!player.isSneaking())
        {
            Vector3 location = Vector3.getNewVector().set(player).add(Vector3.getNewVector().set(player.getLookVec()))
                    .add(0, 1.62, 0);
            EntityTarget t = new EntityTarget(world);
            location.moveEntity(t);
            world.spawnEntityInWorld(t);
        }

        return itemstack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 3));
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getItemDamage();

        if (i == 1) return "item.warplinker";
        if (i == 3) { return "item.biomeSetter"; }

        return super.getUnlocalizedName();
    }

}
