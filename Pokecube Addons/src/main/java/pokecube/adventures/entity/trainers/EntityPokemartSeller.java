package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.items.ItemTM;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.items.vitamins.VitaminManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class EntityPokemartSeller extends EntityTrainer
{
    static TypeTrainer merchant = new TypeTrainer("Merchant");

    public EntityPokemartSeller(World par1World)
    {
        super(par1World, merchant, 100);
        this.setAIState(PERMFRIENDLY, true);
        friendlyCooldown = Integer.MAX_VALUE;
    }

    @Override
    protected void addRandomTrades()
    {
        itemList.clear();
        int num = HeldItemHandler.megaVariants.size();
        Set<Object> added = Sets.newHashSet();
        num = cubeList.size();
        if (!cubeList.isEmpty()) for (int i = 0; i < num; i++)
        {
            CubeTrade trade = cubeList.get(i);
            if (added.contains(trade)) continue;
            added.add(trade);
            itemList.add(trade.getTrade());
        }
        for (Map.Entry<Integer, ItemVitamin> entry : VitaminManager.vitaminItems.entrySet())
        {
            ItemStack in = new ItemStack(Items.emerald);
            in.stackSize = Config.instance.vitaminCost;
            itemList.add(new MerchantRecipe(in, new ItemStack(entry.getValue())));
        }
        num = HeldItemHandler.megaVariants.size();
        for (int i = 0; i < num; i++)
        {
            String name = HeldItemHandler.megaVariants.get(i);
            if (!added.contains(name))
            {
                ItemStack output = PokecubeItems.getStack(name);
                if (output == null) continue;
                added.add(name);
                ItemStack in1 = new ItemStack(Items.emerald);
                int size = Config.instance.megaCost;
                if (name.endsWith("orb")) size = Config.instance.orbCost;
                else if (name.endsWith("charm")) size = Config.instance.shinyCost;
                in1.stackSize = (size & 63);
                ItemStack in2 = null;
                if (size > 64)
                {
                    in2 = in1.copy();
                    in1.stackSize = 64;
                    in2.stackSize = ((size - 64) & 63);
                    if (size - 64 >= 64) in2.stackSize = 64;
                }
                else if (size == 64)
                {
                    in1.stackSize = 64;
                }
                itemList.add(new MerchantRecipe(in1, in2, output));
            }
        }
        added.clear();
        num = rand.nextInt(3);
        ArrayList<String> moves = Lists.newArrayList(MovesUtils.moves.keySet());
        int randNum = rand.nextInt(moves.size());
        for (int i = 0; i < num; i++)
        {
            int index = (randNum + i) % moves.size();
            String name = moves.get(index);
            if (added.contains(name)) continue;
            added.add(name);
            ItemStack tm = PokecubeItems.getStack("tm");
            ItemStack in = new ItemStack(Items.emerald);
            in.stackSize = Config.instance.tmCost;
            ItemTM.addMoveToStack(name, tm);
            itemList.add(new MerchantRecipe(in, tm));
        }
        added.clear();
        PokeType type = PokeType.values()[rand.nextInt(PokeType.values().length)];
        if (type != PokeType.unknown)
        {
            ItemStack badge = PokecubeItems.getStack("badge" + type);
            if (badge != null)
            {
                ItemStack in1 = new ItemStack(Items.emerald);
                int size = Config.instance.badgeCost;
                in1.stackSize = ((size / 2) & 63);
                if (size / 2 == 64) in1.stackSize = 64;
                itemList.add(new MerchantRecipe(badge, in1));
            }
        }
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
    }
}
