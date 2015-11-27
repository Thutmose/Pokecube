package pokecube.modelloader.client.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.client.custom.LoadedModel;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;
import pokecube.modelloader.client.custom.animation.AnimationLoader;
import pokecube.modelloader.client.custom.animation.ModelAnimation;
import pokecube.modelloader.client.tabula.animation.BasicFlapAnimation;
import pokecube.modelloader.client.tabula.animation.BiWalkAnimation;
import pokecube.modelloader.client.tabula.animation.QuadWalkAnimation;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.ModelJson;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModel;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModelParser;
import thut.api.maths.Vector3;

public class TabulaPackLoader extends AnimationLoader
{
    public static HashMap<PokedexEntry, TabulaModelSet> modelMap = new HashMap();

    public static boolean loadModel(String path)
    {
        ResourceLocation model = new ResourceLocation(path + ".tbl");
        String anim = path + ".xml";
        ResourceLocation texture = new ResourceLocation(path.replace(MODELPATH, TEXTUREPATH) + ".png");
        ResourceLocation extraData = new ResourceLocation(anim);

        String[] args = path.split(":");
        String[] args2 = args[1].split("/");
        String name = args2[args2.length > 1 ? args2.length - 1 : 0];
        PokedexEntry entry = Database.getEntry(name);

        if (modelMap.containsKey(entry)) return true;

        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);

