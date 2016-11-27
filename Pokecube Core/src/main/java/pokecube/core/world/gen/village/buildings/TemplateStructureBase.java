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
        setup(template, pos, placeSettings, dir);
    }

    protected void setup(Template template, BlockPos pos, PlacementSettings settings, EnumFacing dir)
    {
        this.template = template;
        this.templatePosition = pos;
        this.placeSettings = settings;
        this.setBoundingBoxFromTemplate(dir);
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

    private void offsetBox(StructureBoundingBox boxIn)
    {
        Rotation rotation = this.placeSettings.getRotation();
        Mirror mirror = this.placeSettings.getMirror();
        BlockPos blockpos = this.template.transformedSize(rotation);
        int dx = 0;
        int dz = 0;
        int dpx = 0, dpz = 0;
        switch (rotation)
        {
        case NONE:
            switch (mirror)
            {
            case LEFT_RIGHT:
                dx = -1;
                dz = -1;
                dpz = blockpos.getZ() - 2;
                dpx = -1;
                break;
            case NONE:
                dx = -1;
                dz = 1;
                dpx = -1;
                dpz = 1;
                break;
            default:
                break;
            }
            break;
        case CLOCKWISE_90:
            switch (mirror)
            {
            case NONE:
                dx = -1;
                dz = -1;
                dpx = blockpos.getX() - 2;
                dpz = -1;
                break;
            case LEFT_RIGHT:
                dz = -1;
                dx = 1;
                dpx = 1;
                dpz = -1;
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        templatePosition = templatePosition.add(dpx, 0, dpz);
        boxIn.offset(dx, 0, dz);
    }

    /** second Part of Structure generating, this for example places Spiderwebs,
     * Mob Spawners, it closes Mineshafts at the end, it adds Fences... */
    public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox boxIn)
    {
        try
        {
            averageGroundLevel = -1;
            if (this.averageGroundLevel < 0)
            {
                this.averageGroundLevel = this.getAverageGroundLevel(worldIn, boxIn);
                if (this.averageGroundLevel < 0) { return true; }
            }
            StructureBoundingBox buildBox = new StructureBoundingBox(boundingBox);
            buildBox.offset(0, this.averageGroundLevel - buildBox.minY + getOffset(), 0);
            this.templatePosition = new BlockPos(templatePosition.getX(), buildBox.minY, templatePosition.getZ());
            this.placeSettings.setIgnoreEntities(false).setBoundingBox(boxIn);
            this.template.addBlocksToWorld(worldIn, this.templatePosition, this.placeSettings);
            Map<BlockPos, String> map = this.template.getDataBlocks(this.templatePosition, this.placeSettings);
            for (BlockPos blockpos : map.keySet())
            {
                String s = (String) map.get(blockpos);
                this.handleDataMarker(s, blockpos, worldIn, randomIn, buildBox);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    public abstract Template getTemplate();

    public abstract int getOffset();

    protected abstract void handleDataMarker(String marker, BlockPos pos, World world, Random rand,
            StructureBoundingBox box);

    public void setBoundingBoxFromTemplate(EnumFacing dir)
    {
        Template t = PokecubeTemplates.getTemplate(PokecubeTemplates.POKEMART);
        BlockPos size = t.getSize();
        boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(templatePosition.getX(),
                templatePosition.getY(), templatePosition.getZ(), 0, 0, 0, size.getX(), size.getY(), size.getZ(), dir);
        templatePosition = new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        offsetBox(boundingBox);
    }

    public void offset(int x, int y, int z)
    {
        super.offset(x, y, z);
        this.templatePosition = this.templatePosition.add(x, y, z);
    }

}
