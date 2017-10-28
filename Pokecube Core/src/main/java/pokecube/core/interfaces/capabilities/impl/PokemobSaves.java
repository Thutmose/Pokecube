package pokecube.core.interfaces.capabilities.impl;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.NonPersistantAI;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.TagNames;
import thut.lib.CompatWrapper;

public abstract class PokemobSaves extends PokemobOwned implements TagNames
{

    @Override
    public void readPokemobData(NBTTagCompound tag)
    {
        NBTTagCompound ownerShipTag = tag.getCompoundTag(OWNERSHIPTAG);
        NBTTagCompound statsTag = tag.getCompoundTag(STATSTAG);
        NBTTagCompound movesTag = tag.getCompoundTag(MOVESTAG);
        NBTTagCompound inventoryTag = tag.getCompoundTag(INVENTORYTAG);
        NBTTagCompound breedingTag = tag.getCompoundTag(BREEDINGTAG);
        NBTTagCompound visualsTag = tag.getCompoundTag(VISUALSTAG);
        NBTTagCompound aiTag = tag.getCompoundTag(AITAG);
        NBTTagCompound miscTag = tag.getCompoundTag(MISCTAG);
        // Read Ownership Tag
        if (!ownerShipTag.hasNoTags())
        {
            this.setPokemobTeam(ownerShipTag.getString(TEAM));
            this.setPokemonNickname(ownerShipTag.getString(NICKNAME));
            this.players = ownerShipTag.getBoolean(PLAYERS);
            try
            {
                if (ownerShipTag.hasKey(OT)) this.setOriginalOwnerUUID(UUID.fromString(ownerShipTag.getString(OT)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                if (ownerShipTag.hasKey(OWNER)) this.setPokemonOwner(UUID.fromString(ownerShipTag.getString(OWNER)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            this.setTraded(ownerShipTag.getBoolean(ISTRADED));
        }
        // Read stats tag
        if (!statsTag.hasNoTags())
        {
            this.setExp(statsTag.getInteger(EXP), false);
            this.setStatus(statsTag.getByte(STATUS));
            addHappiness(statsTag.getInteger(HAPPY));
        }
        // Read moves tag
        if (!movesTag.hasNoTags())
        {
            if (movesTag.hasKey(NEWMOVES))
            {
                try
                {
                    getMoveStats().newMoves.clear();
                    NBTTagList newMoves = (NBTTagList) movesTag.getTag(NEWMOVES);
                    for (int i = 0; i < newMoves.tagCount(); i++)
                        if (!getMoveStats().newMoves.contains(newMoves.getStringTagAt(i)))
                            getMoveStats().newMoves.add(newMoves.getStringTagAt(i));
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error loading new moves for " + getEntity().getName(), e);
                }
            }
            this.setMoveIndex(movesTag.getInteger(MOVEINDEX));
            this.setAttackCooldown(movesTag.getInteger(COOLDOWN));
        }
        // Read Inventory tag
        if (!inventoryTag.hasNoTags())
        {
            NBTTagList nbttaglist = inventoryTag.getTagList(ITEMS, 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;
                if (j < this.getPokemobInventory().getSizeInventory())
                {
                    this.getPokemobInventory().setInventorySlotContents(j, CompatWrapper.fromTag(nbttagcompound1));
                }
                dataManager.set(params.HELDITEM, this.getPokemobInventory().getStackInSlot(1));
            }
            handleArmourAndSaddle();
        }
        // Read Breeding tag
        if (!breedingTag.hasNoTags())
        {
            this.setSexe(breedingTag.getByte(SEXE));
            this.loveTimer = breedingTag.getInteger(SEXETIME);
        }
        // Read visuals tag
        if (!visualsTag.hasNoTags())
        {
            this.setSpecialInfo(visualsTag.getInteger(SPECIALTAG));
            setSize(getSize());
            flavourAmounts = visualsTag.getIntArray(FLAVOURSTAG);
            if (visualsTag.hasKey(POKECUBE))
            {
                NBTTagCompound pokecubeTag = visualsTag.getCompoundTag(POKECUBE);
                this.setPokecube(CompatWrapper.fromTag(pokecubeTag));
            }
        }

        // Read AI
        if (!aiTag.hasNoTags())
        {
            dataManager.set(params.AIACTIONSTATESDW, aiTag.getInteger(AISTATE));
            for (Field f : IMoveConstants.class.getFields())
            {
                NonPersistantAI annot = f.getAnnotation(NonPersistantAI.class);
                if (annot != null)
                {
                    try
                    {
                        int state = f.getInt(null);
                        setPokemonAIState(state, false);
                    }
                    catch (IllegalArgumentException | IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            setHungerTime(aiTag.getInteger(HUNGER));
            int[] home = aiTag.getIntArray(HOME);
            if (home.length == 4)
            {
                setHome(home[0], home[1], home[2], home[3]);
            }
        }
        // Read Misc other
        if (!miscTag.hasNoTags())
        {
            this.setRNGValue(miscTag.getInteger(RNGVAL));
            this.uid = miscTag.getInteger(UID);
            this.wasShadow = miscTag.getBoolean(WASSHADOW);
        }
    }

    @Override
    public NBTTagCompound writePokemobData()
    {
        NBTTagCompound pokemobTag = new NBTTagCompound();
        pokemobTag.setInteger(VERSION, 1);
        // Write Ownership tag
        NBTTagCompound ownerShipTag = new NBTTagCompound();
        // This is still written for pokecubes to read from. Actual number is
        // stored in genes.
        ownerShipTag.setInteger(POKEDEXNB, this.getPokedexNb());
        ownerShipTag.setString(NICKNAME, getPokemonNickname());
        ownerShipTag.setBoolean(PLAYERS, isPlayerOwned());
        ownerShipTag.setString(TEAM, getPokemobTeam());
        if (getOriginalOwnerUUID() != null) ownerShipTag.setString(OT, getOriginalOwnerUUID().toString());
        if (getPokemonOwnerID() != null) ownerShipTag.setString(OWNER, getPokemonOwnerID().toString());
        ownerShipTag.setBoolean(ISTRADED, getPokemonAIState(TRADED));

        // Write stats tag
        NBTTagCompound statsTag = new NBTTagCompound();
        statsTag.setInteger(EXP, getExp());
        statsTag.setByte(STATUS, getStatus());
        statsTag.setInteger(HAPPY, bonusHappiness);

        // Write moves tag
        NBTTagCompound movesTag = new NBTTagCompound();
        movesTag.setInteger(MOVEINDEX, getMoveIndex());
        if (!getMoveStats().newMoves.isEmpty())
        {
            NBTTagList newMoves = new NBTTagList();
            for (String s : getMoveStats().newMoves)
            {
                newMoves.appendTag(new NBTTagString(s));
            }
            movesTag.setTag(NEWMOVES, newMoves);
        }
        movesTag.setInteger(COOLDOWN, getAttackCooldown());

        // Write Inventory tag
        NBTTagCompound inventoryTag = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        this.getPokemobInventory().setInventorySlotContents(1, this.getHeldItem());
        for (int i = 0; i < this.getPokemobInventory().getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.getPokemobInventory().getStackInSlot(i);
            if (CompatWrapper.isValid(itemstack))
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        inventoryTag.setTag(ITEMS, nbttaglist);

        // Write Breeding tag
        NBTTagCompound breedingTag = new NBTTagCompound();
        breedingTag.setByte(SEXE, getSexe());
        breedingTag.setInteger(SEXETIME, loveTimer);

        // Write visuals tag
        NBTTagCompound visualsTag = new NBTTagCompound();

        // This is still written for pokecubes to read from. Actual form is
        // stored in genes.
        visualsTag.setString(FORME, getPokedexEntry().getName());
        visualsTag.setInteger(SPECIALTAG, getSpecialInfo());
        visualsTag.setIntArray(FLAVOURSTAG, flavourAmounts);
        if (CompatWrapper.isValid(getPokecube()))
        {
            NBTTagCompound pokecubeTag = getPokecube().writeToNBT(new NBTTagCompound());
            visualsTag.setTag(POKECUBE, pokecubeTag);
        }
        // Misc AI
        NBTTagCompound aiTag = new NBTTagCompound();
        aiTag.setInteger(AISTATE, dataManager.get(params.AIACTIONSTATESDW));
        aiTag.setInteger(HUNGER, getHungerTime());
        if (getHome() != null) aiTag.setIntArray(HOME,
                new int[] { getHome().getX(), getHome().getY(), getHome().getZ(), (int) getHomeDistance() });

        // Misc other
        NBTTagCompound miscTag = new NBTTagCompound();
        miscTag.setInteger(RNGVAL, getRNGValue());
        miscTag.setInteger(UID, uid);
        miscTag.setBoolean(WASSHADOW, wasShadow);

        // Set tags to the pokemob tag.
        pokemobTag.setTag(OWNERSHIPTAG, ownerShipTag);
        pokemobTag.setTag(STATSTAG, statsTag);
        pokemobTag.setTag(MOVESTAG, movesTag);
        pokemobTag.setTag(INVENTORYTAG, inventoryTag);
        pokemobTag.setTag(BREEDINGTAG, breedingTag);
        pokemobTag.setTag(VISUALSTAG, visualsTag);
        pokemobTag.setTag(AITAG, aiTag);
        pokemobTag.setTag(MISCTAG, miscTag);
        return pokemobTag;
    }

}
