package pokecube.core.client.render.entity;

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
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.entity.professor.EntityProfessor.ProfessorType;
import pokecube.core.interfaces.PokecubeMod;

public class RenderProfessor<T extends EntityLiving> extends RenderBiped<T>
{
    private static Map<String, ResourceLocation> players   = Maps.newHashMap();
    private static final ResourceLocation        PROFESSOR = new ResourceLocation(
            PokecubeMod.ID + ":textures/professor.png");
    private static final ResourceLocation        NURSE     = new ResourceLocation(
            PokecubeMod.ID + ":textures/nurse.png");
    private ModelBiped                           male;
    private ModelBiped                           female;

    public RenderProfessor(RenderManager manager)
    {
        super(manager, new ModelBiped(0.0F), 0.5f);
        male = new ModelPlayer(0, false);
        female = new ModelPlayer(0, true);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (((EntityProfessor) entity).male)
        {
            mainModel = male;
        }
        else
        {
            mainModel = female;
        }
        GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
    }

    /** Returns the location of an entity's texture. Doesn't seem to be called
     * unless you call Render.bindEntityTexture. */
    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        if (!((EntityProfessor) entity).playerName.isEmpty())
        {
            if (players.containsKey(((EntityProfessor) entity).playerName))
                return players.get(((EntityProfessor) entity).playerName);
            Minecraft minecraft = Minecraft.getMinecraft();
            GameProfile profile = new GameProfile((UUID) null, ((EntityProfessor) entity).playerName);
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
            players.put(((EntityProfessor) entity).playerName, resourcelocation);
            return resourcelocation;
        }

        if (((EntityProfessor) entity).type == ProfessorType.HEALER)
        {
            return NURSE;
        }
        return PROFESSOR;
    }
}
