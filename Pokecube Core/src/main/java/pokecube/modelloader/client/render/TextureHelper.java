package pokecube.modelloader.client.render;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.core.client.render.model.IPartTexturer;

public class TextureHelper implements IPartTexturer
{
    private static class RandomState
    {
        double   chance   = 0.005;
        double[] arr;
        int      duration = 1;

        RandomState(String trigger, double[] arr)
        {
            this.arr = arr;
            String[] args = trigger.split(":");
            if (args.length > 1)
            {
                chance = Double.parseDouble(args[1]);
            }
            if (args.length > 2)
            {
                duration = Integer.parseInt(args[2]);
            }
        }
    }

    private static class SequenceState
    {
        double[] arr;
        boolean  shift = true;

        SequenceState(double[] arr)
        {
            this.arr = arr;
            for (double d : arr)
                if (d >= 1) shift = false;
        }
    }

    private static class TexState
    {
        // TODO Redo this to use new state system that specifies
        // logic/ai/combat.
        Map<LogicStates, double[]>   logic        = Maps.newHashMap();
        Map<GeneralStates, double[]> general      = Maps.newHashMap();
        Map<CombatStates, double[]>  combat       = Maps.newHashMap();
        Map<Integer, double[]>       infoStates   = Maps.newHashMap();
        Set<RandomState>             randomStates = Sets.newHashSet();
        SequenceState                sequence     = null;
        // TODO way to handle cheaning this up.
        Map<Integer, RandomState>    running      = Maps.newHashMap();
        Map<Integer, Integer>        setTimes     = Maps.newHashMap();

        void addState(String trigger, String[] diffs)
        {
            double[] arr = new double[diffs.length];
            for (int i = 0; i < arr.length; i++)
                arr[i] = Double.parseDouble(diffs[i].trim());

            boolean state = false;
            boolean info = false;
            try
            {
                int i = Integer.parseInt(trigger);
                infoStates.put(i, arr);
                info = true;
            }
            catch (Exception e)
            {
                state = parseState(trigger, arr);
            }
            if (info)
            {

            }
            if (state)
            {

            }
            else if (trigger.contains("random"))
            {
                randomStates.add(new RandomState(trigger, arr));
            }
            else if (trigger.equals("sequence") || trigger.equals("time"))
            {
                sequence = new SequenceState(arr);
            }
            else
            {
                new NullPointerException("No Template found for " + trigger).printStackTrace();
            }
        }

        private boolean parseState(String trigger, double[] arr)
        {
            for (LogicStates state : LogicStates.values())
            {
                if (state.name().toLowerCase(Locale.ENGLISH).equals(trigger))
                {
                    logic.put(state, arr);
                    logicNames.put(trigger, state);
                    return true;
                }
            }
            for (GeneralStates state : GeneralStates.values())
            {
                if (state.name().toLowerCase(Locale.ENGLISH).equals(trigger))
                {
                    general.put(state, arr);
                    generalNames.put(trigger, state);
                    return true;
                }
            }
            for (CombatStates state : CombatStates.values())
            {
                if (state.name().toLowerCase(Locale.ENGLISH).equals(trigger))
                {
                    combat.put(state, arr);
                    combatNames.put(trigger, state);
                    return true;
                }
            }
            return false;
        }

