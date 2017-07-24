package pokecube.compat.top;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class TheOneProbeCompat implements IProbeInfoProvider, IProbeInfoEntityProvider
{
    private static class Element implements IElement
    {
        PokedexEntry entry;
        int          have   = 0;
        int          killed = 0;

        public Element(ByteBuf buf)
        {
            PacketBuffer buffer = new PacketBuffer(buf);
            have = buffer.readInt();
            killed = buffer.readInt();
            entry = Database.getEntry(buffer.readString(20));
        }

        public Element(IPokemob pokemob, EntityPlayer player)
        {
            entry = pokemob.getPokedexEntry();
            int caught = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUniqueID(), pokemob.getPokedexEntry());
            int hatched = EggStats.getTotalNumberOfPokemobHatchedBy(player.getUniqueID(), pokemob.getPokedexEntry());
            have = caught + hatched;
            killed = KillStats.getTotalNumberOfPokemobKilledBy(player.getUniqueID(), pokemob.getPokedexEntry());
        }

        @Override
        public int getHeight()
        {
            return 20;
        }

        @Override
        public int getID()
        {
            return ELEMENT;
        }

        @Override
        public int getWidth()
        {
            return 100;
        }

        @Override
        public void render(int x, int y)
        {
            if (entry == null) return;
            Minecraft.getMinecraft().fontRenderer.drawString(PokeType.getTranslatedName(entry.getType1()), x, y,
                    entry.getType1().colour, true);
            if (PokeType.unknown != entry.getType2() && entry.getType2() != null)
            {
                int l = Minecraft.getMinecraft().fontRenderer
                        .getStringWidth(PokeType.getTranslatedName(entry.getType1()));
                Minecraft.getMinecraft().fontRenderer.drawString(PokeType.getTranslatedName(entry.getType2()),
                        l + 2 + x, y, entry.getType2().colour, true);
            }

            int l = 0;
            Minecraft.getMinecraft().fontRenderer.drawString(have + "", l + x, y + 10, PokeType.getType("grass").colour, true);
            l += Minecraft.getMinecraft().fontRenderer.getStringWidth(have + "");
            Minecraft.getMinecraft().fontRenderer.drawString("/", l + x, y + 10, PokeType.getType("normal").colour, true);
            l += Minecraft.getMinecraft().fontRenderer.getStringWidth("/");
            Minecraft.getMinecraft().fontRenderer.drawString(killed + "", l + x, y + 10, PokeType.getType("fighting").colour,
                    true);

        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            PacketBuffer buffer = new PacketBuffer(buf);
            buffer.writeInt(have);
            buffer.writeInt(killed);
            buffer.writeString(entry.getName());
        }

    }

    @Optional.Method(modid = "theoneprobe")
    @CompatClass(phase = Phase.POST)
    public static void TheOneProbe_Compat()
    {
        System.out.println("TheOneProbe Compat");
        new pokecube.compat.top.TheOneProbeCompat();
    }
    private static ITheOneProbe probe;
    private static int          ELEMENT;

    IElementFactory             factory;

    public TheOneProbeCompat()
    {
        probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(this);
        probe.registerEntityProvider(this);

        factory = new IElementFactory()
        {
            @Override
            public IElement createElement(ByteBuf buf)
            {
                return new Element(buf);
            }
        };
        ELEMENT = probe.registerElementFactory(factory);
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
            Entity entity, IProbeHitEntityData data)
    {
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null) probeInfo.element(new Element(mob, player));
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
            IBlockState blockState, IProbeHitData data)
    {
        // TODO Auto-generated method stub
        // System.out.println(mode+" "+player+" "+blockState);
    }

    @Override
    public String getID()
    {
        return "pokecube_compat";
    }

    public void GetTheOneProbe(ITheOneProbe in)
    {
        probe = in;
        System.out.println(in);
    }
}
