package pokecube.core.database.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.utils.PokeType;

public class AbilityManager {
	private static HashMap<String, Ability> abilities = new HashMap();
	
	public static Ability getAbility(String name)
	{
		if(name==null)
			return null;
		
		return abilities.get(name);
	}
	
	static 
	{
		abilities.put("Wonder Guard", new Ability("Wonder Guard") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
				
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	
		    	if(attacker==mob || !move.pre ||  attacker==move.attacked) 
		    		return;
				
	    		float eff = PokeType.getAttackEfficiency(attack.getType(), mob.getType1(), mob.getType2());
	    		if(eff<=1 && attack.getPWR(attacker, (Entity) mob) > 0)
	    		{
	    			move.canceled = true;
	    		}
			}

			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});

		addGrassAbilities();
		addFireAbilities();
		addWaterAbilities();
		addBugAbilities();
		addElectricAbilities();
		addMiscAbilities();
		
	}

	private static void addGrassAbilities()
	{
		abilities.put("Effect Spore", new Ability("Effect Spore") {
			
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || move.pre ||  attacker==move.attacked || attacker.isType(PokeType.grass)) 
		    		return;
		    	if(move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
		    	{
		    		int num = new Random().nextInt(30);
		    		if(num < 9)
		    		{
		    			move.attacker.setStatus(IMoveConstants.STATUS_PSN);
		    		}
		    		if(num < 19)
		    		{
		    			move.attacker.setStatus(IMoveConstants.STATUS_PAR);
		    		}
		    		else
		    		{
		    			move.attacker.setStatus(IMoveConstants.STATUS_SLP);
		    		}
		    	}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		

		abilities.put("Overgrow", new Ability("Overgrow") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
				if(mob==move.attacker && move.attackType == PokeType.grass && ((EntityLivingBase)mob).getHealth() < ((EntityLivingBase)mob).getMaxHealth()/3)
				{
					move.PWR *= 1.5;
				}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
	}
	private static void addFireAbilities()
	{
		abilities.put("Blaze", new Ability("Blaze") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
				if(mob==move.attacker && move.attackType == PokeType.fire && ((EntityLivingBase)mob).getHealth() < ((EntityLivingBase)mob).getMaxHealth()/3)
				{
					move.PWR *= 1.5;
				}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});

		abilities.put("Flame Body", new Ability("Flame Body") {
			
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || move.pre ||  attacker==move.attacked) 
		    		return;
		    	if(move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
		    	{
		    		move.attacker.setStatus(IMoveConstants.STATUS_BRN);
		    	}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Magma Armor", new Ability("Magma Armor")  {
			
			@Override
			public void onUpdate(IPokemob mob) 
			{
				if(mob.getStatus() == IMoveConstants.STATUS_FRZ)
					mob.setStatus(IMoveConstants.STATUS_NON);
			}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || !move.pre ||  attacker==move.attacked) 
		    		return;
		    	if(move.statusChange == IMoveConstants.STATUS_FRZ)
		    		move.statusChange = IMoveConstants.STATUS_FRZ;
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
	}
	private static void addWaterAbilities()
	{

		abilities.put("Torrent", new Ability("Torrent") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
				if(mob==move.attacker && move.attackType == PokeType.water && ((EntityLivingBase)mob).getHealth() < ((EntityLivingBase)mob).getMaxHealth()/3)
				{
					move.PWR *= 1.5;
				}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
	}
	private static void addBugAbilities()
	{

		abilities.put("Swarm", new Ability("Swarm") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
				if(mob==move.attacker && move.attackType == PokeType.bug && ((EntityLivingBase)mob).getHealth() < ((EntityLivingBase)mob).getMaxHealth()/3)
				{
					move.PWR *= 1.5;
				}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
	}
	private static void addElectricAbilities()
	{

		abilities.put("Static", new Ability("Static") {
			
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || move.pre ||  attacker==move.attacked) 
		    		return;
		    	if(move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
		    	{
		    		move.attacker.setStatus(IMoveConstants.STATUS_PAR);
		    	}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
	}
	private static void addMiscAbilities()
	{
		abilities.put("Levitate", new Ability("Levitate") {
			
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || !move.pre ||  attacker==move.attacked) 
		    		return;
		    	if(attack.getType() == PokeType.ground)
		    		move.canceled = true;
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		abilities.put("Insomnia", new Ability("Insomnia") {
			
			@Override
			public void onUpdate(IPokemob mob) {
				if(mob.getStatus() == IMoveConstants.STATUS_SLP)
					mob.setStatus(IMoveConstants.STATUS_NON);
			}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
		    	Move_Base attack = move.getMove();
		    	
		    	IPokemob attacker = move.attacker;
		    	if(attacker==mob || !move.pre ||  attacker==move.attacked) 
		    		return;
		    	if(move.statusChange == IMoveConstants.STATUS_SLP)
		    		move.statusChange = IMoveConstants.STATUS_NON;
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Adaptability", new Ability("Adaptability") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
				if(mob==move.attacker)
					move.stabFactor = 2;
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Aerilate", new Ability("Aerilate") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {

				if(!move.pre)
					return;
		    	if(move.attackType == PokeType.normal && mob==move.attacker)
		    	{
		    		move.attackType = PokeType.flying;
		    	}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Shed Skin", new Ability("Shed Skin") {
			@Override
			public void onUpdate(IPokemob mob) 
			{
				if(mob.getStatus()!=IMoveConstants.STATUS_NON)
				{
					EntityLivingBase poke = (EntityLivingBase) mob;
					if(poke.ticksExisted%20==0 && Math.random() < 0.3)
					{
						mob.setStatus(IMoveConstants.STATUS_NON);
					}
				}
			}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Rivalry", new Ability("Rivalry") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {
				
				if(!move.pre)
					return;
				
				if(mob==move.attacker && move.attacked instanceof IPokemob)
				{
					IPokemob target = (IPokemob) move.attacked;
					byte mobGender = mob.getSexe();
					byte targetGender = target.getSexe();
					if(mobGender == IPokemob.SEXLEGENDARY || targetGender == IPokemob.SEXLEGENDARY || mobGender == IPokemob.NOSEXE || targetGender == IPokemob.NOSEXE)
					{
						return;
					}
					
					if(mobGender == targetGender)
					{
						move.PWR *= 1.25;
					}
					else
					{
						move.PWR *= 0.75;
					}
				}
			}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});

		abilities.put("Pickup", new Ability("Pickup") {
			@Override
			public void onUpdate(IPokemob mob) 
			{
				EntityLivingBase poke = (EntityLivingBase) mob;
				if(poke.ticksExisted%200==0 && Math.random() < 0.1)
				{
					if(poke.getHeldItem()==null)
					{
						List items = new ArrayList(PokecubeItems.heldItems);
						Collections.shuffle(items);
						ItemStack item = (ItemStack) items.get(0);
						
						if(item!=null)
							poke.setCurrentItemOrArmor(0, item.copy());
					}
				}
			}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
		
		abilities.put("Honey Gather", new Ability("Honey Gather") {
			@Override
			public void onUpdate(IPokemob mob) {}
			
			@Override
			public void onMoveUse(IPokemob mob, MovePacket move) {}
			
			@Override
			public void onAgress(IPokemob mob, EntityLivingBase target) {}
		});
	}
	
	public static boolean hasAbility(String abilityName, IPokemob pokemob)
	{
		Ability ability = pokemob.getMoveStats().ability;
		if(ability==null)
		{
			return false;
		}
		return ability.name.equalsIgnoreCase(abilityName);
	}
}
