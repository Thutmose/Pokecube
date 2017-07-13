package pokecube.core.client.render.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerEntityOnShoulder;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.ClientProxy;
import pokecube.modelloader.client.render.AnimationLoader;
import thut.core.client.render.model.IModelRenderer;

@SideOnly(Side.CLIENT)
public class RenderPokemobOnShoulder implements LayerRenderer<EntityPlayer>
{
    private final RenderManager                            renderManager;
    private final LayerEntityOnShoulder                    parent;
    protected RenderLivingBase<? extends EntityLivingBase> leftRenderer;
    private ModelBase                                      leftModel;
    private ResourceLocation                               leftResource;
    private UUID                                           leftUniqueId;
    private Class<?>                                       leftEntityClass;
    protected RenderLivingBase<? extends EntityLivingBase> rightRenderer;
    private ModelBase                                      rightModel;
    private ResourceLocation                               rightResource;
    private UUID                                           rightUniqueId;
    private Class<?>                                       rightEntityClass;

    public RenderPokemobOnShoulder(RenderManager renderManager, LayerEntityOnShoulder parent)
    {
        this.renderManager = renderManager;
        this.parent = parent;
    }

    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (player.getLeftShoulderEntity() != null || player.getRightShoulderEntity() != null)
        {
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            NBTTagCompound nbttagcompound = player.getLeftShoulderEntity();

            boolean left = true;
            boolean right = true;

            if (!nbttagcompound.hasNoTags())
            {
                RenderPokemobOnShoulder.DataHolder holder = this.renderEntityOnShoulder(player, this.leftUniqueId,
                        nbttagcompound, this.leftRenderer, this.leftModel, this.leftResource, this.leftEntityClass,
                        limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, true);
                if (holder == null)
                {
                    left = false;
                }
                else
                {
                    this.leftUniqueId = holder.entityId;
                    this.leftRenderer = holder.renderer;
                    this.leftResource = holder.textureLocation;
                    this.leftModel = holder.model;
                    this.leftEntityClass = holder.clazz;
                }
            }

            NBTTagCompound nbttagcompound1 = player.getRightShoulderEntity();

            if (!nbttagcompound1.hasNoTags())
            {
                RenderPokemobOnShoulder.DataHolder holder = this.renderEntityOnShoulder(player, this.rightUniqueId,
                        nbttagcompound1, this.rightRenderer, this.rightModel, this.rightResource, this.rightEntityClass,
                        limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, false);
                if (holder == null)
                {
                    right = false;
                }
                else
                {
                    this.rightUniqueId = holder.entityId;
                    this.rightRenderer = holder.renderer;
                    this.rightResource = holder.textureLocation;
                    this.rightModel = holder.model;
                    this.rightEntityClass = holder.clazz;
                }
            }

            GlStateManager.disableRescaleNormal();
            // System.out.println(nbttagcompound);
            // System.out.println(nbttagcompound1);
            if (!left || !right)
            {
                NBTTagCompound bakLeft = nbttagcompound.copy();
                NBTTagCompound bakRight = nbttagcompound1.copy();
                if (left)
                {
                    for (String s : bakLeft.getKeySet())
                        nbttagcompound.removeTag(s);
                }

                if (right)
                {
                    for (String s : bakRight.getKeySet())
                        nbttagcompound1.removeTag(s);
                }

                parent.doRenderLayer(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
                        headPitch, scale);

                if (left)
                {
                    for (String s : bakLeft.getKeySet())
                        nbttagcompound.setTag(s, bakLeft.getTag(s));
                }

                if (right)
                {
                    for (String s : bakRight.getKeySet())
                        nbttagcompound1.setTag(s, bakRight.getTag(s));
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    private RenderPokemobOnShoulder.DataHolder renderEntityOnShoulder(EntityPlayer player, @Nullable UUID mobUUID,
            NBTTagCompound mobNBTTag, RenderLivingBase<? extends EntityLivingBase> mobRenderer, ModelBase mobModelBase,
            ResourceLocation texture, Class<?> mobClass, float limbSwing, float limbSwingAmount, float partialTick,
            float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, boolean left)
    {

        if (mobUUID == null || !mobUUID.equals(mobNBTTag.getUniqueId("UUID")))
        {
            mobUUID = mobNBTTag.getUniqueId("UUID");
            mobClass = EntityList.getClassFromName(mobNBTTag.getString("id"));
            if (!IPokemob.class.isAssignableFrom(mobClass)) return null;
            mobRenderer = (RenderLivingBase<?>) this.renderManager
                    .getEntityClassRenderObject((Class<? extends Entity>) mobClass);
            mobModelBase = ((RenderLivingBase<?>) mobRenderer).getMainModel();
            Entity entity = EntityList.newEntity((Class<? extends Entity>) mobClass, player.world);
            entity.readFromNBT(mobNBTTag);
            texture = RenderPokemobs.getInstance().getEntityTexturePublic(entity);
            if (mobModelBase == null)
            {
                IPokemob mob = (IPokemob) entity;
                IModelRenderer<?> model = (IModelRenderer<?>) RenderAdvancedPokemobModel
                        .getRenderer(mob.getPokedexEntry().getName(), (EntityLiving) entity);
                if (model == null && mob.getPokedexEntry().getBaseForme() != null)
                {
                    model = (IModelRenderer<?>) RenderAdvancedPokemobModel
                            .getRenderer(mob.getPokedexEntry().getBaseForme().getName(), (EntityLiving) entity);
                    AnimationLoader.modelMaps.put(mob.getPokedexEntry().getName(), model);
                }
                if (model != null && model instanceof RenderLivingBase)
                {
                    mobModelBase = ((RenderLivingBase<?>) model).getMainModel();
                }
                if (mobModelBase == null)
                {
                    PokedexEntry entry = mob.getPokedexEntry();
                    if (entry.getBaseForme() != null)
                    {
                        entry = entry.getBaseForme();
                    }
                    ((ClientProxy) ModPokecubeML.proxy).reloadModel(entry);
                    for (PokedexEntry e : entry.forms.values())
                    {
                        ((ClientProxy) ModPokecubeML.proxy).reloadModel(e);
                    }
                }
            }

        }
        if (mobModelBase == null) return null;
        if (texture != null) mobRenderer.bindTexture(texture);
        GlStateManager.pushMatrix();
        float f = player.isSneaking() ? -1.3F : -1.5F;
        float f1 = left ? 0.4F : -0.4F;
        GlStateManager.translate(f1, f, 0.0F);

        if (mobClass == EntityParrot.class)
        {
            ageInTicks = 0.0F;
        }

        mobModelBase.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTick);
        mobModelBase.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
                player);
        mobModelBase.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        GlStateManager.popMatrix();
        return new RenderPokemobOnShoulder.DataHolder(mobUUID, mobRenderer, mobModelBase, texture, mobClass);
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    class DataHolder
    {
        public UUID                                         entityId;
        public RenderLivingBase<? extends EntityLivingBase> renderer;
        public ModelBase                                    model;
        public ResourceLocation                             textureLocation;
        public Class<?>                                     clazz;

        public DataHolder(UUID p_i47463_2_, RenderLivingBase<? extends EntityLivingBase> p_i47463_3_,
                ModelBase p_i47463_4_, ResourceLocation p_i47463_5_, Class<?> p_i47463_6_)
        {
            this.entityId = p_i47463_2_;
            this.renderer = p_i47463_3_;
            this.model = p_i47463_4_;
            this.textureLocation = p_i47463_5_;
            this.clazz = p_i47463_6_;
        }
    }
}