        boolean applyState(double[] toFill, IPokemob pokemob)
        {
            double dx = 0;
            double dy = 0;
            toFill[0] = dx;
            toFill[1] = dy;
            int info = pokemob.getSpecialInfo();
            Random random = new Random();
            if (infoStates.containsKey(info))
            {
                double[] arr = infoStates.get(info);
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }

            for (LogicStates state : logic.keySet())
            {
                // If logic state, or state is sleeping, and mob has sleep
                // status.
                if (pokemob.getLogicState(state)
                        || (state == LogicStates.SLEEPING && (pokemob.getStatus() & IMoveConstants.STATUS_SLP) != 0))
                {
                    double[] arr = logic.get(state);
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    return true;
                }
            }

            for (GeneralStates state : general.keySet())
            {
                // If logic state, or state is sleeping, and mob has sleep
                // status.
                if (pokemob.getGeneralState(state))
                {
                    double[] arr = logic.get(state);
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    return true;
                }
            }

            for (CombatStates state : combat.keySet())
            {
                // If logic state, or state is sleeping, and mob has sleep
                // status.
                if (pokemob.getCombatState(state))
                {
                    double[] arr = logic.get(state);
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    return true;
                }
            }

            if (running.containsKey(pokemob.getEntity().getEntityId()))
            {
                RandomState run = running.get(pokemob.getEntity().getEntityId());
                double[] arr = run.arr;
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                if (pokemob.getEntity().ticksExisted > setTimes.get(pokemob.getEntity().getEntityId()) + run.duration)
                {
                    running.remove(pokemob.getEntity().getEntityId());
                    setTimes.remove(pokemob.getEntity().getEntityId());
                }
                return true;
            }
            for (RandomState state : randomStates)
            {
                double[] arr = state.arr;
                if (random.nextFloat() < state.chance)
                {
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    running.put(pokemob.getEntity().getEntityId(), state);
                    setTimes.put(pokemob.getEntity().getEntityId(), pokemob.getEntity().ticksExisted);
                    return true;
                }
            }
            if (sequence != null && sequence.shift)
            {
                int tick = pokemob.getEntity().ticksExisted % (sequence.arr.length / 2);
                dx = sequence.arr[tick * 2];
                dy = sequence.arr[tick * 2 + 1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }
            return false;
        }

        String modifyTexture(IPokemob pokemob)
        {
            if (sequence != null && !sequence.shift)
            {
                int tick = pokemob.getEntity().ticksExisted % (sequence.arr.length / 2);
                int dx = (int) sequence.arr[tick * 2];
                return "" + dx;
            }
            return null;
        }
    }

    private final static Map<String, LogicStates>   logicNames   = Maps.newHashMap();
    private final static Map<String, GeneralStates> generalNames = Maps.newHashMap();
    private final static Map<String, CombatStates>  combatNames  = Maps.newHashMap();

    IPokemob                                        pokemob;
    PokedexEntry                                    entry;
    /** Map of part/material name -> texture name */
    Map<String, String>                             texNames     = Maps.newHashMap();
    /** Map of part/material name -> map of custom state -> texture name */
    Map<String, Map<String, String>>                texNames2    = Maps.newHashMap();
    ResourceLocation                                default_tex;
    String                                          default_path;

    Map<String, Boolean>                            smoothing    = Maps.newHashMap();

    boolean                                         default_flat = true;

    /** Map of part/material name -> resource location */
    Map<String, ResourceLocation>                   texMap       = Maps.newHashMap();

    Map<String, TexState>                           texStates    = Maps.newHashMap();

    Map<String, String>                             formeMap     = Maps.newHashMap();

