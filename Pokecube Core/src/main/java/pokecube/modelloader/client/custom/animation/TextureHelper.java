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
    IPokemob                      pokemob;
    Map<String, String>           texNames  = Maps.newHashMap();
    Map<String, ResourceLocation> texMap    = Maps.newHashMap();
    Map<String, TexState>         texStates = Maps.newHashMap();

    public TextureHelper(Node node)
    {
        if (node.getAttributes().getNamedItem("default") != null)
            texNames.put("default", node.getAttributes().getNamedItem("default").getNodeValue());
        NodeList parts = node.getChildNodes();
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                String partTex = part.getAttributes().getNamedItem("tex").getNodeValue();
                texNames.put(partName, partTex);
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
        String tex = texNames.containsKey(part) ? texNames.get(part) : texNames.get("default");

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
    public void shiftUVs(String part, double[] toFill)
    {
        TexState state;
        if ((state = texStates.get(part)) != null)
        {
            state.applyState(toFill, pokemob);
        }
    }

    private static class TexState
    {
        final static Map<String, Integer> mappedStates = Maps.newHashMap();
        Map<Integer, double[]>            aiStates     = Maps.newHashMap();
        Set<RandomState>                  randomStates = Sets.newHashSet();
        RandomState                       running      = null;

        void addState(String trigger, String[] diffs)
        {
            double[] arr = new double[2];
            arr[0] = Double.parseDouble(diffs[0]);
            arr[1] = Double.parseDouble(diffs[1]);
            if (mappedStates.containsKey(trigger))
            {
                int state = mappedStates.get(trigger);
                aiStates.put(state, arr);
            }
            else if (trigger.contains("random"))
            {
                randomStates.add(new RandomState(trigger, arr));
            }
            else
            {
                String test = trigger.toUpperCase();
                try
                {
                    Field f;
                    if ((f = IMoveConstants.class.getDeclaredField(test)) != null)
                    {
                        int state = f.getInt(null);
                        mappedStates.put(trigger, state);
                        aiStates.put(state, arr);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        void applyState(double[] toFill, IPokemob pokemob)
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
                    return;
                }
            }
            if (running != null)
            {
                double[] arr = running.arr;
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                if(((Entity)pokemob).ticksExisted > running.set + running.duration)
                {
                    running = null;
                }
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
                    running = state;
                    state.set = ((Entity)pokemob).ticksExisted;
                    return;
                }
            }
        }
    }

    private static class RandomState
    {
        double   chance = 0.005;
        double[] arr;
        int      set;
        int      duration = 1;

        RandomState(String trigger, double[] arr)
        {
            this.arr = arr;
            String[] args = trigger.split(":");
            if(args.length>1)
            {
                chance = Double.parseDouble(args[1]);
            }
            if(args.length>2)
            {
                duration = Integer.parseInt(args[2]);
            }
        }
    }

}
