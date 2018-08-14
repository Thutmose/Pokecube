package pokecube.core.database.recipes;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class PokemobMoveRecipeParser implements IRecipeParser
{
    private static final QName MOVENAME   = new QName("move");
    private static final QName HUNGERCOST = new QName("cost");
    private static final QName OREDICT    = new QName("oreDict");
    private static final QName SIZE       = new QName("n");

    public static class WrappedSizedIngredient extends Ingredient
    {
        public final Ingredient wrapped;
        public final int        size;

        public WrappedSizedIngredient(Ingredient wrapped, int size)
        {
            this.wrapped = wrapped;
            this.size = size;
        }

        @Override
        public boolean apply(ItemStack arg)
        {
            return wrapped.apply(arg) && arg.getCount() == size;
        }
    }

    private static class CustomShapessOreRecipe extends ShapelessOreRecipe
    {
        public CustomShapessOreRecipe(ResourceLocation name, ItemStack result, Object... recipe)
        {
            super(name, result, new Object[0]);
            for (Object in : recipe)
            {
                int size = 1;
                if (in instanceof ItemStack) size = ((ItemStack) in).getCount();

                Ingredient ing = null;

                if (in instanceof List)
                {
                    List<?> list = (List<?>) in;
                    list.removeIf(new Predicate<Object>()
                    {
                        @Override
                        public boolean test(Object t)
                        {
                            return !CompatWrapper.isValid((ItemStack) t);
                        }
                    });
                    ItemStack[] stacks = new ItemStack[list.size()];
                    for (int i = 0; i < stacks.length; i++)
                    {
                        stacks[i] = (ItemStack) list.get(i);
                        size = stacks[i].getCount();
                    }
                    ing = Ingredient.fromStacks(stacks);
                }
                else
                {
                    if (size == 0) ing = null;
                    else CraftingHelper.getIngredient(in);
                }

                if (ing != null)
                {
                    input.add(new WrappedSizedIngredient(ing, size));
                    this.isSimple &= ing.isSimple();
                }
                else
                {
                    String ret = "Invalid shapeless ore recipe: ";
                    for (Object tmp : recipe)
                    {
                        ret += tmp + ", ";
                    }
                    ret += output;
                    throw new RuntimeException(ret);
                }
            }
        }

        /** Used to check if a recipe matches current crafting inventory */
        @Override
        public boolean matches(InventoryCrafting inv, World world)
        {
            int ingredientCount = 0;
            List<ItemStack> items = Lists.newArrayList();
            for (int i = 0; i < inv.getSizeInventory(); ++i)
            {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty())
                {
                    ++ingredientCount;
                    items.add(itemstack);
                }
            }
            if (ingredientCount != this.input.size()) return false;
            return RecipeMatcher.findMatches(items, this.input) != null;
        }

    }

    public static class WrappedRecipeMove implements IMoveAction
    {
        public final IMoveAction parent;
        public final IMoveAction other;

        public WrappedRecipeMove(IMoveAction parent, IMoveAction other)
        {
            this.parent = parent;
            this.other = other;
        }

        @Override
        public boolean applyEffect(IPokemob user, Vector3 location)
        {
            // Only applies other action if parent action failed.
            return parent.applyEffect(user, location) || other.applyEffect(user, location);
        }

        @Override
        public String getMoveName()
        {
            return parent.getMoveName();
        }

        @Override
        public void init()
        {
            parent.init();
            other.init();
        }

    }

    public static class RecipeMove implements IMoveAction
    {
        public final String     name;
        public final IRecipe    recipe;
        public final int        hungerCost;
        final InventoryCrafting inventory;
        final Container         handler;

        public RecipeMove(XMLRecipe recipe)
        {
            this.name = recipe.values.get(MOVENAME);
            this.hungerCost = Integer.parseInt(recipe.values.get(HUNGERCOST));
            System.out.println(name + " " + hungerCost);
            this.handler = new Container()
            {
                @Override
                public boolean canInteractWith(EntityPlayer playerIn)
                {
                    return false;
                }
            };
            this.inventory = new InventoryCrafting(handler, 3, 3);
            ItemStack output = XMLRecipeHandler.getStack(recipe.output);
            List<Object> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                if (xml.values.containsKey(OREDICT))
                {
                    List<ItemStack> items = OreDictionary.getOres(xml.values.get(OREDICT));
                    List<ItemStack> oreInputs = Lists.newArrayList();
                    for (ItemStack stack : items)
                    {
                        int size = 1;
                        if (xml.values.containsKey(SIZE))
                        {
                            size = Integer.parseInt(xml.values.get(SIZE));
                        }
                        if (!stack.isEmpty())
                        {
                            stack = stack.copy();
                            stack.setCount(size);
                            oreInputs.add(stack);
                        }
                    }
                    PokecubeMod.log(oreInputs + "");
                    if (!oreInputs.isEmpty()) inputs.add(oreInputs);
                    else throw new IllegalArgumentException(
                            "No Items found registered for ore name:" + xml.values.get(OREDICT));
                }
                else inputs.add(XMLRecipeHandler.getStack(xml));
            }
            boolean failed = output.isEmpty();
            for (Object o : inputs)
                failed = failed || o == null;
            if (failed) { throw new NullPointerException("output: " + output + " inputs: " + inputs); }
            this.recipe = new CustomShapessOreRecipe(new ResourceLocation(PokecubeMod.ID, "loaded"), output,
                    inputs.toArray());
        }

        @Override
        public boolean applyEffect(IPokemob user, Vector3 location)
        {
            return attemptCraft(user, location) || attemptWorldCraft(user, location);
        }

        public boolean attemptWorldCraft(IPokemob user, Vector3 location)
        {
            World world = user.getEntity().getEntityWorld();
            IBlockState state = location.getBlockState(world);
            if (canCraftBlocks(world, location.getPos(), state)) return true;
            for (EnumFacing dir : EnumFacing.VALUES)
            {
                BlockPos pos = location.getPos().offset(dir);
                if (canCraftBlocks(world, pos, world.getBlockState(pos))) return true;
            }
            return false;
        }

        @SuppressWarnings("deprecation")
        public boolean canCraftBlocks(World world, BlockPos pos, IBlockState state)
        {
            ItemStack stack = Move_Basic.createStackedBlock(state);
            if (!CompatWrapper.isValid(stack)) return false;
            for (int i = 0; i < 9; i++)
            {
                inventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
            inventory.setInventorySlotContents(0, stack);
            if (recipe.matches(inventory, world))
            {
                stack = recipe.getCraftingResult(inventory);
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block != null)
                {
                    world.setBlockState(pos, block.getStateFromMeta(stack.getItemDamage()));
                }
                else
                {
                    world.setBlockToAir(pos);
                    world.spawnEntity(
                            new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
                }
                return true;
            }
            return false;
        }

        public boolean attemptCraft(IPokemob attacker, Vector3 location)
        {
            World world = attacker.getEntity().getEntityWorld();
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, location.getAABB().grow(1));
            if (!items.isEmpty())
            {
                List<ItemStack> stacks = Lists.newArrayList();
                Set<EntityItem> valid = Sets.newHashSet();
                for (EntityItem item : items)
                {
                    ItemStack stack = item.getItem();
                    if (CompatWrapper.isValid(stack))
                    {
                        stacks.add(stack);
                        valid.add(item);
                    }
                }
                for (int i = 0; i < 9; i++)
                {
                    inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
                int num = Math.min(stacks.size(), 9);
                for (int i = 0; i < num; i++)
                {
                    inventory.setInventorySlotContents(i, stacks.get(i));
                }
                boolean crafted = false;
                if (recipe.matches(inventory, world))
                {
                    for (EntityItem item : valid)
                    {
                        item.setDead();
                    }
                    ItemStack stack = recipe.getCraftingResult(inventory);
                    EntityItem item = new EntityItem(world, location.x, location.y, location.z, stack);
                    crafted = world.spawnEntity(item);
                    for (ItemStack remain : recipe.getRemainingItems(inventory))
                    {
                        if (CompatWrapper.isValid(remain))
                        {
                            item = new EntityItem(world, location.x, location.y, location.z, remain);
                            world.spawnEntity(item);
                        }
                    }
                }
                return crafted;
            }
            return false;
        }

        @Override
        public String getMoveName()
        {
            return name;
        }

    }

    public PokemobMoveRecipeParser()
    {
    }

    @Override
    public void manageRecipe(XMLRecipe recipe) throws NullPointerException
    {
        IMoveAction action = new RecipeMove(recipe);
        if (MoveEventsHandler.customActions.containsKey(action.getMoveName()))
        {
            action = new WrappedRecipeMove(MoveEventsHandler.customActions.get(action.getMoveName()), action);
        }
        MoveEventsHandler.customActions.put(action.getMoveName(), action);
    }

}
