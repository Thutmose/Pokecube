package pokecube.adventures.events;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.handlers.PlayerAsPokemobManager;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.maths.Vector3;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.ICosmeticAttachable;

@SideOnly(Side.CLIENT)
public class RenderHandler
{

	public static float				partialTicks	= 0.0F;
	public static boolean BOTANIA = false;

	public RenderHandler()
	{
	}

	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Post event)
	{

		if (BOTANIA) { return; }

		EntityPlayer player = event.entityPlayer;
		InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);

		GL11.glPushMatrix();
		float yaw = 180;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.partialRenderTick;
		yaw += yawOffset;

		
		
		GL11.glRotatef(-yaw, 0, 1, 0);
		GL11.glTranslated(0, 1.4, 0);
		
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		boolean loop = false;
		for (int i = 2; i < trace.length; i++)
		{
			if (trace[i].getClassName().toLowerCase().contains("pokedex"))
			{
				loop = true;
				break;
			}
		}
		if(loop)
		{
			GL11.glTranslated(-1, 0.35, 2);
		}

		GL11.glRotatef(180, 0, 0, 1);
		
		dispatchRenders(inv, event, RenderType.BODY);
		GL11.glPopMatrix();

		yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.partialRenderTick;
		yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.partialRenderTick;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.partialRenderTick;
		
		GL11.glPushMatrix();
		GL11.glRotatef(yawOffset, 0, -1, 0);
		GL11.glRotatef(yaw - 270, 0, 1, 0);
		GL11.glRotatef(pitch, 0, 0, 1);
		dispatchRenders(inv, event, RenderType.HEAD);

		GL11.glPopMatrix();
	}

	@SubscribeEvent
	public void keyInput(KeyInputEvent evt)
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
		boolean bag = false;
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null)
			{
				Item item = stack.getItem();
				if (item instanceof ItemBag)
				{
					bag = true;
					break;
				}
			}
		}
		if (bag && Keyboard.getEventKey() == ClientProxy.bag.getKeyCode())
		{
			PacketPokeAdv.sendBagOpenPacket(false, Vector3.empty);
		}

		IPokemob entity =  PlayerAsPokemobManager.getInstance().getTransformed(player);
		if(entity!=null && Keyboard.getEventKey() == ClientProxyPokecube.mobAttack.getKeyCode())
		{
			Vector3 here = Vector3.getNewVectorFromPool().set(player, false);
			Entity hit = here.firstEntityExcluding(16, Vector3.getNewVectorFromPool().set(player.getLookVec()), player.worldObj, false, player);
			if(hit!=null)
			{
				PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
				buffer.writeByte(10);
				buffer.writeInt(hit.getEntityId());
				MessageServer message = new MessageServer(buffer);
				PokecubeMod.packetPipeline.sendToServer(message);
			}
		}
	}

	@SubscribeEvent
	public void onToolTop(ItemTooltipEvent evt)
	{
		EntityPlayer player = evt.entityPlayer;
		ItemStack stack = evt.itemStack;
		if (player.openContainer instanceof ContainerCloner && stack.getItem() instanceof ItemPokemobEgg)
		{
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
			{
				evt.toolTip.add("" + stack.getTagCompound().getLong("ivs") + ":"
						+ stack.getTagCompound().getFloat("size") + ":" + stack.getTagCompound().getByte("nature"));
			}
		}
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
//		partialTicks = event.renderTickTime;
//		Minecraft mc = Minecraft.getMinecraft();
//		if (true)
//		{
//			if (this.alt == null)
//			{
//				this.alt = new EntityRendererAsPokemob(mc);
//			}
//			if (mc.entityRenderer != this.alt)
//			{
//				this.prevAlt = mc.entityRenderer;
//				mc.entityRenderer = this.alt;
//			}
//		}
//		// else if ((this.prevAlt != null) && (mc.entityRenderer !=
//		// this.prevAlt))
//		// {
//		// mc.entityRenderer = this.prevAlt;
//		// }
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
//		Minecraft mc = Minecraft.getMinecraft();
//		
//		World world = mc.theWorld;
//		if ((world != null) && (this.prevWorld != world))
//		{
//			// TODO update request packet here
//			this.prevWorld = world;
//		}
//		if ((true) && (mc.thePlayer != null) && (world != null)// replace true
//																// with a check
//																// of if the
//																// above update
//																// occured
//				&& (world.getWorldTime() % 20L == 0L))
//		{
//			List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class,
//					mc.thePlayer.boundingBox.expand(64.0D, 64.0D, 64.0D));
//			for (EntityPlayer player : list)
//			{
//				if ((player != mc.thePlayer) && (
//
//				(this.playerlist == null) || (!this.playerlist.contains(player))))
//				{
//					// Send Client Data request packet here
//				}
//			}
//			this.playerlist = list;
//		}
	}

    HashMap<String, Float> heights = new HashMap<String, Float>();
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
//		if ((event.side == Side.SERVER) || (event.phase == TickEvent.Phase.START)) { return; }
//		EntityPlayer player = event.player;
//		IPokemob entity =  PlayerAsPokemobManager.getInstance().getTransformed(player);
//		if (entity != null)
//		{
//			PlayerAsPokemobManager.copyEntity(player, (EntityLivingBase) entity);
//			((EntityLiving)entity).onUpdate();
//		}
//		if (entity != null)
//		{
//			if(!heights.containsKey(player.getName()))
//			{
//				heights.put(player.getName(), player.height);
//			}
//			player.height = entity.getSize() * entity.getPokedexEntry().height;
//			player.eyeHeight = 0 - 2f + player.height;//player.height;
//			if(player.boundingBox!=null)
//			{
//				player.boundingBox.maxY = player.boundingBox.minY + player.height;
//			}
//		}
//		else if(heights.containsKey(player.getName()))
//		{
//			player.height = heights.remove(player.getName());	
//			player.eyeHeight = 0f;
//			if(player.boundingBox!=null)
//			{
//				player.boundingBox.maxY = player.boundingBox.minY + player.height;
//			}
//		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void pre(RenderPlayerEvent.Pre event)
	{
//		EntityPlayer player = event.entityPlayer;
//		EntityLiving entity = (EntityLiving) PlayerAsPokemobManager.getInstance().getTransformed(player);
//		if (entity != null)
//		{
//			entity.posY -= player.yOffset;
//			PlayerAsPokemobManager.copyEntity(player, entity);
//			entity.onUpdate();
//		}
	}

	private void dispatchRenders(InventoryBaubles inv, RenderPlayerEvent event, RenderType type)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack != null) {
				Item item = stack.getItem();

				if(item instanceof ICosmeticAttachable) {
					ICosmeticAttachable attachable = (ICosmeticAttachable) item;
					ItemStack cosmetic = attachable.getCosmeticItem(stack);
					if(cosmetic != null) {
						GL11.glPushMatrix();
						GL11.glColor4f(1F, 1F, 1F, 1F);
						((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, event, type);
						GL11.glPopMatrix();
						continue;
					}
				}

				if(item instanceof IBaubleRender) {
					GL11.glPushMatrix();
					GL11.glColor4f(1F, 1F, 1F, 1F);
					((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, event, type);
					GL11.glPopMatrix();
				}
			}
		}
	}
}
