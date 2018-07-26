package pokecube.adventures.ai.tasks;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import thut.api.maths.Vector3;

public class AIMate extends AITrainerBase
{
    EntityAgeable                        foundMate = null;
    final Class<? extends EntityAgeable> targetClass;
    final EntityAgeable                  thisEntity;
    EntityAgeable                        child     = null;
    int                                  mateTimer = -1;

    public AIMate(EntityLivingBase trainer, Class<? extends EntityAgeable> targetClass)
    {
        super(trainer);
        this.targetClass = targetClass;
        if (trainer instanceof EntityAgeable) thisEntity = (EntityAgeable) trainer;
        else thisEntity = null;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (shouldRun())
        {
            foundMate = this.world.findNearestEntityWithinAABB(targetClass,
                    this.thisEntity.getEntityBoundingBox().grow(8.0D, 3.0D, 8.0D), this.thisEntity);
            if (foundMate == null)
            {
                thisEntity.setGrowingAge(600);
                return;
            }
            if (this.world.getEntitiesWithinAABB(targetClass,
                    this.thisEntity.getEntityBoundingBox().grow(16.0D, 10.0D, 16.0D)).size() > 3)
            {
                thisEntity.setGrowingAge(6000);
                return;
            }
            child = this.thisEntity.createChild(this.foundMate);
            this.thisEntity.setGrowingAge(6000);
            this.foundMate.setGrowingAge(6000);
            final BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(
                    thisEntity, foundMate, child);
            if (MinecraftForge.EVENT_BUS.post(event) || event.getChild() == null) { return; }
            child = event.getChild();
            child.setGrowingAge(-24000);
            mateTimer = 50;
        }
        if (child != null && foundMate != null)
        {
            if (mateTimer-- <= 0)
            {
                thisEntity.getNavigator().tryMoveToEntityLiving(foundMate, thisEntity.getAIMoveSpeed());
                foundMate.getNavigator().tryMoveToEntityLiving(thisEntity, foundMate.getAIMoveSpeed());
            }
            else
            {
                Vector3 loc = Vector3.getNewVector().set(this.thisEntity.getLookVec());
                loc.y = 0;
                loc.norm();
                child.setLocationAndAngles(this.thisEntity.posX + loc.x, this.thisEntity.posY,
                        this.thisEntity.posZ + loc.z, 0.0F, 0.0F);
                this.world.spawnEntity(child);
                this.world.setEntityState(child, (byte) 12);
                child = null;
                foundMate = null;
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        return thisEntity != null && thisEntity.getGrowingAge() == 0 && trainer.getGender() == 2
                && aiTracker.getAIState(IHasNPCAIStates.MATES);
    }
}
