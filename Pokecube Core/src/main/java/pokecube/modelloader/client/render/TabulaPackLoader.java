package pokecube.modelloader.client.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IShearable;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.modelloader.IMobProvider;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationBuilder;
import thut.core.client.render.animation.AnimationRandomizer;
import thut.core.client.render.animation.AnimationRegistry;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.CubeGroup;
import thut.core.client.render.tabula.components.CubeInfo;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.model.tabula.TabulaModel;
import thut.core.client.render.tabula.model.tabula.TabulaModelParser;

public class TabulaPackLoader extends AnimationLoader
{
    public static class TabulaModelSet implements IPartRenamer, IAnimationChanger
    {
        /** The pokemon associated with this model. */
        final PokedexEntry                      entry;
        public final TabulaModel                model;
        public final TabulaModelParser          parser;
        public IPartTexturer                    texturer         = null;
        public AnimationRandomizer              animator         = null;

        /** Animations to merge together, animation key is merged into animation
         * value. so key of idle and value of walk will merge the idle animation
         * into the walk animation. */
        private HashMap<String, String>         mergedAnimations = Maps.newHashMap();

        /** The root part of the head. */
        /** A set of identifiers of shearable parts. */
        public Set<String>                      shearableIdents  = Sets.newHashSet();
        /** A set of identifiers of dyeable parts. */
        public Set<String>                      dyeableIdents    = Sets.newHashSet();
        /** Animations loaded from the XML */
        public HashMap<String, List<Animation>> loadedAnimations = Maps.newHashMap();
        /** Translation of the model */
        public Vector3                          shift            = Vector3.getNewVector();
        /** Global rotation of the model */
        public Vector5                          rotation;
        /** Scale of the model */
        public Vector3                          scale            = Vector3.getNewVector();
        private boolean                         foundExtra       = false;

