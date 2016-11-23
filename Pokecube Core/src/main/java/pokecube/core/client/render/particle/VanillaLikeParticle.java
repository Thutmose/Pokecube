package pokecube.core.client.render.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class VanillaLikeParticle extends Particle
{
    public static class VanillaLikeParticleFactory implements IParticleFactory
    {
        private ResourceLocation textureMap;

        public VanillaLikeParticleFactory(ResourceLocation textureMap)
        {
            this.textureMap = textureMap;
        }

        @Override
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
                double xSpeedIn, double ySpeedIn, double zSpeedIn, int... args)
        {
            VanillaLikeParticle particle = new VanillaLikeParticle(worldIn, xCoordIn, yCoordIn, zCoordIn);
            particle.textureMap = textureMap;
            if (args.length > 0)
            {
                int rgba = args[0];
                float alpha = ((rgba >> 24) & 255) / 255f;
                float red = ((rgba >> 16) & 255) / 255f;
                float green = ((rgba >> 8) & 255) / 255f;
                float blue = (rgba & 255) / 255f;
                particle.setAlphaF(alpha);
                particle.setRBGColorF(red, green, blue);
            }
            if (args.length > 1) particle.setMaxAge(Math.max(2, args[1]));
            return particle;
        }
    }

    public static class AuroraParticleFactory implements IParticleFactory
    {
        private ResourceLocation textureMap;

        public AuroraParticleFactory(ResourceLocation textureMap)
        {
            this.textureMap = textureMap;
        }

        @Override
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
                double xSpeedIn, double ySpeedIn, double zSpeedIn, int... args)
        {
            ParticleAurora particle = new ParticleAurora(worldIn, xCoordIn, yCoordIn, zCoordIn);
            particle.textureMap = textureMap;
            particle.start = (int) (worldIn.getTotalWorldTime() % 100000);
            if (args.length > 0)
            {
                int rgba = args[0];
                float alpha = ((rgba >> 24) & 255) / 255f;
                float red = ((rgba >> 16) & 255) / 255f;
                float green = ((rgba >> 8) & 255) / 255f;
                float blue = (rgba & 255) / 255f;
                particle.setAlphaF(alpha);
                particle.setRBGColorF(red, green, blue);
            }
            if (args.length > 1) particle.setMaxAge(Math.max(2, args[1]));
            return particle;
        }
    }

    ResourceLocation textureMap;

    protected VanillaLikeParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        textureMap = null;
    }

    /** Renders the particle */
    @Override
    public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
            float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        if (textureMap != null) Minecraft.getMinecraft().renderEngine.bindTexture(textureMap);
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
                rotationXZ);
    }

    public static class ParticleAurora extends VanillaLikeParticle
    {
        private int speed = 2;
        private int start = 0;

        protected ParticleAurora(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
        {
            super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }

        @Override
        public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
                float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
        {
            setColour();
            super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
                    rotationXZ);
        }

        private void setColour()
        {
            int time = particleAge + start;
            int num = (time / speed) % 16;
            int rgba = EnumDyeColor.byMetadata(num).getMapColor().colorValue;
            float red = ((rgba >> 16) & 255) / 255f;
            float green = ((rgba >> 8) & 255) / 255f;
            float blue = (rgba & 255) / 255f;
            setRBGColorF(red, green, blue);
        }
    }
}