    public TextureHelper(Node node)
    {
        if (node.getAttributes().getNamedItem("default") != null)
        {
            default_path = node.getAttributes().getNamedItem("default").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("smoothing") != null)
        {
            boolean flat = !node.getAttributes().getNamedItem("smoothing").getNodeValue().equalsIgnoreCase("smooth");
            default_flat = flat;
        }
        NodeList parts = node.getChildNodes();
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                String partTex = part.getAttributes().getNamedItem("tex").getNodeValue();
                addMapping(partName, partTex);
                if (part.getAttributes().getNamedItem("smoothing") != null)
                {
                    boolean flat = !node.getAttributes().getNamedItem("smoothing").getNodeValue()
                            .equalsIgnoreCase("smooth");
                    smoothing.put(partName, flat);
                }
            }
            else if (part.getNodeName().equals("animation"))
            {
                String partName = part.getAttributes().getNamedItem("part").getNodeValue();
                String trigger = part.getAttributes().getNamedItem("trigger").getNodeValue();
                String[] diffs = part.getAttributes().getNamedItem("diffs").getNodeValue().split(",");
                TexState states = texStates.get(partName);
                if (states == null) texStates.put(partName, states = new TexState());
                states.addState(trigger, diffs);
            }
            else if (part.getNodeName().equals("custom"))
            {
                String partName = part.getAttributes().getNamedItem("part").getNodeValue();
                String state = part.getAttributes().getNamedItem("state").getNodeValue();
                String partTex = part.getAttributes().getNamedItem("tex").getNodeValue();
                addCustomMapping(partName, state, partTex);
            }
            else if (part.getNodeName().equals("forme"))
            {
                String name = part.getAttributes().getNamedItem("name").getNodeValue();
                String tex = part.getAttributes().getNamedItem("tex").getNodeValue();
                formeMap.put(name.toLowerCase(java.util.Locale.ENGLISH).replace(" ", ""), tex);
            }
        }
    }

    @Override
    public void addCustomMapping(String part, String state, String tex)
    {
        Map<String, String> partMap = texNames2.get(part);
        if (partMap == null)
        {
            partMap = Maps.newHashMap();
            texNames2.put(part, partMap);
        }
        partMap.put(state, tex);
    }

    @Override
    public void addMapping(String part, String tex)
    {
        texNames.put(part, tex);
    }

    @Override
    public void applyTexture(String part)
    {
        if (bindPerState(part)) return;
        String texName = texNames.containsKey(part) ? texNames.get(part) : default_path;
        if (texName == null || texName.trim().isEmpty())
        {
            texNames.put(part, default_path);
        }
        ResourceLocation tex = getResource(texName);
        TexState state;
        String texMod;
        if ((state = texStates.get(part)) != null && (texMod = state.modifyTexture(pokemob)) != null)
            tex = getResource(tex.getResourcePath() + texMod);
        bindTex(tex);
    }

    @Override
    public void bindObject(Object thing)
    {
        pokemob = CapabilityPokemob.getPokemobFor((ICapabilityProvider) thing);
        entry = pokemob.getPokedexEntry();
        default_tex = getResource(default_path);
    }

    private boolean bindPerState(String part)
    {
        Map<String, String> partNames = texNames2.get(part);
        if (partNames == null)
        {
            PokedexEntry forme = entry;
            if (forme.getBaseForme() != null && forme != forme.getBaseForme())
            {
                String name = forme.getTrimmedName();
                String tex = formeMap.get(name);
                if (tex != null)
                {
                    TexState state;
                    String texMod;
                    if ((state = texStates.get(part)) != null && (texMod = state.modifyTexture(pokemob)) != null)
                        tex = tex + texMod;
                    bindTex(getResource(tex));
                    return true;
                }
            }
            return false;
        }
        for (String key : partNames.keySet())
        {
            if (isState(key))
            {
                String texKey = part + key;
                String tex;
                if ((tex = texNames.get(texKey)) != null)
                {
                }
                else
                {
                    tex = partNames.get(key);
                    texNames.put(texKey, tex);
                }
                TexState state;
                String texMod;
                if ((state = texStates.get(part)) != null && (texMod = state.modifyTexture(pokemob)) != null)
                    tex = tex + texMod;

                bindTex(getResource(tex));
                return true;
            }
        }
        return false;
    }

    private void bindTex(ResourceLocation tex)
    {
        tex = pokemob.modifyTexture(tex);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(tex);
    }

    private ResourceLocation getResource(String tex)
    {
        if (tex == null)
        {
            return new ResourceLocation(entry.getModId(), entry.getTrimmedName());
        }
        else if (tex.contains(":"))
        {
            return new ResourceLocation(tex);
        }
        else
        {
            return new ResourceLocation(entry.getModId(), tex);
        }
    }

    @Override
    public boolean hasMapping(String part)
    {
        return texNames.containsKey(part);
    }

    @Override
    public boolean isFlat(String part)
    {
        if (smoothing.containsKey(part)) { return smoothing.get(part); }
        return default_flat;
    }

    private boolean isState(String state)
    {
        int info = pokemob.getSpecialInfo();
        try
        {
            int i = Integer.parseInt(state);
            return i == info;
        }
        catch (Exception e)
        {
            LogicStates logic = logicNames.get(state);
            if (logic != null) return pokemob.getLogicState(logic)
                    || (logic == LogicStates.SLEEPING && (pokemob.getStatus() & IMoveConstants.STATUS_SLP) != 0);
            GeneralStates general = generalNames.get(state);
            if (general != null) return pokemob.getGeneralState(general);
            CombatStates combat = combatNames.get(state);
            if (combat != null) return pokemob.getCombatState(combat);
        }
        return false;
    }

    @Override
    public boolean shiftUVs(String part, double[] toFill)
    {
        TexState state;
        if ((state = texStates.get(part)) != null) { return state.applyState(toFill, pokemob); }
        return false;
    }

}
