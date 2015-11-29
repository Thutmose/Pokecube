package pokecube.modelloader.client.custom.animation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.LoadedModel;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;
import pokecube.modelloader.client.custom.PartInfo;
import thut.api.maths.Vector3;

public class AnimationLoader
{
    public static final String MODELPATH = "models/pokemobs/";

    /** texture folder */
    public final static String TEXTUREPATH = "textures/entities/";

    static String                              file       = "";
    @SuppressWarnings("rawtypes")
    public static HashMap<String, LoadedModel> modelMaps  = new HashMap<String, LoadedModel>();
    public static HashMap<String, Model>       models     = new HashMap<String, Model>();

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

            HashSet<String> hl = new HashSet<String>();
            HashSet<String> hr = new HashSet<String>();
            HashSet<String> fl = new HashSet<String>();
            HashSet<String> fr = new HashSet<String>();
            int headDir = 2;
            int headAxis = 2;
            float[] headCaps = { -180, 180 };
            int quadwalkdur = 0;
            int biwalkdur = 0;
            float walkAngle1 = 20;
            float walkAngle2 = 20;
            Vector3 offset = null;
            Vector5 rotation = null;
            Vector3 scale = null;
            ArrayList<String> names = new ArrayList<String>();
            HashMap<String, ModelAnimation> loadedPresets = new HashMap<String, ModelAnimation>();
            for (int i = 0; i < modelList.getLength(); i++)
            {
                Node modelNode = modelList.item(i);
                String modelName = model.name;// modelNode.getAttributes().getNamedItem("name").getNodeValue();
                HashMap<String, PartInfo> parts = new HashMap<String, PartInfo>();
                HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<String, ArrayList<Vector5>>();

                names.add(modelNode.getAttributes().getNamedItem("name").getNodeValue());
                NodeList partsList = modelNode.getChildNodes();
                for (int j = 0; j < partsList.getLength(); j++)
                {
                    Node part = partsList.item(j);
                    if (part.getNodeName().equals("part"))
                    {
                        parts.put(part.getAttributes().getNamedItem("name").getNodeValue(), getPart(part));

                    }
                    if (part.getNodeName().equals("phase"))
                    {
                        ArrayList<Vector5> phase = new ArrayList<LoadedModel.Vector5>();
                        String phaseName = part.getAttributes().getNamedItem("name").getNodeValue();

                        if (phaseName.equals("global"))
                        {
                            try
                            {
                                offset = getOffset(part);
                                scale = getScale(part);
                                rotation = getRotation(part);
                                headDir = getHeadDir(part);
                                headAxis = getHeadAxis(part);
                                setHeadCaps(part, headCaps);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (phaseName.equals("quadWalk"))
                        {
                            String[] lh = part.getAttributes().getNamedItem("leftHind").getNodeValue().split(":");
                            String[] rh = part.getAttributes().getNamedItem("rightHind").getNodeValue().split(":");
                            String[] lf = part.getAttributes().getNamedItem("leftFront").getNodeValue().split(":");
                            String[] rf = part.getAttributes().getNamedItem("rightFront").getNodeValue().split(":");
                            for (String s : lh)
                                hl.add(s);
                            for (String s : rh)
                                hr.add(s);
                            for (String s : rf)
                                fr.add(s);
                            for (String s : lf)
                                fl.add(s);
                            if (part.getAttributes().getNamedItem("angle") != null)
                            {
                                walkAngle1 = Float
                                        .parseFloat(part.getAttributes().getNamedItem("angle").getNodeValue());
                            }
                            quadwalkdur = Integer
                                    .parseInt(part.getAttributes().getNamedItem("duration").getNodeValue());
                        }
                        else if (phaseName.equals("biWalk"))
                        {
                            String[] lh = part.getAttributes().getNamedItem("leftLeg").getNodeValue().split(":");
                            String[] rh = part.getAttributes().getNamedItem("rightLeg").getNodeValue().split(":");
                            String[] lf = part.getAttributes().getNamedItem("leftArm").getNodeValue().split(":");
                            String[] rf = part.getAttributes().getNamedItem("rightArm").getNodeValue().split(":");
                            for (String s : lh)
                                hl.add(s);
                            for (String s : rh)
                                hr.add(s);
                            for (String s : rf)
                                fr.add(s);
                            for (String s : lf)
                                fl.add(s);
                            biwalkdur = Integer.parseInt(part.getAttributes().getNamedItem("duration").getNodeValue());
                            try
                            {
                                if (part.getAttributes().getNamedItem("legAngle") != null)
                                {
                                    walkAngle1 = Float
                                            .parseFloat(part.getAttributes().getNamedItem("legAngle").getNodeValue());
                                }
                                if (part.getAttributes().getNamedItem("armAngle") != null)
                                {
                                    walkAngle2 = Float
                                            .parseFloat(part.getAttributes().getNamedItem("armAngle").getNodeValue());
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            addVectors(part.getChildNodes(), phase);
                            phaseList.put(phaseName, phase);
                        }
                    }
                }

                LoadedModel<?> loaded = modelMaps.get(modelName);
                if (loaded == null)
                {
                    loaded = new LoadedModel(parts, phaseList, model);
                }
                if (quadwalkdur > 0)
                {
                    HashSet<IExtendedModelPart> modelParts = loaded.getAllParts();
                    HashSet<IExtendedModelPart> phl = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> phr = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> pfl = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> pfr = new HashSet<IExtendedModelPart>();

                    for (IExtendedModelPart p : modelParts)
                    {
                        if (hl.contains(p.getName()))
                        {
                            phl.add(p);
                        }
                        if (hr.contains(p.getName()))
                        {
                            phr.add(p);
                        }
                        if (fl.contains(p.getName()))
                        {
                            pfl.add(p);
                        }
                        if (fr.contains(p.getName()))
                        {
                            pfr.add(p);
                        }
                    }

                    AnimationQuadrupedWalk w = new AnimationQuadrupedWalk();
                    w.maxAngle = walkAngle1;
                    w.initAnimation(pfl, pfr, phl, phr, quadwalkdur);
                    loadedPresets.put("walking", w);
                }
                if (biwalkdur > 0)
                {
                    HashSet<IExtendedModelPart> modelParts = loaded.getAllParts();
                    HashSet<IExtendedModelPart> phl = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> phr = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> pfl = new HashSet<IExtendedModelPart>();
                    HashSet<IExtendedModelPart> pfr = new HashSet<IExtendedModelPart>();

                    for (IExtendedModelPart p : modelParts)
                    {
                        if (hl.contains(p.getName()))
                        {
                            phl.add(p);
                        }
                        if (hr.contains(p.getName()))
                        {
                            phr.add(p);
                        }
                        if (fl.contains(p.getName()))
                        {
                            pfl.add(p);
                        }
                        if (fr.contains(p.getName()))
                        {
                            pfr.add(p);
                        }
                    }
                    AnimationBipedWalk w = new AnimationBipedWalk();
                    w.angleArms = walkAngle2;
                    w.angleLegs = walkAngle1;
                    w.initAnimation(phl, phr, pfr, pfl, biwalkdur);
                    loadedPresets.put("walking", w);
                }
                loaded.updateModel(parts, phaseList, model);
                loaded.offset.set(offset);
                loaded.scale.set(scale);
                loaded.rotations = rotation;

                for (String s : loadedPresets.keySet())
                {
                    ModelAnimation m = loadedPresets.get(s);
                    ModelAnimation old = (ModelAnimation) loaded.phaseMap.get(s);
                    if (old == null || m.getClass().isInstance(m))
                    {
                        loaded.phaseMap.put(s, m);
                    }
                    else
                    {
                        for (String s1 : old.animations.keySet())
                        {
                            // if (!m.animations.containsKey(s))
                            m.animations.put(s, old.animations.get(s1));
                        }
                    }
                }

                if (scale != null) scale.freeVectorFromPool();

                if (headDir != 2) loaded.headDir = headDir;
                loaded.headAxis = headAxis;
                loaded.headCaps = headCaps;

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
        }

    }

    public static LoadedModel<?> getModel(String name)
    {
        Model model = models.get(name);
        if (model == null)
        {
            return null;
        }
        if (modelMaps.get(model.name) != null) return modelMaps.get(model.name);
        parse(model);
        if (modelMaps.get(model.name) != null) return modelMaps.get(model.name);

        System.err.println("Model not found, finding a random one instead");
        for (LoadedModel<?> m : modelMaps.values())
        {
            if (m != null) return m;
        }
        System.err.println("no models found");
        return null;
    }

    static PartInfo getPart(Node node)
    {
        if (node.getAttributes().getNamedItem("name") == null) { return null; }

        String name = node.getAttributes().getNamedItem("name").getNodeValue();
        PartInfo ret = new PartInfo(name);

        NodeList children = node.getChildNodes();

        HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<String, ArrayList<Vector5>>();
        HashMap<String, PartInfo> partsList = new HashMap<String, PartInfo>();

        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeName().equals("phase"))
            {
                String phaseName = child.getAttributes().getNamedItem("name").getNodeValue();
                NodeList phases = child.getChildNodes();

                ArrayList<Vector5> vectors = new ArrayList<Vector5>();
                addVectors(phases, vectors);
                phaseList.put(phaseName, vectors);

            }
            if (child.getNodeName().equals("part"))
            {
                String partName = child.getAttributes().getNamedItem("name").getNodeValue();
                partsList.put(partName, getPart(child));
            }
        }

        ret.children = partsList;
        ret.setPhaseInfo(phaseList);

        return ret;
    }

    static void addVectors(NodeList nodes, ArrayList<Vector5> list)
    {

        for (int j = 0; j < nodes.getLength(); j++)
        {
            Node node = nodes.item(j);
            try
            {
                Vector5 vect = getRotation(node);
                if (vect != null) list.add(vect);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Vector5 getRotation(Node node)
    {
        Vector5 vect = null;
        if (node.getAttributes() == null) return vect;
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
            vect = new Vector5(ro, t);
        }
        return vect;
    }

    public static Vector3 getOffset(Node node)
    {
        Vector3 vect = null;
        if (node.getAttributes() == null) return vect;
        if (node.getAttributes().getNamedItem("offset") != null)
        {
            vect = Vector3.getNewVectorFromPool();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("offset").getNodeValue();
            r = shift.split(",");
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
        }
        return vect;
    }

    public static Vector3 getScale(Node node)
    {
        Vector3 vect = null;
        if (node.getAttributes() == null) return vect;
        if (node.getAttributes().getNamedItem("scale") != null)
        {
            vect = Vector3.getNewVectorFromPool();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("scale").getNodeValue();
            r = shift.split(",");

            if (r.length == 3)
                vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            else vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()));
        }
        return vect;
    }

    public static int getHeadDir(Node node)
    {
        int ret = 2;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem("headDir") != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem("headDir").getNodeValue());
        }
        return ret;
    }

    static int getHeadAxis(Node node)
    {
        int ret = 2;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem("headAxis") != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem("headAxis").getNodeValue());
        }
        return ret;
    }

    public static void setHeadCaps(Node node, float[] toFill)
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
//            System.out.println("did not find "+s);
            model = null;
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
                    System.out.println("Registerd an x3d model for "+name);
//                    getModel(name);
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
