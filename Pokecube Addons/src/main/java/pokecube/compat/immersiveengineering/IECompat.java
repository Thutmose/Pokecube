package pokecube.compat.immersiveengineering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.DefaultPlantHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.recipes.IRecipeParser;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.items.berries.BerryManager;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.lib.CompatWrapper;

public class IECompat
{
    private static final QName ENERGY  = new QName("energy");
    private static final QName NUMBER  = new QName("n");
    private static final QName TIME    = new QName("time");
    private static final QName FLUID   = new QName("fluid");
    private static final QName VOLUME  = new QName("volume");
    private static final QName CHANCE  = new QName("chance");

    private static final QName OREDICT = new QName("oreDict");

    private static ItemStack parseItemStack(Drop input)
    {
        if (input.values.containsKey(OREDICT))
        {
            NonNullList<ItemStack> ores = OreDictionary.getOres(input.values.get(OREDICT), false);
            if (!ores.isEmpty())
            {
                ItemStack stack = ores.get(0).copy();
                Map<QName, String> values = input.values;
                if (input.tag != null)
                {
                    QName name = new QName("tag");
                    values.put(name, input.tag);
                }
                return updateStack(values, stack);
            }
        }
        return updateStack(input.values, XMLRecipeHandler.getStack(input));
    }

    private static ItemStack updateStack(Map<QName, String> values, ItemStack stack)
    {
        QName name = new QName("tag");
        String tag = values.containsKey(name) ? values.get(name) : "";
        if (values.containsKey(NUMBER)) CompatWrapper.setStackSize(stack, Integer.parseInt(values.get(NUMBER)));
        if (!tag.isEmpty())
        {
            try
            {
                stack.setTagCompound(JsonToNBT.getTagFromJson(tag));
            }
            catch (NBTException e)
            {
                e.printStackTrace();
            }
        }
        return stack;
    }

    private static ComparableItemStack getComparableStack(XMLRecipeInput input)
    {
        return new ComparableItemStack(parseItemStack(input), input.values.containsKey(OREDICT));
    }

    private static IngredientStack parseStack(Drop input)
    {
        if (input.values.containsKey(OREDICT))
        {
            int size = 1;
            if (input.values.containsKey(NUMBER))
            {
                size = Integer.parseInt(input.values.get(NUMBER));
            }
            return new IngredientStack(input.values.get(OREDICT), size);
        }
        else return new IngredientStack(updateStack(input.values, XMLRecipeHandler.getStack(input)));
    }

    private static FluidStack parseFluid(Drop fluid)
    {
        String fluidId = fluid.values.get(FLUID);
        if (fluidId == null || fluidId.isEmpty()) return null;
        Fluid theFluid = FluidRegistry.getFluid(fluidId);
        if (theFluid == null) return null;
        int num = 1000;
        if (fluid.values.containsKey(VOLUME)) num = Integer.parseInt(fluid.values.get(VOLUME));
        return new FluidStack(theFluid, num);
    }