        // These get copied into the headInfo for the model.
        int                                     headAxis         = 1;
        int                                     headDir          = 1;
        float[]                                 headCap          = { -180, 180 };
        float[]                                 headCap1         = { -30, 30 };

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
                ResourceLocation test2 = new ResourceLocation(extraData.getResourceDomain(), extraData.getResourcePath()
                        .replace(old.toLowerCase(Locale.ENGLISH), name.toLowerCase(Locale.ENGLISH)));
                try
                {
                    parse(test2);
                    foundExtra = true;
                }
                catch (Exception e1)
                {
                    // Not really important for tabula models, they can have
                    // animations built in.
                }
            }
            if (model.getHeadParts().isEmpty())
            {
                String[] defaultHeads = { "head", "Head" };
                convertToIdents(defaultHeads);
                if (defaultHeads[0] != null) model.getHeadParts().add(defaultHeads[0]);
                else if (defaultHeads[1] != null) model.getHeadParts().add(defaultHeads[1]);
            }
            model.getHeadInfo().yawCapMin = headCap[0];
            model.getHeadInfo().yawCapMax = headCap[1];
            model.getHeadInfo().yawDirection = headDir;
            model.getHeadInfo().pitchCapMin = headCap1[0];
            model.getHeadInfo().pitchCapMax = headCap1[1];
            model.getHeadInfo().pitchAxis = 0;
            model.getHeadInfo().yawAxis = 1;
        }

        public TabulaModelSet(TabulaModelSet from, ResourceLocation extraData, PokedexEntry entry)
        {
            this(from.model, from.parser, extraData, entry);
        }

        private void addAnimation(Animation animation)
        {
            String key = animation.name;
            List<Animation> anims = loadedAnimations.get(key);
            if (anims == null) loadedAnimations.put(key, anims = Lists.newArrayList());
            anims.add(animation);
        }

        @Override
        public void convertToIdents(String[] names)
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

        @Override
        public int getColourForPart(String partIdentifier, Entity entity, int default_)
        {
            if (dyeableIdents.contains(partIdentifier))
            {
                int rgba = 0xFF000000;
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                rgba += EnumDyeColor.byDyeDamage(pokemob.getSpecialInfo()).getColorValue();
                return rgba;
            }
            return default_;
        }

        @Override
        public boolean isPartHidden(String part, Entity entity, boolean default_)
        {
            if (shearableIdents.contains(part))
            {
                boolean shearable = ((IShearable) entity).isShearable(new ItemStack(Items.SHEARS),
                        entity.getEntityWorld(), entity.getPosition());
                return !shearable;
            }
            return default_;
        }

        @Override
        public String modifyAnimation(EntityLiving entity, float partialTicks, String phase)
        {
            if (animator != null) return animator.modifyAnimation(entity, partialTicks, phase);
            return phase;
        }

        public void parse(ResourceLocation animation) throws Exception
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(animation);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(res.getInputStream());
            res.close();
            doc.getDocumentElement().normalize();

            NodeList modelList = doc.getElementsByTagName("model");

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
                        Node phase = part.getAttributes().getNamedItem("name") == null
                                ? part.getAttributes().getNamedItem("type") : part.getAttributes().getNamedItem("name");
                        String phaseName = phase.getNodeValue();
                        // Look for preset animations to load in
                        boolean preset = false;

                        for (String s : AnimationRegistry.animations.keySet())
                        {
                            if (phaseName.equals(s))
                            {
                                addAnimation(AnimationRegistry.make(s, part.getAttributes(), this));
                                preset = true;
                            }
                        }
                        // Global offset and rotation settings (legacy support
                        // for head stuff as well)
                        if (phaseName.equals("global"))
                        {
                            try
                            {
                                offset = getOffset(part, offset);
                                scale = getScale(part, scale);
                                rotation = getRotation(part, rotation);
                                headAxis = getIntValue(part, "headAxis", headAxis);
                                headDir = getIntValue(part, "headDir", headDir);
                                headDir = Math.min(1, headDir);
                                headDir = Math.max(-1, headDir);

                                setHeadCaps(part, headCap, headCap1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        // Texture Animation info
                        else if (phaseName.equals("textures"))
                        {
                            setTextureDetails(part, entry);
                        }
                        // Try to load in an animation.
                        else if (!preset)
                        {
                            Animation anim = AnimationBuilder.build(part, this);
                            if (anim != null)
                            {
                                addAnimation(anim);
                            }
                        }
                    }
                    // Read in Animation Merges
                    else if (part.getNodeName().equals("merges"))
                    {
                        String[] merges = part.getAttributes().getNamedItem("merge").getNodeValue().split("->");
                        mergedAnimations.put(merges[0], merges[1]);
                    }
                    else if (part.getNodeName().equals("customTex"))
                    {
                        texturer = new TextureHelper(part);
                    }
                    else if (part.getNodeName().equals("subAnims"))
                    {
                        animator = new AnimationRandomizer(part);
                    }
                    else if (part.getNodeName().equals("metadata"))
                    {
                        try
                        {
                            offset = getOffset(part, offset);
                            scale = getScale(part, scale);
                            rotation = getRotation(part, rotation);
                            headDir = getIntValue(part, "headDir", headDir);
                            headAxis = getIntValue(part, "headAxis", 1);
                            Set<String> toAdd = Sets.newHashSet();
                            addStrings("head", part, toAdd);
                            ArrayList<String> temp = new ArrayList<String>(toAdd);
                            toAdd.clear();
                            String[] names = temp.toArray(new String[0]);
                            this.convertToIdents(names);
                            for (String s : names)
                                model.getHeadParts().add(s);
                            temp.clear();
                            addStrings("shear", part, toAdd);
                            temp.addAll(toAdd);
                            toAdd.clear();
                            names = temp.toArray(new String[0]);
                            this.convertToIdents(names);
                            for (String s : names)
                                shearableIdents.add(s);
                            addStrings("dye", part, toAdd);
                            temp.addAll(toAdd);
                            names = temp.toArray(new String[0]);
                            this.convertToIdents(names);
                            for (String s : names)
                                dyeableIdents.add(s);
                            setHeadCaps(part, headCap, headCap1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
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
        }

        private void postInitAnimations()
        {

            for (String from : mergedAnimations.keySet())
            {
                if (!loadedAnimations.containsKey(from)) continue;
                String to = mergedAnimations.get(from);
                if (!loadedAnimations.containsKey(to)) continue;
                List<Animation> fromSet = Lists.newArrayList();
                List<Animation> toSet = loadedAnimations.get(to);
                for (Animation anim : loadedAnimations.get(from))
                {
                    Animation newAnim = new Animation();
                    newAnim.identifier = anim.identifier;
                    newAnim.name = to;
                    newAnim.loops = anim.loops;
                    newAnim.priority = 20;
                    newAnim.length = -1;
                    for (String s : anim.sets.keySet())
                    {
                        newAnim.sets.put(s, Lists.newArrayList(anim.sets.get(s)));
                    }
                    fromSet.add(newAnim);
                }
                toSet.addAll(fromSet);
            }
            for (List<Animation> anims : loadedAnimations.values())
            {
                AnimationBuilder.processAnimations(anims);
            }

            // TODO cleanup and animations from loaded animations here.
            if (animator != null)
            {
                Set<Animation> anims = Sets.newHashSet();
                anims.addAll(model.getAnimations());
                animator.init(anims);
            }
            model.getHeadInfo().yawCapMin = headCap[0];
            model.getHeadInfo().yawCapMax = headCap[1];
            model.getHeadInfo().yawDirection = headDir;
            model.getHeadInfo().pitchCapMin = headCap1[0];
            model.getHeadInfo().pitchCapMax = headCap1[1];
            model.getHeadInfo().pitchAxis = 0;
            model.getHeadInfo().yawAxis = 1;
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
                    model.getHeadParts().add(cube.identifier);
                }
            }
            for (CubeInfo cube1 : cube.children)
            {
                processMetadataForCubeInfo(cube1);
            }
        }
    }

    public static HashMap<PokedexEntry, TabulaModelSet> modelMap = new HashMap<PokedexEntry, TabulaModelSet>();

    public static void clear()
    {
        AnimationLoader.clear();
        modelMap.clear();
    }

    public static void remove(PokedexEntry entry)
    {
        AnimationLoader.remove(entry);
        modelMap.remove(entry);
    }

    public static boolean loadModel(IMobProvider provider, String path, HashSet<String> toReload)
    {
        ResourceLocation model = new ResourceLocation(path + ".tbl");
        String anim = path + ".xml";
        ResourceLocation extraData = new ResourceLocation(anim);

        String[] args = path.split(":");
        String[] args2 = args[1].split("/");
        String name = args2[args2.length > 1 ? args2.length - 1 : 0];
        PokedexEntry entry = Database.getEntry(name);
        try
        {
            if (entry != null)
            {
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
                ZipInputStream zip = new ZipInputStream(res.getInputStream());
                Scanner scanner = new Scanner(zip);
                zip.getNextEntry();
                String json = scanner.nextLine();
                TabulaModelParser parser = new TabulaModelParser();
                TabulaModel tbl = parser.parse(json);
                TabulaModelSet set = new TabulaModelSet(tbl, parser, extraData, entry);
                modelMap.put(entry, set);
                if (!modelMaps.containsKey(entry.getTrimmedName())
                        || modelMaps.get(entry.getTrimmedName()) instanceof TabulaModelRenderer)
                    AnimationLoader.modelMaps.put(entry.getTrimmedName(), new TabulaModelRenderer<>(set));
                scanner.close();
                res.close();
            }
            return entry != null;
        }
        catch (IOException e)
        {
            if (entry.getBaseForme() != null)
            {
                TabulaModelSet set;
                if ((set = modelMap.get(entry.getBaseForme())) == null)
                {
                    toReload.add(path);
                }
                else
                {
                    set = new TabulaModelSet(set, extraData, entry);
                    modelMap.put(entry, set);
                    if (!modelMaps.containsKey(entry.getTrimmedName())
                            || modelMaps.get(entry.getTrimmedName()) instanceof TabulaModelRenderer)
                        AnimationLoader.modelMaps.put(entry.getTrimmedName(), new TabulaModelRenderer<>(set));
                }
            }
        }
        return false;
    }

    public static void postProcess()
    {
        for (PokedexEntry entry : modelMap.keySet())
        {
            TabulaModelSet set = modelMap.get(entry);
            if (!set.foundExtra && !entry.base)
            {
                PokedexEntry base = entry.getBaseForme();
                TabulaModelSet baseSet = modelMap.get(base);
                if (baseSet != null)
                {
                    set.rotation = baseSet.rotation;
                    set.scale.set(baseSet.scale);
                    set.shift.set(baseSet.shift);
                    set.headCap = baseSet.headCap;
                    set.headCap1 = baseSet.headCap1;
                }
                else
                {
                    // Not really important for tabula models, they can have
                    // this built in.
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
        }
    }
}
