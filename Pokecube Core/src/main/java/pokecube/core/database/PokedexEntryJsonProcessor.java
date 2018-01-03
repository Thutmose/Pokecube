package pokecube.core.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import jline.internal.InputStreamReader;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.utils.PokeType;

public class PokedexEntryJsonProcessor
{
    private static final Set<String> list = Sets.newHashSet();
    static
    {

        // list.add("baseForme");
        // list.add("childNb");
        // list.add("childNumbers");
        // list.add("evolutions");
        // list.add("evolvesBy");
        // list.add("evolvesFrom");
        // list.add("formeItems");
        // list.add("forms");
        // list.add("prey");
        // list.add("related");
        // list.add("male");

        list.add("abilities");
        list.add("abilitiesHidden");
        list.add("activeTimes");
        list.add("base");
        list.add("dummy");
        list.add("customSound");
        list.add("baseHappiness");
        list.add("baseName");
        list.add("baseXP");
        list.add("breeds");
        list.add("canSitShoulder");
        list.add("catchRate");
        list.add("colonyBuilder");
        list.add("defaultSpecial");
        list.add("drops");
        list.add("lootTable");
        list.add("dyeable");
        list.add("event");
        list.add("replacedEvent");
        list.add("evolutionMode");
        list.add("evs");
        list.add("female");
        list.add("food");
        list.add("foods");
        list.add("isGenderForme");
        list.add("hasMegaForm");
        list.add("hasShiny");
        list.add("hatedMaterial");
        list.add("height");
        list.add("isMega");
        list.add("ridable");
        list.add("held");
        list.add("heldTable");
        list.add("interactionLogic");
        list.add("isFemaleForme");
        list.add("isMaleForme");
        list.add("isShadowForme");
        list.add("isSocial");
        list.add("isStarter");
        list.add("isStationary");
        list.add("legendary");
        list.add("length");
        list.add("lvlUpMoves");
        list.add("evolutionMoves");
        list.add("mass");
        list.add("megaRules");
        list.add("mobType");
        list.add("modId");
        list.add("particleData");
        list.add("passengerOffsets");
        list.add("pokedexNb");
        list.add("possibleMoves");
        list.add("preferedHeight");
        list.add("sexeRatio");
        list.add("shadowForme");
        list.add("shouldDive");
        list.add("shouldFly");
        list.add("shouldSurf");
        list.add("sound");
        list.add("spawns");
        list.add("species");
        list.add("stats");
        list.add("textureDetails");
        list.add("texturePath");
        list.add("type1");
        list.add("type2");
        list.add("width");
        list.add("modelSize");
        list.add("name");
        list.add("trimmedName");
    }
    private static TypeAdapter<PokeType> typeAdaptor = new TypeAdapter<PokeType>()
                                                     {
                                                         @Override
                                                         public void write(JsonWriter out, PokeType value)
                                                                 throws IOException
                                                         {
                                                             out.value(value.name);
                                                         }

                                                         @Override
                                                         public PokeType read(JsonReader in) throws IOException
                                                         {
                                                             return PokeType.getType(in.nextString());
                                                         }
                                                     };

    private static ExclusionStrategy     strat       = new ExclusionStrategy()
                                                     {
                                                         @Override
                                                         public boolean shouldSkipField(FieldAttributes f)
                                                         {
                                                             return !list.contains(f.getName());
                                                         }

                                                         @Override
                                                         public boolean shouldSkipClass(Class<?> clazz)
                                                         {
                                                             return clazz == EntityPlayer.class;
                                                         }
                                                     };

    private static Gson                  gson        = new GsonBuilder()
            .registerTypeAdapter(PokeType.class, typeAdaptor).addSerializationExclusionStrategy(strat)
            .addDeserializationExclusionStrategy(strat).setPrettyPrinting().create();

    public static class PokedexEntries
    {
        protected List<PokedexEntry> entries = Lists.newArrayList();
    }

    static PokedexEntries entries;

    public static void loadEntries(InputStream stream)
    {
        entries = gson.fromJson(new InputStreamReader(stream), PokedexEntries.class);
    }
}
