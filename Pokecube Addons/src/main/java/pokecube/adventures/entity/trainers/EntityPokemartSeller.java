package pokecube.adventures.entity.trainers;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.events.handlers.EventsHandler;
import thut.api.maths.Vector3;

public class EntityPokemartSeller extends EntityTrainer
{
    public EntityPokemartSeller(World par1World)
    {
        super(par1World, TypeTrainer.merchant, 100);
        this.aiStates.setAIState(IHasNPCAIStates.PERMFRIENDLY, true);
        pokemobsCap.setType(TypeTrainer.merchant);
        pokemobsCap.friendlyCooldown = Integer.MAX_VALUE;
    }

    @Override
    public void populateBuyingList(EntityPlayer player)
    {
        pokemobsCap.setType(TypeTrainer.merchant);
        tradeList = new MerchantRecipeList();
        if (shouldrefresh) itemList = null;
        shouldrefresh = false;
        if (itemList == null)
        {
            itemList = new MerchantRecipeList();
            addRandomTrades();
        }
        tradeList.addAll(itemList);
    }

    @Override
    protected void addRandomTrades()
    {
        itemList.clear();
        itemList.addAll(pokemobsCap.getType().getRecipes(this));
    }

    @Override
    protected void initAI(Vector3 location, boolean stationary)
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(1, guardAI);
        if (location != null)
        {
            location.moveEntity(this);
            if (stationary) setStationary(location);
        }
        rewardsCap.getRewards().clear();
    }
}
