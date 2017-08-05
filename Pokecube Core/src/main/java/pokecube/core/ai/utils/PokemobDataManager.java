package pokecube.core.ai.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Sets;

import io.netty.handler.codec.EncoderException;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.pokemobs.PacketPokemobMetadata;

/** This is a custom implementation of the EntityDataManager to allow syncing
 * certain data values when the entity for the data manager is not the actual
 * entity in the world.. This is used as some of the data values are not synced
 * at appropriate times for players who have transformed into pokemobs, and this
 * was the easiest solution I could come up with for manually syncing only
 * specific values. */
public class PokemobDataManager extends EntityDataManager
{
    final Entity                                       entity;
    final Map<Integer, EntityDataManager.DataEntry<?>> entrySet;
    private final ReadWriteLock                        theLock;
    public Set<DataParameter<?>>                       manualSyncSet = Sets.newHashSet();
    public final EntityDataManager                     wrappedManager;

    public PokemobDataManager(Entity entityIn)
    {
        super(entityIn);
        this.entity = entityIn;
        wrappedManager = entityIn.getDataManager();
        entrySet = ReflectionHelper.getPrivateValue(EntityDataManager.class, wrappedManager, "entries",
                "field_187234_c", "d");
        theLock = ReflectionHelper.getPrivateValue(EntityDataManager.class, wrappedManager, "lock", "field_187235_d",
                "e");
        ReflectionHelper.setPrivateValue(EntityDataManager.class, this, entrySet, "entries", "field_187234_c", "d");
    }

    @Override
    public <T> void set(DataParameter<T> key, T value)
    {
        EntityDataManager.DataEntry<T> entry = getEntry(key);
        T old = entry.getValue();
        wrappedManager.set(key, value);
        if (entity.getEntityWorld() == null || !ObjectUtils.notEqual(value, old)
                || entity.getEntityWorld().getEntityByID(entity.getEntityId()) == entity)
            return;
        if (!entity.getEntityWorld().isRemote && manualSyncSet.contains(key))
        {
            PacketPokemobMetadata message = new PacketPokemobMetadata();
            message.wrapped.writeInt(entity.getEntityId());
            try
            {
                writeEntry(message.wrapped, entry);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            PokecubeMod.packetPipeline.sendToDimension(message, entity.dimension);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void readEntry(PacketBuffer buf) throws IOException
    {
        short id = buf.readUnsignedByte();
        int i = buf.readVarInt();
        DataSerializer<?> dataserializer = DataSerializers.getSerializer(i);
        Object value = dataserializer.read(buf);
        DataParameter key = dataserializer.createKey(id);
        wrappedManager.set(key, value);
    }

    private static <T> void writeEntry(PacketBuffer buf, EntityDataManager.DataEntry<T> entry) throws IOException
    {
        DataParameter<T> dataparameter = entry.getKey();
        int i = DataSerializers.getSerializerId(dataparameter.getSerializer());
        if (i < 0) { throw new EncoderException("Unknown serializer type " + dataparameter.getSerializer()); }
        buf.writeByte(dataparameter.getId());
        buf.writeVarInt(i);
        dataparameter.getSerializer().write(buf, entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private <T> EntityDataManager.DataEntry<T> getEntry(DataParameter<T> key)
    {
        this.theLock.readLock().lock();
        EntityDataManager.DataEntry<T> dataentry;

        try
        {
            dataentry = (EntityDataManager.DataEntry<T>) this.entrySet.get(Integer.valueOf(key.getId()));
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Synched entity data");
            crashreportcategory.addCrashSection("Data ID", key);
            throw new ReportedException(crashreport);
        }

        this.theLock.readLock().unlock();
        return dataentry;
    }
}
