package pokecube.adventures.client.render.entity;

import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;

public class RenderTrainer<T extends EntityLiving> extends RenderBiped<T>
{
    private static Map<TypeTrainer, ResourceLocation> males    = Maps.newHashMap();
    private static Map<TypeTrainer, ResourceLocation> females  = Maps.newHashMap();
    private static Map<String, ResourceLocation>      players  = Maps.newHashMap();
    private static Map<String, ResourceLocation>      urlSkins = Maps.newHashMap();

    private ModelBiped                                male;
    private ModelBiped                                female;
    private ModelBiped                                childMale;
    private ModelBiped                                childFemale;

    public RenderTrainer(RenderManager manager)
    {
        super(manager, new ModelBiped(0.0F), 0.5f);
        male = new ModelPlayer(0, false);
        female = new ModelPlayer(0, true);
        childMale = new ModelPlayer(0.5f, false);
        childFemale = new ModelPlayer(0.5f, true);
        this.addLayer(new BagRenderer(this));
        LayerRenderer<?> badHeadRenderer = null;
        for (Object o : layerRenderers)
        {
            if (o instanceof LayerCustomHead && !(o instanceof BetterCustomHeadLayer))
            {
                badHeadRenderer = (LayerRenderer<?>) o;
            }
        }
        layerRenderers.remove(badHeadRenderer);
        addLayer(new BetterCustomHeadLayer(male.bipedHead));
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks)
    {
        long time = entityIn.getEntityWorld().getTotalWorldTime();
        if (((EntityTrainer) entityIn).visibleTime > time) return;
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        long time = entity.getEntityWorld().getTotalWorldTime();
        if (((EntityTrainer) entity).visibleTime > time) return;
        if (((EntityTrainer) entity).pokemobsCap.getGender() == 1)
        {
            mainModel = entity.isChild() ? childMale : male;
        }
        else
        {
            mainModel = entity.isChild() ? childFemale : female;
        }
        GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
    }

    @Override
    /** Returns the location of an entity's texture. Doesn't seem to be called
     * unless you call Render.bindEntityTexture. */
    protected ResourceLocation getEntityTexture(T entity)
    {
        ResourceLocation texture = null;

        if (entity instanceof EntityTrainer)
        {
            EntityTrainer trainer = ((EntityTrainer) entity);
            if (!trainer.playerName.isEmpty())
            {
                if (players.containsKey(trainer.playerName)) return players.get(trainer.playerName);
                Minecraft minecraft = Minecraft.getMinecraft();
                GameProfile profile = new GameProfile((UUID) null, trainer.playerName);
                profile = TileEntitySkull.updateGameprofile(profile);
                Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);
                ResourceLocation resourcelocation;
                if (map.containsKey(Type.SKIN))
                {
                    resourcelocation = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
                }
                else
                {
                    UUID uuid = EntityPlayer.getUUID(profile);
                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                }
                players.put(trainer.playerName, resourcelocation);
                return resourcelocation;
            }
            if (!trainer.urlSkin.isEmpty())
            {
                if (urlSkins.containsKey(trainer.urlSkin)) return urlSkins.get(trainer.urlSkin);
                try
                {
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    byte[] hash = digest.digest(trainer.urlSkin.getBytes("UTF-8"));
                    StringBuilder sb = new StringBuilder(2 * hash.length);
                    for (byte b : hash)
                    {
                        sb.append(String.format("%02x", b & 0xff));
                    }
                    ResourceLocation resourcelocation = new ResourceLocation("skins/" + sb.toString());
                    TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
                    ITextureObject object = new URLSkinTexture(null, trainer.urlSkin,
                            DefaultPlayerSkin.getDefaultSkinLegacy(), new URLSkinImageBuffer());
                    texturemanager.loadTexture(resourcelocation, object);
                    urlSkins.put(trainer.urlSkin, resourcelocation);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return urlSkins.get(trainer.urlSkin);
            }
            TypeTrainer type = trainer.pokemobsCap.getType();
            boolean male;
            if (male = trainer.pokemobsCap.getGender() == 1)
            {
                texture = males.get(type);
            }
            else
            {
                texture = females.get(type);
            }
            if (texture == null)
            {
                texture = type == null ? super.getEntityTexture(entity) : type.getTexture(trainer);

                if (male)
                {
                    males.put(type, texture);
                }
                else
                {
                    females.put(type, texture);
                }
            }
            return texture;
        }
        texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
        return texture;
    }
}
