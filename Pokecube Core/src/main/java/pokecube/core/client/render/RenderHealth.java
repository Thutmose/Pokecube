package pokecube.core.client.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

/** This health renderer is directly based on Neat vy Vaziki, which can be found
 * here: https://github.com/Vazkii/Neat This version has been modified to only
 * apply to pokemobs, as well as to show level, gender and exp. I have also
 * modified the nametags to indicate ownership */
public class RenderHealth
{

    List<EntityLivingBase> renderedEntities = new ArrayList<>();

    boolean                blend;
    boolean                normalize;
    boolean                lighting;
    int                    src;
    int                    dst;

    protected void postRender()
    {
        // Reset to original state. This fixes changes to guis when rendered in
        // them.
        if (!normalize) GL11.glDisable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        if (lighting) GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GL11.glBlendFunc(src, dst);
    }

    protected void preRender()
    {
        blend = GL11.glGetBoolean(GL11.GL_BLEND);
        normalize = GL11.glGetBoolean(GL11.GL_NORMALIZE);
        src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        if (lighting) GlStateManager.disableLighting();
        if (!normalize) GL11.glEnable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (!PokecubeMod.core.getConfig().doHealthBars
                || (!PokecubeMod.core.getConfig().renderInF1 && !Minecraft.isGuiEnabled()))
            return;

        Entity cameraEntity = mc.getRenderViewEntity();
        BlockPos renderingVector = cameraEntity.getPosition();
        Frustum frustum = new Frustum();

        float partialTicks = event.getPartialTicks();
        double viewX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * partialTicks;
        double viewY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * partialTicks;
        double viewZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * partialTicks;
        frustum.setPosition(viewX, viewY, viewZ);

        if (PokecubeMod.core.getConfig().showOnlyFocused)
        {
            Entity focused = getEntityLookedAt(mc.player);
            if (focused != null && focused instanceof EntityLivingBase && focused.isEntityAlive()) try
            {
                renderHealthBar((EntityLivingBase) focused, partialTicks, cameraEntity);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            WorldClient client = mc.world;
            List<Entity> entities = client.loadedEntityList;
            for (Entity entity : entities)
            {
                if (entity != null && entity instanceof EntityLivingBase && entity != mc.player
                        && entity.isInRangeToRender3d(renderingVector.getX(), renderingVector.getY(),
                                renderingVector.getZ())
                        && (entity.ignoreFrustumCheck || frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox()))
                        && entity.isEntityAlive() && entity.getRecursivePassengers().isEmpty())
                {
                    try
                    {
                        renderHealthBar((EntityLivingBase) entity, partialTicks, cameraEntity);
                    }
                    catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void renderHealthBar(EntityLivingBase passedEntity, float partialTicks, Entity viewPoint)
    {
        Stack<EntityLivingBase> ridingStack = new Stack<>();

        EntityLivingBase entity = passedEntity;

        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return;
        Config config = PokecubeMod.core.getConfig();
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        if (renderManager == null || renderManager.renderViewEntity == null) return;
        UUID viewerID = renderManager.renderViewEntity.getUniqueID();

        ridingStack.push(entity);

        while (entity.getRidingEntity() != null && entity.getRidingEntity() instanceof EntityLivingBase)
        {
            entity = (EntityLivingBase) entity.getRidingEntity();
            ridingStack.push(entity);
        }

        float pastTranslate = 0F;
        while (!ridingStack.isEmpty())
        {
            entity = ridingStack.pop();
            processing:
            {
                float distance = passedEntity.getDistance(viewPoint);
                if (distance > config.maxDistance || !passedEntity.canEntityBeSeen(viewPoint) || entity.isInvisible())
                    break processing;

                double x = passedEntity.lastTickPosX + (passedEntity.posX - passedEntity.lastTickPosX) * partialTicks;
                double y = passedEntity.lastTickPosY + (passedEntity.posY - passedEntity.lastTickPosY) * partialTicks;
                double z = passedEntity.lastTickPosZ + (passedEntity.posZ - passedEntity.lastTickPosZ) * partialTicks;

                float scale = 0.026666672F;
                float maxHealth = entity.getMaxHealth();
                float health = Math.min(maxHealth, entity.getHealth());

                if (maxHealth <= 0) break processing;

                GlStateManager.pushMatrix();

                preRender();

                GlStateManager.translate((float) (x - renderManager.viewerPosX),
                        (float) (y - renderManager.viewerPosY + passedEntity.height + config.heightAbove),
                        (float) (z - renderManager.viewerPosZ));
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-scale, -scale, scale);
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                float padding = config.backgroundPadding;
                int bgHeight = config.backgroundHeight;
                int barHeight1 = config.barHeight;
                float size = config.plateSize;

                int r = 0;
                int g = 255;
                int b = 0;
                ItemStack stack = ItemStack.EMPTY;
                if (pokemob.getPokemonOwner() == renderManager.renderViewEntity)
                {
                    stack = entity.getHeldItemMainhand();
                }
                int armor = entity.getTotalArmorValue();
                float hue = Math.max(0F, (health / maxHealth) / 3F - 0.07F);
                Color color = Color.getHSBColor(hue, 1F, 1F);
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                GlStateManager.translate(0F, pastTranslate, 0F);
                ITextComponent nameComp = pokemob.getPokemonDisplayName();
                boolean nametag = pokemob.getGeneralState(GeneralStates.TAMED);
                PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance()
                        .getPlayerData(Minecraft.getMinecraft().player).getData(PokecubePlayerStats.class);
                boolean captureOrHatch = StatsCollector.getCaptured(pokemob.getPokedexEntry(),
                        Minecraft.getMinecraft().player) > 0
                        || StatsCollector.getHatched(pokemob.getPokedexEntry(), Minecraft.getMinecraft().player) > 0;
                boolean scanned = false;
                nametag = nametag || captureOrHatch || (scanned = stats.hasInspected(pokemob.getPokedexEntry()));
                if (!nametag)
                {
                    nameComp.getStyle().setObfuscated(true);
                }
                if (entity instanceof EntityLiving && ((EntityLiving) entity).hasCustomName())
                    nameComp = new TextComponentString(
                            TextFormatting.ITALIC + ((EntityLiving) entity).getCustomNameTag());
                float s = 0.5F;
                String name = I18n.format(nameComp.getFormattedText());
                float namel = mc.fontRenderer.getStringWidth(name) * s;
                if (namel + 20 > size * 2) size = namel / 2F + 10F;
                float healthSize = size * (health / maxHealth);

                // Background
                if (config.drawBackground)
                {
                    buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    buffer.pos(-size - padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
                    buffer.pos(-size - padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
                    buffer.pos(size + padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
                    buffer.pos(size + padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
                    tessellator.draw();
                }

                // Health bar
                // Gray Space
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, 0, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(-size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, 0, 0.0D).color(127, 127, 127, 127).endVertex();
                tessellator.draw();

                // Health Bar
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, 0, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(-size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(healthSize * 2 - size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(healthSize * 2 - size, 0, 0.0D).color(r, g, b, 127).endVertex();
                tessellator.draw();

                // Exp Bar
                r = 64;
                g = 64;
                b = 255;

                int exp = pokemob.getExp() - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                float maxExp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1)
                        - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                if (pokemob.getLevel() == 100) maxExp = exp = 1;
                if (exp < 0 || !pokemob.getGeneralState(GeneralStates.TAMED))
                {
                    exp = 0;
                }
                float expSize = size * (exp / maxExp);
                // Gray Space
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(-size, barHeight1 + 1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, barHeight1 + 1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                tessellator.draw();

                // Health Bar
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(-size, barHeight1 + 1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(expSize * 2 - size, barHeight1 + 1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(expSize * 2 - size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                tessellator.draw();

                GlStateManager.enableTexture2D();

                GlStateManager.pushMatrix();
                GlStateManager.translate(-size, -4.5F, 0F);
                GlStateManager.scale(s, s, s);

                UUID owner = pokemob.getPokemonOwnerID();
                boolean isOwner = viewerID.equals(owner);
                int colour = isOwner ? config.ownedNameColour
                        : owner == null ? nametag ? scanned ? config.scannedNameColour : config.caughtNamedColour
                                : config.unknownNameColour : config.otherOwnedNameColour;
                mc.fontRenderer.drawString(name, 0, 0, colour);

                GlStateManager.pushMatrix();
                float s1 = 0.75F;
                GlStateManager.scale(s1, s1, s1);

                int h = config.hpTextHeight;
                String maxHpStr = "" + (int) (Math.round(maxHealth * 100.0) / 100.0);
                String hpStr = "" + (int) (Math.round(health * 100.0) / 100.0);
                String healthStr = hpStr + "/" + maxHpStr;
                String gender = pokemob.getSexe() == IPokemob.MALE ? "\u2642"
                        : pokemob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";
                String lvlStr = "L." + pokemob.getLevel();

                if (maxHpStr.endsWith(".0")) maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 2);
                if (hpStr.endsWith(".0")) hpStr = hpStr.substring(0, hpStr.length() - 2);
                colour = 0xBBBBBB;
                if (pokemob.getSexe() == IPokemob.MALE)
                {
                    colour = 0x0011CC;
                }
                else if (pokemob.getSexe() == IPokemob.FEMALE)
                {
                    colour = 0xCC5555;
                }
                if (isOwner) mc.fontRenderer.drawString(healthStr,
                        (int) (size / (s * s1)) - mc.fontRenderer.getStringWidth(healthStr) / 2, h, 0xFFFFFFFF);
                mc.fontRenderer.drawString(lvlStr, 2, h, 0xFFFFFF);
                mc.fontRenderer.drawString(gender,
                        (int) (size / (s * s1) * 2) - 2 - mc.fontRenderer.getStringWidth(gender), h - 1, colour);
                if (PokecubeMod.core.getConfig().enableDebugInfo && mc.gameSettings.showDebugInfo)
                {
                    String entityID = EntityList.getKey(entity).toString();
                    mc.fontRenderer.drawString("ID: \"" + entityID + "\"" + "(" + entity.getEntityId() + ")", 0, h + 16,
                            0xFFFFFFFF);
                }
                GlStateManager.popMatrix();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int off = 0;
                s1 = 0.5F;
                GlStateManager.scale(s1, s1, s1);
                GlStateManager.translate(size / (s * s1) * 2 - 16, 0F, 0F);
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (CompatWrapper.isValid(stack) && config.showHeldItem)
                {
                    renderIcon(off, 0, stack, 16, 16);
                    off -= 16;
                }

                if (armor > 0 && config.showArmor)
                {
                    int ironArmor = armor % 5;
                    int diamondArmor = armor / 5;
                    if (!config.groupArmor)
                    {
                        ironArmor = armor;
                        diamondArmor = 0;
                    }

                    stack = new ItemStack(Items.IRON_CHESTPLATE);
                    for (int i = 0; i < ironArmor; i++)
                    {
                        renderIcon(off, 0, stack, 16, 16);
                        off -= 4;
                    }

                    stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                    for (int i = 0; i < diamondArmor; i++)
                    {
                        renderIcon(off, 0, stack, 16, 16);
                        off -= 4;
                    }
                }

                GlStateManager.popMatrix();
                this.postRender();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();

                pastTranslate -= bgHeight + barHeight1 + padding;
            }
        }
    }

    public static void renderIcon(int vertexX, int vertexY, ItemStack stack, int intU, int intV)
    {
        try
        {
            IBakedModel iBakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
            TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(iBakedModel.getParticleTexture().getIconName());
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos((vertexX), vertexY + intV, 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMaxV())
                    .endVertex();
            buffer.pos(vertexX + intU, vertexY + intV, 0.0D)
                    .tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMaxV()).endVertex();
            buffer.pos(vertexX + intU, (vertexY), 0.0D).tex(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMinV())
                    .endVertex();
            buffer.pos((vertexX), (vertexY), 0.0D).tex(textureAtlasSprite.getMinU(), textureAtlasSprite.getMinV())
                    .endVertex();
            tessellator.draw();
        }
        catch (Exception e)
        {
        }
    }

    public static Entity getEntityLookedAt(Entity e)
    {
        Entity foundEntity = null;

        final double finalDistance = 32;
        double distance = finalDistance;
        RayTraceResult pos = raycast(e, finalDistance);

        Vec3d positionVector = e.getPositionVector();
        if (e instanceof EntityPlayer) positionVector = positionVector.addVector(0, e.getEyeHeight(), 0);

        if (pos != null) distance = pos.hitVec.distanceTo(positionVector);

        Vec3d lookVector = e.getLookVec();
        Vec3d reachVector = positionVector.addVector(lookVector.x * finalDistance, lookVector.y * finalDistance,
                lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e
                .getEntityBoundingBox()
                .expand(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance)
                .grow(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox)
        {
            if (entity.canBeCollidedWith())
            {
                float collisionBorderSize = entity.getCollisionBorderSize();
                AxisAlignedBB hitbox = entity.getEntityBoundingBox().grow(collisionBorderSize, collisionBorderSize,
                        collisionBorderSize);
                RayTraceResult interceptPosition = hitbox.calculateIntercept(positionVector, reachVector);

                if (hitbox.contains(positionVector))
                {
                    if (0.0D < minDistance || minDistance == 0.0D)
                    {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                }
                else if (interceptPosition != null)
                {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.hitVec);

                    if (distanceToEntity < minDistance || minDistance == 0.0D)
                    {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if (lookedEntity != null && (minDistance < distance || pos == null)) foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    public static RayTraceResult raycast(Entity e, double len)
    {
        Vec3d vec = new Vec3d(e.posX, e.posY, e.posZ);
        if (e instanceof EntityPlayer) vec = vec.add(new Vec3d(0, e.getEyeHeight(), 0));

        Vec3d look = e.getLookVec();
        if (look == null) return null;

        return raycast(e.getEntityWorld(), vec, look, len);
    }

    public static RayTraceResult raycast(World world, Vec3d origin, Vec3d ray, double len)
    {
        Vec3d end = origin.add(ray.normalize().scale(len));
        RayTraceResult pos = world.rayTraceBlocks(origin, end);
        return pos;
    }
}
