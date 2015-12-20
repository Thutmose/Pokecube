package pokecube.core.interfaces;

import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;

public interface IBreedingMob
{
    /** @return the byte sexe */
    byte getSexe();

    /** @param sexe
     *            the byte sexe */
    void setSexe(byte sexe);

    /** Which entity is this pokemob trying to breed with
     * 
     * @return */
    Entity getLover();

    /** Sets the entity to try to breed with
     * 
     * @param lover */
    void setLover(Entity lover);

    /** resets the status of being in love */
    void resetLoveStatus();

    /** @return the timer indcating delay between looking for a mate. */
    int getLoveTimer();

    /** Sets the timer for the delay between looking for a mate.
     * 
     * @param value */
    void setLoveTimer(int value);
    
    boolean isInLove();

    /** Will be called by the mother before she lays to know what baby to put in
     * the egg.
     *
     * @param male
     *            the male
     * @return the pokedex number of the child */
    Object getChild(IBreedingMob male);
    
    Vector<IBreedingMob> getMalesForBreeding();
    
    boolean canMateWith(EntityAnimal entityAnimal);
    
    void mateWith(IBreedingMob male);
}
