package pokecube.core.moves.animations;

import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class MoveAnimationHelper
{

	private static MoveAnimationHelper instance;

	public static MoveAnimationHelper Instance()
	{
		if (instance == null)
		{
			instance = new MoveAnimationHelper();
			FMLCommonHandler.instance().bus().register(instance);
			MinecraftForge.EVENT_BUS.register(instance);
		}
		return instance;
	}

	HashMap<Entity, HashSet<MoveAnimation>> moves = new HashMap<Entity, HashSet<MoveAnimation>>();

	public void addMove(Entity attacker, MoveAnimation move)
	{
		HashSet<MoveAnimation> moves = this.moves.get(attacker);
		if (moves == null)
		{
			moves = new HashSet<MoveAnimation>();
			this.moves.put(attacker, moves);
		}
		moves.add(move);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldPost(RenderFogEvent event)
	{

		try
		{
            GL11.glPushMatrix();
			for (Entity e : moves.keySet())
			{
				HashSet<MoveAnimation> moves = Sets.newHashSet(this.moves.get(e));

				for (MoveAnimation move : moves)
				{
					Vector3 target = Vector3.getNewVectorFromPool().set(move.targetLoc);
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					Vector3 source = Vector3.getNewVectorFromPool().set(player);
                    GL11.glPushMatrix();
					source.set(target.subtract(source));
					
					GL11.glTranslated(source.x, source.y, source.z);
					//Clear out the jitteryness from rendering
                    source.x = player.prevPosX - player.posX;
                    source.y = player.prevPosY - player.posY;
                    source.z = player.prevPosZ - player.posZ;
					source.scalarMultBy(event.renderPartialTicks);
                    GL11.glTranslated(source.x, source.y, source.z);
                    //TODO see about fixing the slight movement that occurs when the player stops or starts moving
					
					move.render(event.renderPartialTicks);
					GL11.glPopMatrix();
					source.freeVectorFromPool();
					target.freeVectorFromPool();
				}

			}
            GL11.glPopMatrix();
			for (Object e : moves.keySet())
			{
				HashSet<MoveAnimation> moves = this.moves.get(e);
				for (MoveAnimation move : moves)
				{
					if (move.lastDrop != event.entity.worldObj.getTotalWorldTime())
					{
						move.duration--;
						move.lastDrop = event.entity.worldObj.getTotalWorldTime();
					}
				}
			}
			HashSet toRemove = new HashSet();
			for (Object e : moves.keySet())
			{
				HashSet<MoveAnimation> moves = this.moves.get(e);
				HashSet remove = new HashSet();
				for (MoveAnimation move : moves)
				{
					if (move.duration < 0)
					{
						remove.add(move);
					}
				}
				moves.removeAll(remove);
				if (moves.size() == 0) toRemove.add(e);
			}
			for (Object o : toRemove)
			{
				moves.remove(o);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderPlayerPost(RenderPlayerEvent.Post event)
	{

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event)
	{

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldPre(RenderWorldEvent.Pre event)
	{
	}
	
	@SubscribeEvent
	public void WorldUnloadEvent(Unload evt)
	{
		if (evt.world.provider.getDimensionId() == 0 && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			clear();
		}

	}
	
	public void clear()
	{
		moves.clear();
	}

	public static class MoveAnimation
	{
		public final Entity		attacker;
		public final Entity		targetEnt;
		public final Vector3	targetLoc;
		public final Vector3	sourceStart;
		public final Move_Base	move;
		public int				duration;
		public long				lastDrop;
		final MovePacketInfo	info;

		public MoveAnimation(Entity attacker, Entity targetEnt, Vector3 targetLoc, Move_Base move, int time)
		{
			this.attacker = attacker;
			this.targetEnt = targetEnt;
			this.targetLoc = targetLoc;
			this.sourceStart = Vector3.getNewVectorFromPool().set(attacker).addTo(0, attacker.getEyeHeight(), 0);
			this.move = move;
			info = new MovePacketInfo(move, attacker, targetEnt, sourceStart, targetLoc);
			duration = time;
		}

		public void render(double partialTick)
		{
			if (move.animation != null)
			{
				info.currentTick = move.animation.getDuration() - duration;
				move.animation.clientAnimation(info, Minecraft.getMinecraft().renderGlobal, (float) partialTick);
			}
			else
			{
				throw(new NullPointerException("Who Registered null animation for "+move.name));
			}
		}
	}
}
