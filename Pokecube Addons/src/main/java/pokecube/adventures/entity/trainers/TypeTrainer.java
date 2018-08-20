package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.EntityHasTrades;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.events.handlers.SpawnHandler.Variance;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class TypeTrainer
{
    private static final int CAREERFIELDINDEX = 13;

    public static class TrainerTrades
    {
        public List<TrainerTrade> tradesList = Lists.newArrayList();

        public void addTrades(List<MerchantRecipe> ret, EntityHasTrades trader)
        {
            for (TrainerTrade trade : tradesList)
            {
                if (Math.random() < trade.chance)
                {
                    MerchantRecipe toAdd = trade.getRecipe();
                    if (toAdd != null) ret.add(toAdd);
                }
            }
        }
    }

    public static class TrainerTrade extends MerchantRecipe
    {
        public int   min    = -1;
        public int   max    = -1;
        public float chance = 1;

        public TrainerTrade(ItemStack buy1, ItemStack buy2, ItemStack sell)
        {
            super(buy1, buy2, sell);
        }

        public MerchantRecipe getRecipe()
        {
            ItemStack buy1 = this.getItemToBuy();
            ItemStack buy2 = this.getSecondItemToBuy();
            if (!buy1.isEmpty())
            {
                buy1 = buy1.copy();
            }
            if (!buy2.isEmpty())
            {
                buy2 = buy2.copy();
            }
            ItemStack sell = this.getItemToSell();
            if (!sell.isEmpty())
            {
                sell = sell.copy();
            }
            else
            {
                return null;
            }
            if (min != -1 && max != -1)
            {
                if (max < min) max = min;
                sell.setCount(min + new Random().nextInt(1 + max - min));
            }
            // TODO Find out where the mess with client side so the 65 isn't
            // needed.
            MerchantRecipe ret = new MerchantRecipe(buy1, buy2, sell, 0, 65);
            return ret;
        }
    }

    public static interface ITypeMapper
    {
        /** Mapping of EntityLivingBase to a TypeTrainer. EntityTrainers set
         * this on spawn, so it isn't needed for them. <br>
         * <br>
         * if forSpawn, it means this is being initialized, otherwise it is
         * during the check for whether this mob should have trainers. */
        default TypeTrainer getType(EntityLivingBase mob, boolean forSpawn)
        {
            if (!SpawnHandler.canSpawnInWorld(mob.getEntityWorld())) return null;
            if (!forSpawn)
            {
                if (mob instanceof EntityTrainer) return merchant;
                if (Config.instance.npcsAreTrainers && mob instanceof INpc) return merchant;
                for (Class<? extends Entity> clazz : Config.instance.customTrainers)
                {
                    if (clazz.isInstance(mob)) return merchant;
                }
                return null;
            }

            if (mob instanceof EntityVillager && Config.instance.npcsAreTrainers)
            {
                EntityVillager villager = (EntityVillager) mob;
                VillagerProfession profession = villager.getProfessionForge();
                int career = ReflectionHelper.getPrivateValue(EntityVillager.class, villager, CAREERFIELDINDEX);
                String type = profession.getCareer(career).getName();
                return getTrainer(type);
            }
            return null;
        }

        /** Should the IHasPokemobs for this mob sync the values to client? if
         * not, it will use a server-side list of mobs instead of datamanager
         * values. */
        default boolean shouldSync(EntityLivingBase mob)
        {
            return mob instanceof EntityTrainer;
        }
    }

    public static ITypeMapper                    mobTypeMapper = new ITypeMapper()
                                                               {
                                                               };

    public static HashMap<String, TrainerTrades> tradesMap     = Maps.newHashMap();
    public static HashMap<String, TypeTrainer>   typeMap       = new HashMap<String, TypeTrainer>();
    public static ArrayList<String>              maleNames     = new ArrayList<String>();
    public static ArrayList<String>              femaleNames   = new ArrayList<String>();

    public static TypeTrainer                    merchant      = new TypeTrainer("Merchant");
    static
    {
        merchant.tradeTemplate = "merchant";
    }

    public static void addTrainer(String name, TypeTrainer type)
    {
        typeMap.put(name, type);
    }

    public static void getRandomTeam(IHasPokemobs trainer, EntityLivingBase owner, int level, World world)
    {
        TypeTrainer type = trainer.getType();

        for (int i = 0; i < 6; i++)
            trainer.setPokemob(i, ItemStack.EMPTY);

        if (level == 0) level = 5;
        Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        int number = 1 + new Random().nextInt(6);
        number = Math.min(number, trainer.getMaxPokemobCount());

        List<PokedexEntry> values = Lists.newArrayList();
        if (type.pokemon != null) values.addAll(type.pokemon);
        else PokecubeMod.log(Level.WARNING, "No mobs for " + type);

        for (int i = 0; i < number; i++)
        {
            Collections.shuffle(values);
            ItemStack item = ItemStack.EMPTY;
            for (PokedexEntry s : values)
            {
                if (s != null)
                {
                    System.out.println(s + " " + level + " " + variance.apply(level));
                    item = makeStack(s, owner, world, variance.apply(level));
                }
                if (CompatWrapper.isValid(item)) break;
            }
            trainer.setPokemob(i, item);
        }
    }

    public static TypeTrainer getTrainer(String name)
    {
        TypeTrainer ret = typeMap.get(name);
        if (ret == null)
        {
            for (TypeTrainer t : typeMap.values())
            {
                if (t != null && t.name.equalsIgnoreCase(name)) return t;
            }
            for (TypeTrainer t : typeMap.values())
            {
                if (t != null) return t;
            }
        }
        return ret;
    }

    public static ItemStack makeStack(PokedexEntry entry, EntityLivingBase trainer, World world, int level)
    {
        int num = entry.getPokedexNb();
        if (Pokedex.getInstance().getEntry(num) == null) return ItemStack.EMPTY;

        IPokemob pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, world));
        if (pokemob != null)
        {
            for (int i = 1; i < level; i++)
            {
                if (pokemob.getPokedexEntry().canEvolve(i))
                {
                    for (EvolutionData d : pokemob.getPokedexEntry().getEvolutions())
                    {
                        if (d.shouldEvolve(pokemob))
                        {
                            IPokemob temp = CapabilityPokemob.getPokemobFor(d.getEvolution(world));
                            if (temp != null)
                            {
                                pokemob = temp;
                                break;
                            }
                        }
                    }
                }
            }
            pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
            pokemob = pokemob.setPokedexEntry(entry);
            pokemob.setPokemonOwner(trainer);
            pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob = pokemob.setForSpawn(exp);
            ItemStack item = PokecubeManager.pokemobToItem(pokemob);
            pokemob.getEntity().isDead = true;
            return item;
        }

        return ItemStack.EMPTY;
    }

    public static void initSpawns()
    {
        for (TypeTrainer type : typeMap.values())
        {
            for (SpawnBiomeMatcher matcher : type.matchers.keySet())
            {
                matcher.reset();
                matcher.parse();
            }
        }
    }

    public static void postInitTrainers()
    {
        List<TypeTrainer> toRemove = new ArrayList<TypeTrainer>();
        for (TypeTrainer t : typeMap.values())
        {
            if (t.pokemon.size() == 0 && t != merchant)
            {
                toRemove.add(t);
            }
        }
        if (PokecubeMod.debug) if (!toRemove.isEmpty()) PokecubeMod.log("Removing Trainer Types: " + toRemove);
        for (TypeTrainer t : toRemove)
        {
            typeMap.remove(t.name);
        }
    }

    public final String                  name;
    /** 1 = male, 2 = female, 3 = both */
    public byte                          genders       = 1;

    public Map<SpawnBiomeMatcher, Float> matchers      = Maps.newHashMap();
    public boolean                       hasBag        = false;
    public ItemStack                     bag           = ItemStack.EMPTY;
    public boolean                       hasBelt       = false;
    private ResourceLocation             texture;

    private ResourceLocation             femaleTexture;

    public String                        tradeTemplate = "default";
    public List<PokedexEntry>            pokemon       = Lists.newArrayList();
    public TrainerTrades                 trades;

    private ItemStack[]                  loot          = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY)
            .toArray(new ItemStack[4]);

    public String                        drops         = "";
    public ItemStack                     held          = ItemStack.EMPTY;

    public TypeTrainer(String name)
    {
        this.name = name;
        addTrainer(name, this);
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getTexture(EntityTrainer trainer)
    {
        if (texture == null && (genders == 1 || genders == 2))
        {
            texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + name.toLowerCase(Locale.US) + ".png");
            if (!texExists(texture)) texture = null;
            if (genders == 2 && texture == null)
            {
                texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "female.png");
            }
            if (genders == 1 && texture == null)
            {
                texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
            }
        }
        else if (genders == 3)
        {

            if (femaleTexture == null)
            {
                femaleTexture = new ResourceLocation(
                        PokecubeAdv.TRAINERTEXTUREPATH + name.toLowerCase(Locale.US) + "female.png");
                if (!texExists(femaleTexture)) femaleTexture = null;
            }
            if (texture == null)
            {
                texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + name.toLowerCase(Locale.US) + ".png");
                if (!texExists(texture)) texture = null;
            }
            if (femaleTexture == null)
            {
                femaleTexture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "female.png");
            }
            if (texture == null)
            {
                texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
            }

            return trainer.pokemobsCap.getGender() == 1 ? texture : femaleTexture;
        }
        return texture;
    }

    private void initLoot()
    {
        if (CompatWrapper.isValid(loot[0])) return;

        if (!drops.equals(""))
        {
            String[] args = drops.split(":");
            int num = 0;
            for (String s : args)
            {
                if (s == null) continue;
                String[] stackinfo = s.split("`");
                ItemStack stack = PokecubeItems.getStack(stackinfo[0]);
                if (stackinfo.length > 1)
                {
                    try
                    {
                        int count = Integer.parseInt(stackinfo[1]);
                        stack.setCount(count);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
                if (stackinfo.length > 2)
                {
                    try
                    {
                        int count = Integer.parseInt(stackinfo[2]);
                        stack.setItemDamage(count);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
                loot[num] = stack;
                num++;
            }
        }
        if (!CompatWrapper.isValid(loot[0])) loot[0] = new ItemStack(Items.EMERALD);
    }

    public void initTrainerItems(EntityTrainer trainer)
    {
        initLoot();
        for (int i = 1; i < 5; i++)
        {
            EntityEquipmentSlot slotIn = EntityEquipmentSlot.values()[i];
            trainer.setItemStackToSlot(slotIn, loot[i - 1]);
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean texExists(ResourceLocation texture)
    {
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(texture);
            res.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "" + name;
    }

    public Collection<MerchantRecipe> getRecipes(EntityHasTrades trader)
    {
        if (trades == null && tradeTemplate != null)
        {
            trades = tradesMap.get(tradeTemplate);
            if (trades == null) tradeTemplate = null;
        }
        List<MerchantRecipe> ret = Lists.newArrayList();
        if (trades != null)
        {
            trades.addTrades(ret, trader);
        }
        return ret;
    }
}
