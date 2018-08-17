package pokecube.adventures.ai.helper;

import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.trainers.EntityTrainer;

public class PathNavigateTrainer extends PathNavigateGround
{
    private final EntityTrainer trainer;

    public PathNavigateTrainer(EntityTrainer entitylivingIn, World worldIn)
    {
        super(entitylivingIn, worldIn);
        this.trainer = entitylivingIn;
        this.setBreakDoors(true);
        this.setEnterDoors(true);
    }

    @Override
    protected boolean canNavigate()
    {
        if (trainer.aiStates.getAIState(IHasNPCAIStates.STATIONARY))
        {
            BlockPos pos = trainer.guardAI.capability.getPrimaryTask().getPos();
            if (pos != null && trainer.getDistanceSq(pos) > 1) return true;
            return false;
        }
        return super.canNavigate();
    }

}
