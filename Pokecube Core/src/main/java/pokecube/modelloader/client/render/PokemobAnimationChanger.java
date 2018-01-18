package pokecube.modelloader.client.render;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IShearable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.client.render.animation.AnimationRandomizer;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.tabula.components.Animation;

public class PokemobAnimationChanger implements IAnimationChanger
{
    final IAnimationChanger                              parent;
    /** These parts can be sheared off. */
    public final Set<String>                             shearables    = Sets.newHashSet();
    /** These parts are dyed based on the specialInfo of the pokemob; */
    public final Set<String>                             dyeables      = Sets.newHashSet();
    /** These parts get a specific colour offset from the default colour of the
     * specialInfo. */
    public final Map<String, Function<Integer, Integer>> colourOffsets = Maps.newHashMap();
    /** This is a cache of which parts have been checked for being a
     * wildcard. */
    private final Set<String>                            checkWildCard = Sets.newHashSet();

    public PokemobAnimationChanger()
    {
        parent = null;
    }

    public PokemobAnimationChanger(IAnimationChanger parent)
    {
        this.parent = null;
    }

    private void checkWildCard(String partIdentifier)
    {
        if (!checkWildCard.contains(partIdentifier))
        {
            checkWildCard.add(partIdentifier);
            for (String s : dyeables)
            {
                if (s.startsWith("*") && partIdentifier.matches(s.substring(1)))
                {
                    dyeables.add(partIdentifier);
                    if (colourOffsets.containsKey(s))
                    {
                        colourOffsets.put(partIdentifier, colourOffsets.get(s));
                    }
                    break;
                }
            }
            for (String s : shearables)
            {
                if (s.startsWith("*") && partIdentifier.matches(s.substring(1)))
                {
                    dyeables.add(partIdentifier);
                    break;
                }
            }
        }
    }

    @Override
    public int getColourForPart(String partIdentifier, Entity entity, int default_)
    {
        checkWildCard(partIdentifier);
        if (dyeables.contains(partIdentifier))
        {
            int rgba = 0xFF000000;
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            Function<Integer, Integer> offset = colourOffsets.get(partIdentifier);
            int colour = pokemob.getSpecialInfo() & 15;
            if (offset != null) colour = offset.apply(colour);
            rgba += EnumDyeColor.byDyeDamage(colour).getColorValue();
            return rgba;
        }
        if (parent != null) return parent.getColourForPart(partIdentifier, entity, default_);
        return default_;
    }

    @Override
    public boolean isPartHidden(String part, Entity entity, boolean default_)
    {
        boolean mask = false;
        if (parent != null)
        {
            mask = parent.isPartHidden(part, entity, default_);
        }
        checkWildCard(part);
        if (shearables.contains(part)) { return !((IShearable) entity).isShearable(new ItemStack(Items.SHEARS),
                entity.getEntityWorld(), entity.getPosition()); }
        return default_ || mask;
    }

    @Override
    public String modifyAnimation(EntityLiving entity, float partialTicks, String phase)
    {
        if (parent != null) return parent.modifyAnimation(entity, partialTicks, phase);
        return phase;
    }

    public void init(Set<Animation> existingAnimations)
    {
        if (parent instanceof AnimationRandomizer)
        {
            ((AnimationRandomizer) parent).init(existingAnimations);
        }
    }

}