            InputStream stream = res.getInputStream();
            ZipInputStream zip = new ZipInputStream(stream);
            Scanner scanner = new Scanner(zip);
            zip.getNextEntry();
            String json = scanner.nextLine();
            if (entry != null)
            {
                TabulaModelParser parser = new TabulaModelParser();
                res = Minecraft.getMinecraft().getResourceManager().getResource(texture);
                TabulaModel tbl = parser.parse(json, res.getInputStream());
                TabulaModelSet set = new TabulaModelSet(tbl, parser, extraData, entry);
                modelMap.put(entry, set);
            }
            scanner.close();
            return entry != null;
        }
        catch (IOException e)
        {

        }

        return false;
    }

    public static void postProcess()
    {
        for (PokedexEntry entry : modelMap.keySet())
        {
            TabulaModelSet set = modelMap.get(entry);
            if (!set.foundExtra)
            {
                PokedexEntry base = entry.baseForme;
                TabulaModelSet baseSet = modelMap.get(base);
                if (baseSet != null)
                {
                    set.rotation = baseSet.rotation;
                    set.scale.set(baseSet.scale);
                    set.shift.set(baseSet.shift);
                    set.headCap = baseSet.headCap;
                }
                else
                {
                    // new NullPointerException("Cannot find base forme for
                    // "+entry).printStackTrace();
                    System.err.println("Cannot find base forme for " + entry + " " + entry.baseForme);
                }
            }
        }
    }

    public static class TabulaModelSet
    {
        final PokedexEntry                entry;
        public final TabulaModel          model;
        public final TabulaModelParser    parser;
        public HashMap<String, Animation> loadedAnimations = Maps.newHashMap();
        public Vector3                    shift            = Vector3.getNewVectorFromPool();
        public Vector5                    rotation;
        public Vector3                    scale            = Vector3.getNewVectorFromPool();
        public float[]                    headCap          = { -180, 180 };
        public int                        headAxis         = 1;
        public boolean                    foundExtra       = false;

        public TabulaModelSet(TabulaModel model, TabulaModelParser parser, ResourceLocation extraData,
                PokedexEntry entry)
        {
            this.model = model;
            this.parser = parser;
            this.entry = entry;
            try
            {
                parse(extraData);
                foundExtra = true;
            }
            catch (Exception e)
            {
                String name = entry.getBaseName();
                String old = entry.getName();
                ResourceLocation test2 = new ResourceLocation(extraData.getResourceDomain(),
                        extraData.getResourcePath().replace(old, name));
                try
                {
                    parse(test2);
                    foundExtra = true;
                }
                catch (Exception e1)
                {
                    System.out.println("did not find " + test2 + " " + e1);
                }
            }
        }

        public void parse(ResourceLocation animation) throws Exception
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(animation);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = res.getInputStream();
            Document doc = dBuilder.parse(stream);

            doc.getDocumentElement().normalize();

            NodeList modelList = doc.getElementsByTagName("model");

            HashSet<String> hl = new HashSet();
            HashSet<String> hr = new HashSet();
            HashSet<String> fl = new HashSet();
            HashSet<String> fr = new HashSet();
            int quadwalkdur = 0;
            int biwalkdur = 0;
            int flapdur = 0;
            int flapaxis = 2;
            float walkAngle1 = 20;
            float walkAngle2 = 20;

            HashMap<String, ModelAnimation> loadedPresets = new HashMap();

            float[] headCaps = { -180, 180 };
            Vector3 offset = null;
            Vector5 rotation = null;
            Vector3 scale = null;
            for (int i = 0; i < modelList.getLength(); i++)
            {
                Node modelNode = modelList.item(i);
                NodeList partsList = modelNode.getChildNodes();
                for (int j = 0; j < partsList.getLength(); j++)
                {
                    Node part = partsList.item(j);
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
                                setHeadCaps(part, headCaps);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (phaseName.equals("biWalk"))
                        {
                            String[] lh = part.getAttributes().getNamedItem("leftLeg").getNodeValue().split(":");
                            String[] rh = part.getAttributes().getNamedItem("rightLeg").getNodeValue().split(":");
                            String[] lf = part.getAttributes().getNamedItem("leftArm").getNodeValue().split(":");
                            String[] rf = part.getAttributes().getNamedItem("rightArm").getNodeValue().split(":");

                            convertToIdents(lh);
                            convertToIdents(rh);
                            convertToIdents(lf);
                            convertToIdents(rf);

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
                        else if (phaseName.equals("quadWalk"))
                        {
                            String[] lh = part.getAttributes().getNamedItem("leftHind").getNodeValue().split(":");
                            String[] rh = part.getAttributes().getNamedItem("rightHind").getNodeValue().split(":");
                            String[] lf = part.getAttributes().getNamedItem("leftFront").getNodeValue().split(":");
                            String[] rf = part.getAttributes().getNamedItem("rightFront").getNodeValue().split(":");

                            convertToIdents(lh);
                            convertToIdents(rh);
                            convertToIdents(lf);
                            convertToIdents(rf);

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
                            if (part.getAttributes().getNamedItem("frontAngle") != null)
                            {
                                walkAngle2 = Float
                                        .parseFloat(part.getAttributes().getNamedItem("frontAngle").getNodeValue());
                            }
                            else quadwalkdur = Integer
                                    .parseInt(part.getAttributes().getNamedItem("duration").getNodeValue());
                        }
                        else if (phaseName.equals("flap"))
                        {
                            String[] lh = part.getAttributes().getNamedItem("leftWing").getNodeValue().split(":");
                            String[] rh = part.getAttributes().getNamedItem("rightWing").getNodeValue().split(":");

                            convertToIdents(lh);
                            convertToIdents(rh);

                            for (String s : lh)
                                hl.add(s);
                            for (String s : rh)
                                hr.add(s);

                            if (part.getAttributes().getNamedItem("angle") != null)
                            {
                                walkAngle1 = Float
                                        .parseFloat(part.getAttributes().getNamedItem("angle").getNodeValue());
                            }
                            if (part.getAttributes().getNamedItem("start") != null)
                            {
                                walkAngle2 = Float
                                        .parseFloat(part.getAttributes().getNamedItem("start").getNodeValue());
                            }
                            if (part.getAttributes().getNamedItem("axis") != null)
                            {
                                flapaxis = Integer.parseInt(part.getAttributes().getNamedItem("axis").getNodeValue());
                            }
                            flapdur = Integer.parseInt(part.getAttributes().getNamedItem("duration").getNodeValue());
                        }
                    }
                }
            }
            if (offset != null)
            {
                this.shift.set(offset);
            }
            if (scale != null)
            {
                this.scale.set(scale);
            }
            if (rotation != null)
            {
                this.rotation = rotation;
            }
            else
            {
                this.rotation = new Vector5();
            }
            this.headCap[0] = headCaps[0];
            this.headCap[1] = headCaps[1];

            if (biwalkdur != 0)
            {
                loadedAnimations.put("walking",
                        new BiWalkAnimation().init(hl, hr, fl, fr, biwalkdur, walkAngle1, walkAngle2));
            }
            if (quadwalkdur != 0)
            {
                loadedAnimations.put("walking",
                        new QuadWalkAnimation().init(hl, hr, fl, fr, quadwalkdur, walkAngle1, walkAngle2));
            }
            if (flapdur != 0)
            {
                loadedAnimations.put("flying",
                        new BasicFlapAnimation().init(hl, hr, flapdur, walkAngle1, walkAngle2, flapaxis));
            }
        }

        void convertToIdents(String[] names)
        {
            for (int i = 0; i < names.length; i++)
            {
                for (ModelJson json : parser.modelMap.values())
                {
                    if (json.nameMap.containsKey(names[i]))
                    {
                        Object o = json.nameMap.get(names[i]);
                        for (String ident : json.identifierMap.keySet())
                        {
                            if (json.identifierMap.get(ident) == o)
                            {
                                names[i] = ident;
                                break;
                            }
                        }
                        break;
                    }

                }
            }
        }
    }
}
