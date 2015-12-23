package pokecube.modelloader.client.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.client.custom.LoadedModel.Vector5;
import pokecube.modelloader.client.custom.animation.AnimationLoader;
import pokecube.modelloader.client.tabula.animation.AdvancedFlapAnimation;
import pokecube.modelloader.client.tabula.animation.BasicFlapAnimation;
import pokecube.modelloader.client.tabula.animation.BiWalkAnimation;
import pokecube.modelloader.client.tabula.animation.QuadWalkAnimation;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.CubeGroup;
import pokecube.modelloader.client.tabula.components.CubeInfo;
import pokecube.modelloader.client.tabula.components.ModelJson;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModel;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModelParser;
import thut.api.maths.Vector3;

public class TabulaPackLoader extends AnimationLoader
{
    public static HashMap<PokedexEntry, TabulaModelSet> modelMap = new HashMap<PokedexEntry, TabulaModelSet>();

    public static void clear()
    {
        AnimationLoader.clear();
        modelMap.clear();
    }

    public static boolean loadModel(String path)
    {
        ResourceLocation model = new ResourceLocation(path + ".tbl");
        String anim = path + ".xml";
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
                TabulaModel tbl = parser.parse(json);
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
                    System.err.println("Cannot find base forme for " + entry + " " + entry.baseForme);
                }
            }
            set.postInitAnimations();
            if (set.rotation == null)
            {
                set.rotation = new Vector5();
            }
            if (set.scale.isEmpty())
            {
                set.scale.set(1, 1, 1);
            }
            if (set.shift.isEmpty())
            {
                set.shift.set(0, -1.5, 0);
            }
        }
    }

    public static class TabulaModelSet
    {
        /** The pokemon associated with this model. */
        final PokedexEntry             entry;
        public final TabulaModel       model;
        public final TabulaModelParser parser;

        /** Animations to merge together, animation key is merged into animation
         * value. so key of idle and value of walk will merge the idle animation
         * into the walk animation. */
        private HashMap<String, String> mergedAnimations = Maps.newHashMap();

        /** The root part of the head. */
        private Set<String>               headRoots        = Sets.newHashSet();
        private String                    headRoot         = "";
        /** A set of identifiers of shearable parts. */
        public Set<String>                shearableIdents  = Sets.newHashSet();
        /** A set of identifiers of dyeable parts. */
        public Set<String>                dyeableIdents    = Sets.newHashSet();
        /** Animations loaded from the XML */
        public HashMap<String, Animation> loadedAnimations = Maps.newHashMap();
        /** Translation of the model */
        public Vector3                    shift            = Vector3.getNewVectorFromPool();
        /** Global rotation of the model */
        public Vector5                    rotation;
        /** Scale of the model */
        public Vector3                    scale            = Vector3.getNewVectorFromPool();
        /** Limits on the rotation of the head */
        public float[]                    headCap          = { -180, 180 };
        /** Which axis the head rotates around, 0,1 or for x, y */
        public int                        headAxis         = 1;
        /** Which direction the head rotates */
        public int                        headDir          = 1;
        /** Internal, used to determine if it should copy xml data from base
         * forme. */
        private boolean                   foundExtra       = false;

        public TabulaModelSet(TabulaModel model, TabulaModelParser parser, ResourceLocation extraData,
                PokedexEntry entry)
        {
            this.model = model;
            this.parser = parser;
            this.entry = entry;
            processMetadata();
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

        private void processMetadata()
        {
            for (CubeInfo cube : model.getCubes())
            {
                processMetadataForCubeInfo(cube);
            }
            for (CubeGroup group : model.getCubeGroups())
            {
                processMetadataForCubeGroup(group);
            }
        }

        private void processMetadataForCubeGroup(CubeGroup group)
        {
            for (CubeInfo cube : group.cubes)
            {
                processMetadataForCubeInfo(cube);
            }
            for (CubeGroup group1 : group.cubeGroups)
            {
                processMetadataForCubeGroup(group1);
            }
        }

        private void processMetadataForCubeInfo(CubeInfo cube)
        {
            if (headRoot.isEmpty() && cube.name.toLowerCase().contains("head") && cube.parentIdentifier != null)
            {
                headRoot = cube.identifier;
            }
            for (String s : cube.metadata)
            {
                if (s.equalsIgnoreCase("shearable"))
                {
                    shearableIdents.add(cube.identifier);
                }
                if (s.equalsIgnoreCase("dyeable"))
                {
                    dyeableIdents.add(cube.identifier);
                }
                if (s.equalsIgnoreCase("head"))
                {
                    headRoots.add(cube.identifier);
                }
            }
            for (CubeInfo cube1 : cube.children)
            {
                processMetadataForCubeInfo(cube1);
            }
        }

        private void postInitAnimations()
        {
            HashSet<String> toRemove = Sets.newHashSet();
            for (Animation anim : model.getAnimations())
            {
                for (String s : loadedAnimations.keySet())
                {
                    if (s.equals(anim.name))
                    {
                        Animation loaded = loadedAnimations.get(s);
                        merge(loaded, anim);
                        toRemove.add(s);
                    }
                }
            }
            for (String s : mergedAnimations.keySet())
            {
                String toName = mergedAnimations.get(s);
                Animation to = null;
                Animation from = null;
                for (Animation anim : model.getAnimations())
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
                if (to == null || from == null) for (Animation anim : loadedAnimations.values())
                {
                    if (from == null) if (s.equals(anim.name))
                    {
                        from = anim;
                    }
                    if (to == null) if (toName.equals(anim.name))
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
                    loadedAnimations.put(toName, to);
                }

                if (to != null && from != null)
                {
                    merge(from, to);
                }
            }
            for (String s : toRemove)
            {
                loadedAnimations.remove(s);
            }
            if (toRemove.size() > 0) System.out.println("Merged " + toRemove.size() + " Animations for " + entry);
        }

        /** Merges animation data from from to to.
         * 
         * @param from
         * @param to */
        private void merge(Animation from, Animation to)
        {
            for (String s1 : from.sets.keySet())
            {
                // Prioritize to, if to already has animations for that part,
                // skip it.
                if (!to.sets.containsKey(s1))
                {
                    to.sets.put(s1, from.sets.get(s1));
                }
            }
        }

        private void addAnimation(String key, Animation animation)
        {
            if (loadedAnimations.containsKey(key))
            {
                merge(animation, loadedAnimations.get(key));
            }
            else
            {
                loadedAnimations.put(key, animation);
            }
        }

        /** Returns true of the given identifier matches the part listed as the
         * root of the head.
         * 
         * @param identifier
         * @return */
        public boolean isHeadRoot(String identifier)
        {
            if(!headRoots.isEmpty())
            {
                return headRoots.contains(identifier);
            }
            return identifier.equals(headRoot);
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
                        String phaseName = part.getAttributes().getNamedItem("name").getNodeValue();

                        if (phaseName.equals("global"))
                        {
                            try
                            {
                                offset = getOffset(part);
                                scale = getScale(part);
                                rotation = getRotation(part);
                                headAxis = getHeadAxis(part, 1);
                                headDir = getHeadDir(part);
                                headDir = Math.min(1, headDir);
                                headDir = Math.max(-1, headDir);
                                setHeadCaps(part, headCaps);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (phaseName.equals("biWalk"))
                        {
                            addBiWalk(part.getAttributes());
                        }
                        else if (phaseName.equals("quadWalk"))
                        {
                            addQuadWalk(part.getAttributes());
                        }
                        else if (phaseName.equals("flap"))
                        {
                            addBasicFlap(part.getAttributes());
                        }
                        else if (phaseName.equals("advFlap"))
                        {
                            addAdvancedFlap(part.getAttributes());
                        }
                        else if(phaseName.equals("textures"))
                        {
                            setTextureDetails(part, entry);
                        }
                    }
                    else if (part.getNodeName().equals("merges"))
                    {
                        String[] merges = part.getAttributes().getNamedItem("merge").getNodeValue().split("->");
                        mergedAnimations.put(merges[0], merges[1]);
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
        }

        void convertToIdents(String[] names)
        {
            for (int i = 0; i < names.length; i++)
            {
                boolean found = false;
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
                                found = true;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (!found) names[i] = null;
            }
        }

        /** Reads and adds the quadwalk animation from the map.
         * 
         * @param map */
        void addQuadWalk(NamedNodeMap map)
        {
            HashSet<String> hl = new HashSet<String>();
            HashSet<String> hr = new HashSet<String>();
            HashSet<String> fl = new HashSet<String>();
            HashSet<String> fr = new HashSet<String>();
            int quadwalkdur = 0;
            float walkAngle1 = 20;
            float walkAngle2 = 20;
            String[] lh = map.getNamedItem("leftHind").getNodeValue().split(":");
            String[] rh = map.getNamedItem("rightHind").getNodeValue().split(":");
            String[] lf = map.getNamedItem("leftFront").getNodeValue().split(":");
            String[] rf = map.getNamedItem("rightFront").getNodeValue().split(":");

            convertToIdents(lh);
            convertToIdents(rh);
            convertToIdents(lf);
            convertToIdents(rf);

            for (String s : lh)
                if (s != null) hl.add(s);
            for (String s : rh)
                if (s != null) hr.add(s);
            for (String s : rf)
                if (s != null) fr.add(s);
            for (String s : lf)
                if (s != null) fl.add(s);
            if (map.getNamedItem("angle") != null)
            {
                walkAngle1 = Float.parseFloat(map.getNamedItem("angle").getNodeValue());
            }
            if (map.getNamedItem("frontAngle") != null)
            {
                walkAngle2 = Float.parseFloat(map.getNamedItem("frontAngle").getNodeValue());
            }
            else
            {
                walkAngle2 = walkAngle1;
            }
            quadwalkdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());

            addAnimation("walking", new QuadWalkAnimation().init(hl, hr, fl, fr, quadwalkdur, walkAngle1, walkAngle2));

        }

        /** Reads and adds the biwalk animation from the map.
         * 
         * @param map */
        void addBiWalk(NamedNodeMap map)
        {
            HashSet<String> hl = new HashSet<String>();
            HashSet<String> hr = new HashSet<String>();
            HashSet<String> fl = new HashSet<String>();
            HashSet<String> fr = new HashSet<String>();
            int biwalkdur = 0;
            float walkAngle1 = 20;
            float walkAngle2 = 20;
            String[] lh = map.getNamedItem("leftLeg").getNodeValue().split(":");
            String[] rh = map.getNamedItem("rightLeg").getNodeValue().split(":");
            String[] lf = map.getNamedItem("leftArm").getNodeValue().split(":");
            String[] rf = map.getNamedItem("rightArm").getNodeValue().split(":");

            convertToIdents(lh);
            convertToIdents(rh);
            convertToIdents(lf);
            convertToIdents(rf);

            for (String s : lh)
                if (s != null) hl.add(s);
            for (String s : rh)
                if (s != null) hr.add(s);
            for (String s : rf)
                if (s != null) fr.add(s);
            for (String s : lf)
                if (s != null) fl.add(s);
            biwalkdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());
            try
            {
                if (map.getNamedItem("legAngle") != null)
                {
                    walkAngle1 = Float.parseFloat(map.getNamedItem("legAngle").getNodeValue());
                }
                if (map.getNamedItem("armAngle") != null)
                {
                    walkAngle2 = Float.parseFloat(map.getNamedItem("armAngle").getNodeValue());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            addAnimation("walking", new BiWalkAnimation().init(hl, hr, fl, fr, biwalkdur, walkAngle1, walkAngle2));

        }

        /** Reads and adds the basic flap animation from the walk.
         * 
         * @param map */
        void addBasicFlap(NamedNodeMap map)
        {
            HashSet<String> hl = new HashSet<String>();
            HashSet<String> hr = new HashSet<String>();
            int flapdur = 0;
            int flapaxis = 2;
            float walkAngle1 = 20;
            float walkAngle2 = 20;

            String[] lh = map.getNamedItem("leftWing").getNodeValue().split(":");
            String[] rh = map.getNamedItem("rightWing").getNodeValue().split(":");

            convertToIdents(lh);
            convertToIdents(rh);

            for (String s : lh)
                if (s != null) hl.add(s);
            for (String s : rh)
                if (s != null) hr.add(s);

            if (map.getNamedItem("angle") != null)
            {
                walkAngle1 = Float.parseFloat(map.getNamedItem("angle").getNodeValue());
            }
            if (map.getNamedItem("start") != null)
            {
                walkAngle2 = Float.parseFloat(map.getNamedItem("start").getNodeValue());
            }
            if (map.getNamedItem("axis") != null)
            {
                flapaxis = Integer.parseInt(map.getNamedItem("axis").getNodeValue());
            }
            flapdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());

            addAnimation("flying", new BasicFlapAnimation().init(hl, hr, flapdur, walkAngle1, walkAngle2, flapaxis));

        }

        /** Reads and adds the basic flap animation from the walk.
         * 
         * @param map */
        void addAdvancedFlap(NamedNodeMap map)
        {
            int flapdur = 0;
            float walkAngle2 = 20;
            AdvancedFlapAnimation anim = new AdvancedFlapAnimation();

            flapdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());
            // Can have up to 255 wing segments, more than this would be silly.
            for (int i = 1; i <= 255; i++)
            {
                if (map.getNamedItem("leftWing" + i) == null) break;

                int flapaxis = 2;
                float[] walkAngle1 = { 20, 20 };
                HashSet<String> hl = new HashSet<String>();
                HashSet<String> hr = new HashSet<String>();
                String[] lh = map.getNamedItem("leftWing" + i).getNodeValue().split(":");
                String[] rh = map.getNamedItem("rightWing" + i).getNodeValue().split(":");
                convertToIdents(lh);
                convertToIdents(rh);

                for (String s : lh)
                    if (s != null) hl.add(s);
                for (String s : rh)
                    if (s != null) hr.add(s);

                if (map.getNamedItem("angle" + i) != null)
                {
                    String[] args = map.getNamedItem("angle" + i).getNodeValue().split(",");
                    walkAngle1[0] = Float.parseFloat(args[0]);
                    walkAngle1[1] = Float.parseFloat(args[1]);
                }
                if (map.getNamedItem("start" + i) != null)
                {
                    walkAngle2 = Float.parseFloat(map.getNamedItem("start" + i).getNodeValue());
                }
                if (map.getNamedItem("axis" + i) != null)
                {
                    flapaxis = Integer.parseInt(map.getNamedItem("axis" + i).getNodeValue());
                }
                anim.init(hl, hr, flapdur, walkAngle1, walkAngle2, flapaxis, i > 1);
            }
            addAnimation("flying", anim);

        }
    }
}