    private static class CrusherParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            ItemStack output = parseItemStack(recipe.output);
            List<Object> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(parseStack(xml));
            }
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 1600;
            }
            CrusherRecipe theRecipe = CrusherRecipe.addRecipe(output, inputs.get(0), energy);
            for (int i = 1; i < inputs.size(); i++)
            {
                theRecipe.addToSecondaryOutput(inputs.get(i),
                        Float.parseFloat(recipe.inputs.get(i).values.get(CHANCE)));
            }
        }
    }

    private static class SqueezerParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            FluidStack outputFluid = parseFluid(recipe.output);
            // Put empty id if you want nullstack.
            ItemStack outputStack = outputFluid == null ? parseItemStack(recipe.output)
                    : parseItemStack(recipe.inputs.get(0));
            if (outputFluid == null && outputStack == null)
                throw new NullPointerException("No output Found for " + recipe.output);
            IngredientStack inputStack = parseStack(recipe.inputs.get(1));
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 6400;
            }
            SqueezerRecipe.addRecipe(outputFluid, outputStack, inputStack, energy);
        }
    }

    private static class AlloyParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            ItemStack output = parseItemStack(recipe.output);
            List<Object> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(parseStack(xml));
            }
            int time;
            if (recipe.output.values.containsKey(TIME))
            {
                time = Integer.parseInt(recipe.output.values.get(TIME));
            }
            else
            {
                time = 200;
            }
            AlloyRecipe.addRecipe(output, inputs.get(0), inputs.get(1), time);
        }
    }

    private static class ArcFurnaceParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            ItemStack output = parseItemStack(recipe.output);
            List<IngredientStack> inputs = Lists.newArrayList();
            IngredientStack input = parseStack(recipe.inputs.get(0));
            ItemStack slag = ItemStack.EMPTY;
            if (recipe.inputs.size() > 1)
            {
                slag = parseItemStack(recipe.inputs.get(1));
                for (int i = 2; i < recipe.inputs.size(); i++)
                {
                    XMLRecipeInput xml = recipe.inputs.get(i);
                    inputs.add(parseStack(xml));
                }
            }
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 512;
            }
            int time;
            if (recipe.output.values.containsKey(TIME))
            {
                time = Integer.parseInt(recipe.output.values.get(TIME));
            }
            else
            {
                time = 100;
            }
            ArcFurnaceRecipe.addRecipe(output, input, slag, time, energy, inputs.toArray());
        }
    }

    private static class MixerParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            FluidStack outputFluid = parseFluid(recipe.output);
            if (outputFluid == null) throw new NullPointerException("No Fluid Found for " + recipe.output);
            FluidStack inputFluid = parseFluid(recipe.inputs.get(0));
            List<Object> inputs = Lists.newArrayList();
            for (int i = 1; i < recipe.inputs.size(); i++)
            {
                XMLRecipeInput xml = recipe.inputs.get(i);
                inputs.add(parseStack(xml));
            }
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 200;
            }
            MixerRecipe.addRecipe(outputFluid, inputFluid, inputs.toArray(), energy);
        }
    }

    private static class FermenterParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            FluidStack outputFluid = parseFluid(recipe.output);
            if (outputFluid == null) throw new NullPointerException("No Fluid Found for " + recipe.output);
            // Put empty id if you want nullstack.
            ItemStack outputStack = parseItemStack(recipe.inputs.get(0));
            IngredientStack inputStack = parseStack(recipe.inputs.get(1));
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 6400;
            }
            FermenterRecipe.addRecipe(outputFluid, outputStack, inputStack, energy);
        }
    }

    private static class RefineryParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            FluidStack outputFluid = parseFluid(recipe.output);
            if (outputFluid == null) throw new NullPointerException("No Fluid Found for " + recipe.output);
            FluidStack inputFluid0 = parseFluid(recipe.inputs.get(0));
            FluidStack inputFluid1 = parseFluid(recipe.inputs.get(1));
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 80;
            }
            RefineryRecipe.addRecipe(outputFluid, inputFluid0, inputFluid1, energy);
        }
    }

    private static class PressParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            ItemStack output = parseItemStack(recipe.output);
            int energy;
            if (recipe.output.values.containsKey(ENERGY))
            {
                energy = Integer.parseInt(recipe.output.values.get(ENERGY));
            }
            else
            {
                energy = 80;
            }
            MetalPressRecipe.addRecipe(output, parseStack(recipe.inputs.get(0)),
                    getComparableStack(recipe.inputs.get(1)), energy);
        }
    }

    private static HashMap<ComparableItemStack, IBlockState>   seedOutputMap = new HashMap<>();
    private static HashMap<ComparableItemStack, IBlockState[]> seedRenderMap = new HashMap<>();

    private static class BerryClocheHandler extends DefaultPlantHandler
    {
        private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

        @Override
        protected HashSet<ComparableItemStack> getSeedSet()
        {
            return validSeeds;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
        {
            return new IBlockState[0];
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
        {
            return 1f;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile,
                BlockRendererDispatcher blockRenderer)
        {
            ComparableItemStack comp = new ComparableItemStack(seed, false);
            IBlockState[] renderStates = seedRenderMap.get(comp);
            if (renderStates.length > 0 && renderStates[0] != null && renderStates[0].getBlock() instanceof BlockStem)
            {
                GlStateManager.rotate(-90, 0, 1, 0);
                BlockStem stem = (BlockStem) renderStates[0].getBlock();
                IBlockState state = stem.getDefaultState().withProperty(BlockStem.AGE,
                        (int) (growth >= .5 ? 7 : 2 * growth * 7));
                if (growth >= .5) state = state.withProperty(BlockStem.FACING, EnumFacing.NORTH);
                IBakedModel model = blockRenderer.getModelForState(state);
                GlStateManager.translate(.25f, .0625f, 0);
                GlStateManager.pushMatrix();
                blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
                GlStateManager.popMatrix();
                if (growth >= .5)
                {
                    state = seedOutputMap.get(new ComparableItemStack(seed, false));
                    if (state != null)
                    {
                        model = blockRenderer.getModelForState(state);
                        GlStateManager.pushMatrix();
                        float scale = (growth - .5f) * .5f;
                        GlStateManager.translate(-scale / 2, .5 - scale, -.5 + scale / 2);
                        GlStateManager.scale(scale, scale, scale);
                        blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
                        GlStateManager.popMatrix();
                    }
                }
            }
            return true;
        }

        public void register(ItemStack seed, ItemStack[] output, IBlockState seedRender, Object soil,
                IBlockState... cropRender)
        {
            // Call super to register the soil.
            super.register(seed, output, soil, cropRender);
            ComparableItemStack comp = new ComparableItemStack(seed, false);
            seedOutputMap.put(comp, seedRender);
            seedRenderMap.put(comp, cropRender);
        }
    }

    @Method(modid = "immersiveengineering")
    @CompatClass(phase = Phase.CONSTRUCT)
    public static void ConstructIE()
    {
        XMLRecipeHandler.recipeParsers.put("ie_crusher", new CrusherParser());
        XMLRecipeHandler.recipeParsers.put("ie_squeezer", new SqueezerParser());
        XMLRecipeHandler.recipeParsers.put("ie_alloy", new AlloyParser());
        XMLRecipeHandler.recipeParsers.put("ie_arcfurnace", new ArcFurnaceParser());
        XMLRecipeHandler.recipeParsers.put("ie_mixer", new MixerParser());
        XMLRecipeHandler.recipeParsers.put("ie_fermenter", new FermenterParser());
        XMLRecipeHandler.recipeParsers.put("ie_refinery", new RefineryParser());
        XMLRecipeHandler.recipeParsers.put("ie_press", new PressParser());
    }

    @Method(modid = "immersiveengineering")
    @CompatClass(phase = Phase.POST)
    public static void PostInitIE()
    {
        BerryClocheHandler clocheHandler = new BerryClocheHandler();
        BelljarHandler.registerHandler(clocheHandler);
        for (String name : BerryManager.berryNames.values())
        {
            ItemStack berry = BerryManager.getBerryItem(name);
            Block berryCrop = BerryManager.berryCrop;
            Block berryFruit = BerryManager.berryFruit;
            clocheHandler.register(berry.copy(), new ItemStack[] { berry.copy() },
                    berryFruit.getDefaultState().withProperty(BerryManager.type, name), new ItemStack(Blocks.DIRT),
                    berryCrop.getDefaultState().withProperty(BerryManager.type, name));
        }
    }

}
