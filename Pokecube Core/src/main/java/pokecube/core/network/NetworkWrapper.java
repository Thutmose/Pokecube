package pokecube.core.network;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.PokecubeMod;

public class NetworkWrapper
{
    public EnumMap<Side, FMLEmbeddedChannel> channels;
    private SimpleIndexedCodec               packetCodec;
    private static Class<?>                  defaultChannelPipeline;
    private static Method                    generateName;
    {
        try
        {
            defaultChannelPipeline = Class.forName("io.netty.channel.DefaultChannelPipeline");
            generateName = defaultChannelPipeline.getDeclaredMethod("generateName", ChannelHandler.class);
            generateName.setAccessible(true);
        }
        catch (Exception e)
        {
            // How is this possible?
            PokecubeMod.log(Level.SEVERE, "What? Netty isn't installed, what magic is this?", e);
            throw new RuntimeException(e);
        }
    }

    public NetworkWrapper(String channelName)
    {
        packetCodec = new SimpleIndexedCodec();
        channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec);
    }

    private String generateName(ChannelPipeline pipeline, ChannelHandler handler)
    {
        try
        {
            return (String) generateName.invoke(defaultChannelPipeline.cast(pipeline), handler);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "It appears we somehow have a not-standard pipeline. Huh", e);
            throw new RuntimeException(e);
        }
    }

    /** Register a message and it's associated handler. The message will have
     * the supplied discriminator byte. The message handler will be registered
     * on the supplied side (this is the side where you want the message to be
     * processed and acted upon).
     *
     * @param messageHandler
     *            the message handler type
     * @param requestMessageType
     *            the message type
     * @param discriminator
     *            a discriminator byte
     * @param side
     *            the side for the handler */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType,
            int discriminator, Side side)
    {
        registerMessage(instantiate(messageHandler), requestMessageType, discriminator, side);
    }

    static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(
            Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler)
    {
        try
        {
            return handler.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /** Register a message and it's associated handler. The message will have
     * the supplied discriminator byte. The message handler will be registered
     * on the supplied side (this is the side where you want the message to be
     * processed and acted upon).
     *
     * @param messageHandler
     *            the message handler instance
     * @param requestMessageType
     *            the message type
     * @param discriminator
     *            a discriminator byte
     * @param side
     *            the side for the handler */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType,
            int discriminator, Side side)
    {
        packetCodec.addDiscriminator(discriminator, requestMessageType);
        FMLEmbeddedChannel channel = channels.get(side);
        String type = channel.findChannelHandlerNameForType(SimpleIndexedCodec.class);
        if (side == Side.SERVER)
        {
            addServerHandlerAfter(channel, type, messageHandler, requestMessageType);
        }
        else
        {
            addClientHandlerAfter(channel, type, messageHandler, requestMessageType);
        }
    }

    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addServerHandlerAfter(
            FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
            Class<REQ> requestType)
    {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.SERVER, requestType);
        channel.pipeline().addAfter(type, generateName(channel.pipeline(), handler), handler);
    }

    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addClientHandlerAfter(
            FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
            Class<REQ> requestType)
    {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.CLIENT, requestType);
        channel.pipeline().addAfter(type, generateName(channel.pipeline(), handler), handler);
    }

    private <REPLY extends IMessage, REQ extends IMessage> SimpleChannelHandlerWrapper<REQ, REPLY> getHandlerWrapper(
            IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Side side, Class<REQ> requestType)
    {
        return new SimpleChannelHandlerWrapper<REQ, REPLY>(messageHandler, side, requestType);
    }

    /** Construct a minecraft packet from the supplied message. Can be used
     * where minecraft packets are required, such as
     * {@link TileEntity#getDescriptionPacket()}.
     *
     * @param message
     *            The message to translate into packet form
     * @return A minecraft {@link Packet} suitable for use in minecraft APIs */
    public Packet<?> getPacketFrom(IMessage message)
    {
        return channels.get(Side.SERVER).generatePacketFrom(message);
    }

    /** Send this message to everyone. The {@link IMessageHandler} for this
     * message type should be on the CLIENT side.
     *
     * @param message
     *            The message to send */
    public void sendToAll(IMessage message)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /** Send this message to the specified player. The {@link IMessageHandler}
     * for this message type should be on the CLIENT side.
     *
     * @param message
     *            The message to send
     * @param player
     *            The player to send it to */
    public void sendTo(IMessage message, EntityPlayerMP player)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendTo(IMessage message, Channel channel)
    {
        PokecubeMod.packetPipeline.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
        PokecubeMod.packetPipeline.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
                .set(channel.attr(NetworkDispatcher.FML_DISPATCHER).get());
        PokecubeMod.packetPipeline.channels.get(Side.SERVER).writeOutbound(message);
    }

    /** Send this message to everyone within a certain range of a point. The
     * {@link IMessageHandler} for this message type should be on the CLIENT
     * side.
     *
     * @param message
     *            The message to send
     * @param point
     *            The {@link TargetPoint} around which to send */
    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /** Send this message to everyone within the supplied dimension. The
     * {@link IMessageHandler} for this message type should be on the CLIENT
     * side.
     *
     * @param message
     *            The message to send
     * @param dimensionId
     *            The dimension id to target */
    public void sendToDimension(IMessage message, int dimensionId)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /** Send this message to the server. The {@link IMessageHandler} for this
     * message type should be on the SERVER side.
     *
     * @param message
     *            The message to send */
    public void sendToServer(IMessage message)
    {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
