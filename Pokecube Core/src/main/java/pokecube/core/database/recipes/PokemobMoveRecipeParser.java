package pokecube.core.database.recipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class PokemobMoveRecipeParser implements IRecipeParser
{
    private static final QName MOVENAME   = new QName("move");
    private static final QName HUNGERCOST = new QName("cost");
    private static final QName OREDICT    = new QName("oreDict");
    private static final QName SIZE       = new QName("n");

    private static class CustomShapessOreRecipe extends ShapelessOreRecipe
    {
        public CustomShapessOreRecipe(ResourceLocation name, ItemStack result, Object... recipe)
        {
            super(name, result, new Object[0]);
            for (Object in : recipe)
            {
                Ingredient ing = CraftingHelper.getIngredient(in);

                if (in instanceof List)
                {
                    List<?> list = (List<?>) in;
                    ItemStack[] stacks = new ItemStack[list.size()];
                    for (int i = 0; i < stacks.length; i++)
                    {
                        stacks[i] = (ItemStack) list.get(i);
                    }
                    input.add(Ingredient.fromStacks(stacks));
                }

                if (ing != null)
                {
                    input.add(ing);
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
        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(InventoryCrafting var1, World world)
        {
            ArrayList<Object> required = new ArrayList<Object>(input);

            for (int x = 0; x < var1.getSizeInventory(); x++)
            {
                ItemStack slot = var1.getStackInSlot(x);

                if (CompatWrapper.isValid(slot))
                {
                    boolean inRecipe = false;
                    Iterator<Object> req = required.iterator();

                    while (req.hasNext())
                    {
                        boolean match = false;

                        Object next = req.next();

                        if (next instanceof ItemStack)
                        {
                            match = OreDictionary.itemMatches((ItemStack) next, slot, false)
                                    && CompatWrapper.getStackSize(slot) == CompatWrapper.getStackSize((ItemStack) next);
                        }
                        else if (next instanceof List)
                        {
                            Iterator<ItemStack> itr = ((List<ItemStack>) next).iterator();
                            while (itr.hasNext() && !match)
                            {
                                ItemStack test = itr.next();
                                match = OreDictionary.itemMatches(test, slot, false)
                                        && CompatWrapper.getStackSize(slot) == CompatWrapper.getStackSize(test);
                            }
                        }

                        if (match)
                        {
                            inRecipe = true;
                            required.remove(next);
                            break;
                        }
                    }

                    if (!inRecipe) { return false; }
                }
            }

            return required.isEmpty();
        }

    }

    public static class WrappedRecipeMove implements IMoveAction
    {
        final IMoveAction parent;
        final IMoveAction other;

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

    }

    public static class RecipeMove implements IMoveAction
    {
        final String            name;
        final IRecipe           recipe;
        final InventoryCrafting inventory;
        final Container         handler;
        final int               hungerCost;

        public RecipeMove(XMLRecipe recipe)
        {
            this.name = recipe.values.get(MOVENAME);
            this.hungerCost = Integer.parseInt(recipe.values.get(HUNGERCOST));
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
                    for (ItemStack stack : items)
                    {
                        int size = 1;
                        if (xml.values.containsKey(SIZE))
                        {
                            size = Integer.parseInt(xml.values.get(SIZE));
                        }
                        if (CompatWrapper.isValid(stack)) CompatWrapper.setStackSize(stack, size);
                    }
                }
                else inputs.add(XMLRecipeHandler.getStack(xml));
            }
            boolean failed = !CompatWrapper.isValid(output);
            for (Object o : inputs)
                failed = failed || o == null;
            if (failed) { throw new NullPointerException("output: " + output + " inputs: " + inputs); }
            this.recipe = new CustomShapessOreRecipe(new ResourceLocation(PokecubeMod.ID, "loaded"), output,
                    inputs.toArray());
        }

        @Override
        public boolean applyEffect(IPokemob user, Vector3 location)
        {
            return attemptCraft(user, location);
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
                    inventory.setInventorySlotContents(i, CompatWrapper.nullStack);
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
