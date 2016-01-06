package pokecube.core.interfaces;

import net.minecraft.entity.Entity;

/** These mobs will attempt to eat items, blocks, or other mobs.
 * 
 * @author Thutmose */
public interface IHungrymob
{
    /** Called when the mob eats the Entity e. e can be any entity, will often
     * be an EntityItem.
     * 
     * @param e */
    public void eat(Entity e);

    /** Called when the mob fails to eat the entity, this is often because it
     * was already eaten by someone else.
     * 
     * @param e */
    public void noEat(Entity e);

    /** returns true if the mob is not actually a hungry mob, but uses the
     * interface for something else.
     * 
     * @return */
    public boolean neverHungry();

    /** Mob eats rock */
    public boolean isLithotroph();

    /** Mob eats light */
    public boolean isPhototroph();

    /** Mob eats electricity */
    public boolean isElectrotroph();

    /** Mob eats berries */
    public boolean eatsBerries();

    /** Mob eats plants (grass, flowers, etc) */
    public boolean isHerbivore();

    /** Mob eats from being in water */
    public boolean filterFeeder();

    /** Mob eats other mobs */
    public boolean isCarnivore();

    /** @return Time since last meal */
    public int getHungerTime();

    /** sets time since last meal.
     * 
     * @param hungerTime */
    public void setHungerTime(int hungerTime);

    /** @return Cooldown time between looking for meal, will only look if this
     *         is less than or equal to 0 */
    public int getHungerCooldown();

    /** Sets the hungerCooldown
     * 
     * @param hungerCooldown */
    public void setHungerCooldown(int hungerCooldown);
}
