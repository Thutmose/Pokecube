package pokecube.modelloader.client.custom.animation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.custom.LoadedModel;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;
import pokecube.modelloader.client.custom.PartInfo;
import pokecube.modelloader.client.tabula.components.Animation;
import thut.api.maths.Vector3;

public class AnimationLoader
{
    public static final String MODELPATH = "models/pokemobs/";
    public static boolean      loaded    = false;

    /** texture folder */
    public final static String TEXTUREPATH = "textures/entities/";

    static String                              file      = "";
    @SuppressWarnings("rawtypes")
    public static HashMap<String, LoadedModel> modelMaps = new HashMap<String, LoadedModel>();
    public static HashMap<String, Model>       models    = new HashMap<String, Model>();

    public static void clear()
    {
        models.clear();
        modelMaps.clear();
    }

    public static class Model
    {
        public ResourceLocation model;
        public ResourceLocation texture;
        public ResourceLocation animation;
        public String           name;

        public Model(ResourceLocation model, ResourceLocation texture, ResourceLocation animation, String name)
        {
            this.model = model;
            this.texture = texture;
            this.animation = animation;
            this.name = name;
        }

    }

    public static void load()
    {
        ModPokecubeML.proxy.registerRenderInformation();
        for (Model m : models.values())
        {
            parse(m);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void parse(Model model)
    {
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model.animation);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = res.getInputStream();
            Document doc = dBuilder.parse(stream);

            doc.getDocumentElement().normalize();

            NodeList modelList = doc.getElementsByTagName("model");

            int headDir = 2;
            int headAxis = 2;
            int headAxis2 = 1;
            float[] headCaps = { -180, 180 };
            float[] headCaps1 = { -30, 70 };
            Vector3 offset = null;
            Vector5 rotation = null;
            Vector3 scale = null;
            TextureHelper texturer = null;
            Set<String> headNames = Sets.newHashSet();
            Set<String> shear = Sets.newHashSet();
            Set<String> dye = Sets.newHashSet();
            Set<Animation> tblAnims = Sets.newHashSet();
            HashMap<String, String> mergedAnimations = Maps.newHashMap();
            for (int i = 0; i < modelList.getLength(); i++)
            {
                Node modelNode = modelList.item(i);
                String modelName = model.name;
                HashMap<String, PartInfo> parts = new HashMap<String, PartInfo>();
                HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<String, ArrayList<Vector5>>();
                NodeList partsList = modelNode.getChildNodes();
                for (int j = 0; j < partsList.getLength(); j++)
                {
                    Node part = partsList.item(j);
                    if (part.getNodeName().equals("metadata"))
                    {
                        try
                        {
                            offset = getOffset(part, offset);
                            scale = getScale(part, scale);
                            rotation = getRotation(part, rotation);
                            headDir = getHeadDir(part, headDir);
                            headAxis = getHeadAxis(part, 2);
                            headAxis2 = getHeadAxis2(part, 0);
                            addStrings("head", part, headNames);
                            addStrings("shear", part, shear);
                            addStrings("dye", part, dye);
                            setHeadCaps(part, headCaps, headCaps1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (part.getNodeName().equals("phase"))
                    {
                        Node phase = part.getAttributes().getNamedItem("name") == null
                                ? part.getAttributes().getNamedItem("type") : part.getAttributes().getNamedItem("name");
                        String phaseName = phase.getNodeValue();
                        boolean preset = false;
                        for (String s : AnimationRegistry.animations.keySet())
                        {
                            if (phaseName.equals(s))
                            {
                                tblAnims.add(AnimationRegistry.make(s, part.getAttributes(), null));
                                preset = true;
                            }
                        }
                        if (phaseName.equals("global"))
                        {
                            try
                            {
                                offset = getOffset(part, offset);
                                scale = getScale(part, scale);
                                rotation = getRotation(part, rotation);
                                headDir = getHeadDir(part, headDir);
                                headAxis = getHeadAxis(part, 2);
                                headAxis2 = getHeadAxis2(part, 0);
                                addStrings("head", part, headNames);
                                addStrings("shear", part, shear);
                                addStrings("dye", part, dye);
                                setHeadCaps(part, headCaps, headCaps1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (!preset)
                        {
                            Animation anim = AnimationBuilder.build(part, null);
                            if (anim != null)
                            {
                                Animation old = null;
                                for (Animation a : tblAnims)
                                {
                                    if (a.name.equals(anim.name))
                                    {
                                        old = a;
                                        break;
                                    }
                                }
                                if (old != null) AnimationBuilder.merge(anim, old);
                                else tblAnims.add(anim);
                            }
                        }
                    }
                    else if (part.getNodeName().equals("merges"))
                    {
                        String[] merges = part.getAttributes().getNamedItem("merge").getNodeValue().split("->");
                        mergedAnimations.put(merges[0], merges[1]);
                    }
                    else if (part.getNodeName().equals("customTex"))
                    {
                        texturer = new TextureHelper(part);
                    }
                }

                LoadedModel<?> loaded = modelMaps.get(modelName);
                if (loaded == null)
                {
                    loaded = new LoadedModel(parts, phaseList, model);
                }
                loaded.updateModel(parts, phaseList, model);
                loaded.offset.set(offset);
                loaded.scale.set(scale);
                loaded.rotations = rotation;
                loaded.headParts.addAll(headNames);
                loaded.shearableParts.addAll(shear);
                loaded.dyeableParts.addAll(dye);
                loaded.texturer = texturer;
                for (Animation anim : tblAnims)
                {
                    if (anim != null)
                    {
                        loaded.animations.put(anim.name, anim);
                    }
                    else
                    {
                        new NullPointerException("Why is there a null animation?").printStackTrace();
                    }
                }
                for (String s : mergedAnimations.keySet())
                {
                    String toName = mergedAnimations.get(s);
                    Animation to = null;
                    Animation from = null;
                    for (Animation anim : loaded.animations.values())
                    {
                        if (s.equals(anim.name))
                        {
                            from = anim;
                        }
                        if (toName.equals(anim.name))
                        {
                            to = anim;
                        }
                        if (to != null && from != null) break;
                    }
                    if (from != null && to == null)
                    {
                        to = new Animation();
                        to.name = toName;
                        to.identifier = toName;
                        to.loops = from.loops;
                        loaded.animations.put(toName, to);
                    }
                    if (to != null && from != null)
                    {
                        AnimationBuilder.merge(from, to);
                    }
                }

                if (scale != null) scale.freeVectorFromPool();

                if (headDir != 2) loaded.headDir = headDir;
                loaded.headAxis = headAxis;
                loaded.headAxis2 = headAxis2;
                loaded.headCaps = headCaps;
                loaded.headCaps1 = headCaps1;

                models.put(modelName, model);
                modelMaps.put(modelName, loaded);
            }

            stream.close();
        }
        catch (Exception e)
        {
            LoadedModel<?> loaded = modelMaps.get(model.name);
            if (loaded == null)
            {
                loaded = new LoadedModel(new HashMap<String, PartInfo>(), new HashMap<String, ArrayList<Vector5>>(),
                        model);
            }
            else
            {
                loaded.updateModel(new HashMap<String, PartInfo>(), new HashMap<String, ArrayList<Vector5>>(), model);
            }
            models.put(model.name, model);
            modelMaps.put(model.name, loaded);
            System.err.println("No Animation found for " + model.name + " " + model.model);
            e.printStackTrace();
        }

    }

    public static LoadedModel<?> getModel(String name)
    {
        Model model = models.get(name);
        if (model == null) { return null; }
        if (modelMaps.get(model.name) != null) return modelMaps.get(model.name);
        parse(model);
        if (modelMaps.get(model.name) != null) return modelMaps.get(model.name);
        return null;
    }

    public static Vector5 getRotation(Node node, Vector5 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("rotation") != null)
        {
            String rotation;
            String time = "0";
            Vector4 ro = new Vector4();
            int t = 0;
            String[] r;
            rotation = node.getAttributes().getNamedItem("rotation").getNodeValue();
            if (node.getAttributes().getNamedItem("time") != null)
                time = node.getAttributes().getNamedItem("time").getNodeValue();
            r = rotation.split(",");
            t = Integer.parseInt(time);
            ro.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()),
                    Float.parseFloat(r[3].trim()));
            return new Vector5(ro, t);
        }
        return default_;
    }

    public static Vector3 getOffset(Node node, Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        Vector3 vect = null;
        if (node.getAttributes().getNamedItem("offset") != null)
        {
            vect = Vector3.getNewVectorFromPool();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("offset").getNodeValue();
            r = shift.split(",");
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            return vect;
        }
        return default_;
    }

    public static Vector3 getScale(Node node, Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("scale") != null)
        {
            Vector3 vect = null;
            vect = Vector3.getNewVectorFromPool();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("scale").getNodeValue();
            r = shift.split(",");

            if (r.length == 3)
                vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            else vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()));
            return vect;
        }
        return default_;
    }

