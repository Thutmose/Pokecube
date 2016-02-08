package pokecube.modelloader.client.custom.animation;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.modelloader.client.custom.IPartTexturer;

public class TextureHelper implements IPartTexturer
{
    IPokemob                                 pokemob;
    Map<String, String>                      texNames          = Maps.newHashMap();
    String                                   default_tex;
    Map<String, Boolean>                     smoothing         = Maps.newHashMap();
    boolean                                  default_smoothing = true;
    Map<String, ResourceLocation>            texMap            = Maps.newHashMap();
    Map<String, TexState>                    texStates         = Maps.newHashMap();
    public final static Map<String, Integer> mappedStates      = Maps.newHashMap();

    public TextureHelper(Node node)
    {
        if (node.getAttributes().getNamedItem("default") != null)
        {
            default_tex = node.getAttributes().getNamedItem("default").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("smoothing") != null)
        {
            boolean flat = !node.getAttributes().getNamedItem("smoothing").getNodeValue().equalsIgnoreCase("smooth");
            default_smoothing = flat;
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
        }
    }

    @Override
    public void applyTexture(String part)
    {
        String tex = texNames.containsKey(part) ? texNames.get(part) : default_tex;
        tex = pokemob.modifyTexture(tex);
        ResourceLocation loc;
        if ((loc = texMap.get(tex)) != null)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(loc);
        }
        else
        {
            loc = new ResourceLocation(pokemob.getPokedexEntry().getModId(), tex);
            texMap.put(tex, loc);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(loc);
        }
    }

    @Override
    public void bindObject(Object thing)
    {
        pokemob = (IPokemob) thing;
    }

    @Override
    public boolean shiftUVs(String part, double[] toFill)
    {
        TexState state;
        if ((state = texStates.get(part)) != null) { return state.applyState(toFill, pokemob); }
        return false;
    }

    public static int getState(String trigger)
    {
        return getState(trigger, true);
    }

    static int getState(String trigger, boolean exception)
    {
        if (mappedStates.containsKey(trigger)) return mappedStates.get(trigger);
        try
        {
            Field f;
            int state = 0;
            String[] args = trigger.split("\\+");
            for (String s : args)
            {
                String test = s.trim().toUpperCase();
                if ((f = IMoveConstants.class.getDeclaredField(test)) != null)
                {
                    state |= f.getInt(null);
                }
            }
            return state;
        }
        catch (Exception e)
        {
            if (exception) e.printStackTrace();
        }
        return -1;
    }

    private static class TexState
    {
        Map<Integer, double[]>    aiStates     = Maps.newHashMap();
        Set<RandomState>          randomStates = Sets.newHashSet();
        SequenceState             sequence     = null;
        // TODO way to handle cheaning this up.
        Map<Integer, RandomState> running      = Maps.newHashMap();
        Map<Integer, Integer>     setTimes     = Maps.newHashMap();

        void addState(String trigger, String[] diffs)
        {
            double[] arr = new double[diffs.length];
            for (int i = 0; i < arr.length; i++)
                arr[i] = Double.parseDouble(diffs[i].trim());
            int state = getState(trigger, false);
            if (state > 0)
            {
                aiStates.put(state, arr);
            }
            else if (trigger.contains("random"))
            {
                randomStates.add(new RandomState(trigger, arr));
            }
            else if (trigger.equals("sequence"))
            {
                sequence = new SequenceState(arr);
            }
            else
            {
                new NullPointerException("No Template found for " + trigger).printStackTrace();
            }
        }

        boolean applyState(double[] toFill, IPokemob pokemob)
        {
            double dx = 0;
            double dy = 0;
            toFill[0] = dx;
            toFill[1] = dy;
            for (Integer i : aiStates.keySet())
            {
                if (pokemob.getPokemonAIState(i))
                {
                    double[] arr = aiStates.get(i);
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    return true;
                }
            }
            if (running.containsKey(((Entity) pokemob).getEntityId()))
            {
                RandomState run = running.get(((Entity) pokemob).getEntityId());
                double[] arr = run.arr;
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                if (((Entity) pokemob).ticksExisted > setTimes.get(((Entity) pokemob).getEntityId()) + run.duration)
                {
                    running.remove(((Entity) pokemob).getEntityId());
                    setTimes.remove(((Entity) pokemob).getEntityId());
                }
                return true;
            }
            else for (RandomState state : randomStates)
            {
                double[] arr = state.arr;
                if (Math.random() < state.chance)
                {
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    running.put(((Entity) pokemob).getEntityId(), state);
                    setTimes.put(((Entity) pokemob).getEntityId(), ((Entity) pokemob).ticksExisted);
                    return true;
                }
            }
            if (sequence != null)
            {
                int tick = ((Entity) pokemob).ticksExisted % (sequence.arr.length / 2);
                dx = sequence.arr[tick * 2];
                dy = sequence.arr[tick * 2 + 1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }
            return false;
        }
    }

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

        SequenceState(double[] arr)
        {
            this.arr = arr;
        }
    }

    @Override
    public boolean isFlat(String part)
    {
        if (smoothing.containsKey(part)) { return smoothing.get(part); }
        return default_smoothing;
    }

    @Override
    public void addMapping(String part, String tex)
    {
        texNames.put(part, tex);
    }

}
