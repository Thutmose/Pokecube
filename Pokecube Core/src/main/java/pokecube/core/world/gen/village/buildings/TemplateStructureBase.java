package pokecube.core.world.gen.village.buildings;

import java.util.Map;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.world.gen.template.PokecubeTemplate;
import pokecube.core.world.gen.template.PokecubeTemplates;

public abstract class TemplateStructureBase extends Village
{
    protected Template          template;
    protected PlacementSettings placeSettings;
    protected BlockPos          templatePosition;

    public static Rotation getFromDir(EnumFacing dir)
    {
        dir = dir.getOpposite();
        Rotation rotation = Rotation.CLOCKWISE_180;
        if (dir == EnumFacing.NORTH) rotation = Rotation.NONE;
        if (dir == EnumFacing.EAST) rotation = Rotation.CLOCKWISE_90;
        if (dir == EnumFacing.WEST) rotation = Rotation.COUNTERCLOCKWISE_90;
        return rotation;
    }

    public TemplateStructureBase()
    {
        placeSettings = new PlacementSettings().setIgnoreEntities(false).setReplacedBlock(null);
    }

    public TemplateStructureBase(String type, BlockPos pos, EnumFacing dir)
    {
        this();

        Rotation rotation;
        Mirror mirror;
        dir = dir.getOpposite();
        if (dir == null)
        {
            rotation = Rotation.NONE;
            mirror = Mirror.NONE;
        }
        else
        {
            switch (dir)
            {
            case SOUTH:
                mirror = Mirror.LEFT_RIGHT;
                rotation = Rotation.NONE;
                break;
            case WEST:
                mirror = Mirror.LEFT_RIGHT;
                rotation = Rotation.CLOCKWISE_90;
                break;
            case EAST:
                mirror = Mirror.NONE;
                rotation = Rotation.CLOCKWISE_90;
                break;
            default:
                mirror = Mirror.NONE;
                rotation = Rotation.NONE;
            }
        }
        placeSettings.setRotation(rotation);
        placeSettings.setMirror(mirror);
        Template template = PokecubeTemplates.getTemplate(type);
        setup(template, pos, placeSettings);
    }

    protected void setup(Template template, BlockPos pos, PlacementSettings settings)
    {
        this.template = template;
        this.templatePosition = pos;
        this.placeSettings = settings;
    }

    /** (abstract) Helper method to write subclass data to NBT */
    protected void writeStructureToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setInteger("TPX", this.templatePosition.getX());
        tagCompound.setInteger("TPY", this.templatePosition.getY());
        tagCompound.setInteger("TPZ", this.templatePosition.getZ());
        tagCompound.setString("TN", ((PokecubeTemplate) getTemplate()).name);
        tagCompound.setByte("Rot", (byte) placeSettings.getRotation().ordinal());
    }

    /** (abstract) Helper method to read subclass data from NBT */
    protected void readStructureFromNBT(NBTTagCompound tagCompound)
    {
        this.templatePosition = new BlockPos(tagCompound.getInteger("TPX"), tagCompound.getInteger("TPY"),
                tagCompound.getInteger("TPZ"));
        String name = tagCompound.getString("TN");
        if (name.isEmpty()) name = PokecubeTemplates.POKECENTER;
        try
        {
            placeSettings.setRotation(Rotation.values()[tagCompound.getByte("Rot")]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        template = PokecubeTemplates.getTemplate(name);
    }

    int averageGroundLevel;

    /** second Part of Structure generating, this for example places Spiderwebs,
     * Mob Spawners, it closes Mineshafts at the end, it adds Fences... */
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
    {
        try
        {
            averageGroundLevel = -1;
            if (this.averageGroundLevel < 0)
            {
                this.averageGroundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);
                if (this.averageGroundLevel < 0) { return true; }
            }
            boundingBox.offset(0, this.averageGroundLevel - boundingBox.minY + getOffset(), 0);
            this.templatePosition = new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            structureBoundingBoxIn.minY = boundingBox.minY;
            this.placeSettings.setIgnoreEntities(true).setBoundingBox(structureBoundingBoxIn);
            this.template.addBlocksToWorld(worldIn, this.templatePosition, this.placeSettings);
            Map<BlockPos, String> map = this.template.getDataBlocks(this.templatePosition, this.placeSettings);
            for (BlockPos blockpos : map.keySet())
            {
                String s = (String) map.get(blockpos);
                this.handleDataMarker(s, blockpos, worldIn, randomIn, boundingBox);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    protected abstract Template getTemplate();

    protected abstract int getOffset();

    protected abstract void handleDataMarker(String marker, BlockPos pos, World world, Random rand,
            StructureBoundingBox box);

    public StructureBoundingBox setBoundingBoxFromTemplate(StructureBoundingBox structureBoundingBoxIn)
    {
        Rotation rotation = this.placeSettings.getRotation();
        Mirror mirror = this.placeSettings.getMirror();
        System.out.println(mirror + " " + rotation);
        BlockPos blockpos = this.template.transformedSize(rotation);
        boundingBox = new StructureBoundingBox(0, 0, 0, blockpos.getX(), blockpos.getY() - 1, blockpos.getZ());
        switch (rotation)
        {
        case NONE:
            switch (mirror)
            {
            case LEFT_RIGHT:
                boundingBox.offset(0, 0, blockpos.getZ() - 1);
                break;
            default:
                break;
            }
            break;
        case CLOCKWISE_90:
            switch (mirror)
            {
            case NONE:
                boundingBox.offset(blockpos.getX() - 1, 0, 0);
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        boundingBox.offset(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());
        return boundingBox;
    }

    public void offset(int x, int y, int z)
    {
        super.offset(x, y, z);
        this.templatePosition = this.templatePosition.add(x, y, z);
    }

}