    public static int getHeadDir(Node node, int default_)
    {
        int ret = default_;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem("headDir") != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem("headDir").getNodeValue());
        }
        return ret;
    }

    public static int getHeadAxis(Node node, int default_)
    {
        int ret = default_;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem("headAxis") != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem("headAxis").getNodeValue());
        }
        return ret;
    }

    public static int getHeadAxis2(Node node, int default_)
    {
        int ret = default_;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem("headAxis2") != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem("headAxis2").getNodeValue());
        }
        return ret;
    }

    public static void addStrings(String key, Node node, Set<String> toAddTo)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem(key) != null)
        {
            String[] names = node.getAttributes().getNamedItem(key).getNodeValue().split(":");
            for (String s : names)
            {
                toAddTo.add(s);
            }
        }
    }

    public static void setHeadCaps(Node node, float[] toFill, float[] toFill1)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem("headCap") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap").getNodeValue();
            r = shift.split(",");
            toFill[0] = Float.parseFloat(r[0]);
            toFill[1] = Float.parseFloat(r[1]);
        }
        if (node.getAttributes().getNamedItem("headCap1") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap1").getNodeValue();
            r = shift.split(",");
            toFill1[0] = Float.parseFloat(r[0]);
            toFill1[1] = Float.parseFloat(r[1]);
        }
    }

    public static void setTextureDetails(Node node, PokedexEntry entry)
    {
        if (node.getAttributes() == null) return;
        String[] male = null, female = null;
        if (node.getAttributes().getNamedItem("male") != null)
        {
            String shift;
            shift = node.getAttributes().getNamedItem("male").getNodeValue();
            male = shift.split(",");
        }
        if (node.getAttributes().getNamedItem("female") != null)
        {
            String shift;
            shift = node.getAttributes().getNamedItem("female").getNodeValue();
            female = shift.split(",");
        }
        if (female == null && male != null)
        {
            female = male;
        }
        if (male != null)
        {
            entry.textureDetails[0] = male;
            entry.textureDetails[1] = female;
        }
    }

    public static boolean initModel(String s)
    {
        ResourceLocation model = null;
        String anim = s + ".xml";
        // System.out.println(anim);

        ResourceLocation texture = new ResourceLocation(s.replace(MODELPATH, TEXTUREPATH) + ".png");
        try
        {
            model = new ResourceLocation(s + ".x3d");
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
            res.getInputStream().close();
        }
        catch (IOException e1)
        {
            try
            {
                model = new ResourceLocation(s + ".b3d");
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
                res.getInputStream().close();
            }
            catch (IOException e2)
            {
                try
                {
                    model = new ResourceLocation(s + ".obj");
                    IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
                    res.getInputStream().close();
                }
                catch (IOException e3)
                {
                    // System.out.println("did not find "+s);
                    model = null;
                }
            }
        }
        try
        {
            if (model != null)
            {
                String[] args = s.split(":");
                String[] args2 = args[1].split("/");
                String name = args2[args2.length > 1 ? args2.length - 1 : 0];
                // System.out.println(name);
                if (Database.getEntry(name) != null)
                {
                    PokedexEntry entry = Database.getEntry(name);
                    ResourceLocation animation = new ResourceLocation(
                            anim.replace(entry.getName(), entry.getBaseName()));
                    models.put(name, new Model(model, texture, animation, Database.getEntry(name).getName()));
                    if (loaded) getModel(name);
                }
                else
                {
                    System.err.println("Attmpted to register a model for un-registered pokemob " + name);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return model != null;
    }
}
