package pokecube.core.world.gen.template;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.StructureEvent;

public class PokecubeTemplate extends Template
{
    public final String name;

    public PokecubeTemplate(String name)
    {
        this.name = name;
    }

    public PokecubeTemplate(Template from, String name)
    {
        this.read(from.writeToNBT(new NBTTagCompound()));
        this.name = name;
    }

    /** This takes the data stored in this instance and puts them into the
     * world. */
    @Override
    public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn)
    {
        ITemplateProcessor processor = new TemplateProcessor(worldIn, pos, placementIn);
        this.addBlocksToWorld(worldIn, pos, processor, placementIn, 2);
        MinecraftForge.EVENT_BUS.post(new StructureEvent.BuildStructure(pos, worldIn, name, getSize(), placementIn));
    }

    @Override
    public void addEntitiesToWorld(World worldIn, BlockPos pos, Mirror mirrorIn, Rotation rotationIn,
            @Nullable StructureBoundingBox aabb)
    {
        for (Template.EntityInfo template$entityinfo : this.entities)
        {
            BlockPos blockpos = transformedBlockPos(template$entityinfo.blockPos, mirrorIn, rotationIn).add(pos);

            if (aabb == null || aabb.isVecInside(blockpos))
            {
                NBTTagCompound nbttagcompound = template$entityinfo.entityData;
                Vec3d vec3d = transformedVec3d(template$entityinfo.pos, mirrorIn, rotationIn);
                Vec3d vec3d1 = vec3d.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
                NBTTagList nbttaglist = new NBTTagList();
                nbttaglist.appendTag(new NBTTagDouble(vec3d1.xCoord));
                nbttaglist.appendTag(new NBTTagDouble(vec3d1.yCoord));
                nbttaglist.appendTag(new NBTTagDouble(vec3d1.zCoord));
                nbttagcompound.setTag("Pos", nbttaglist);
                nbttagcompound.setUniqueId("UUID", UUID.randomUUID());
                Entity entity;

                try
                {
                    entity = EntityList.createEntityFromNBT(nbttagcompound, worldIn);
                }
                catch (Exception var15)
                {
                    entity = null;
                }

                if (entity != null)
                {
                    float f = 0;
                    switch (rotationIn)
                    {
                    case NONE:
                        switch (mirrorIn)
                        {
                        case LEFT_RIGHT:
                            f = 180;
                            break;
                        case NONE:
                            f = 0;
                            break;
                        default:
                            f = entity.getMirroredYaw(mirrorIn);
                            f -= entity.getRotatedYaw(rotationIn);
                            break;
                        }
                        break;
                    case CLOCKWISE_90:
                        switch (mirrorIn)
                        {
                        case NONE:
                            f = 90;
                            break;
                        case LEFT_RIGHT:
                            f = 270;
                            break;
                        default:
                            f = entity.getMirroredYaw(mirrorIn);
                            f -= entity.getRotatedYaw(rotationIn);
                            break;
                        }
                        break;
                    default:
                        f = entity.getMirroredYaw(mirrorIn);
                        f -= entity.getRotatedYaw(rotationIn);
                        break;
                    }
                    entity.setLocationAndAngles(vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, f, entity.rotationPitch);
                    StructureEvent.SpawnEntity event = new StructureEvent.SpawnEntity(entity, name);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.getToSpawn() != null) worldIn.spawnEntity(event.getToSpawn());
                }
            }
        }
    }

}
