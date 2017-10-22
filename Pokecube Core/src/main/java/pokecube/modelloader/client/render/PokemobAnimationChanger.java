package pokecube.modelloader.client.render;

import java.util.Set;

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
    final IAnimationChanger  parent;
    public final Set<String> shearables = Sets.newHashSet();
    public final Set<String> dyeables   = Sets.newHashSet();

    public PokemobAnimationChanger()
    {
        parent = null;
    }

    public PokemobAnimationChanger(IAnimationChanger parent)
    {
        this.parent = null;
    }

    @Override
    public int getColourForPart(String partIdentifier, Entity entity, int default_)
    {
        if (dyeables.contains(partIdentifier))
        {
            int rgba = 0xFF000000;
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            rgba += EnumDyeColor.byDyeDamage(pokemob.getSpecialInfo()).getColorValue();
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
