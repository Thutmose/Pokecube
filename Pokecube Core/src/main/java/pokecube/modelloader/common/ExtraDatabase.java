package pokecube.modelloader.common;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.vecmath.Vector3f;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.ModPokecubeML;

public class ExtraDatabase
{
    static class AddedXML
    {
        final String name;
        final String modId;
        final String xml;

        AddedXML(String modId, String name, String xml)
        {
            this.modId = modId;
            this.name = name;
            this.xml = xml;
        }
    }

    @XmlRootElement(name = "details")
    public static class XMLDetails
    {
        @XmlElement(name = "RIDDENOFFSET")
        String offset     = "";
        @XmlElement(name = "MODELSIZE")
        String model_size = "";
        @XmlElement(name = "PARTICLEEFFECTS")
        String particles;
    }

    @XmlRootElement(name = "model")
    public static class XMLModel
    {
        @XmlElement
        XMLTexture customTex;
    }

    @XmlRootElement(name = "customTex")
    public static class XMLTexture
    {
        @XmlElement(name = "forme")
        List<XMLForme> entries = Lists.newArrayList();
    }

    @XmlRootElement(name = "forme")
    public static class XMLForme
    {
        @XmlAttribute
        public String name;
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        /** This gets populated with the actual name of the base pokedex entry
         * assosciated with this file.. */
        String                name;
        @XmlElement
        XMLModel              model;
        @XmlElement(name = "details")
        XMLDetails            details;
        @XmlElement(name = "Pokemon")
        List<XMLPokedexEntry> entries = Lists.newArrayList();

        void init(PokedexEntry base) throws NullPointerException
        {
            if (base == null) { throw new NullPointerException("Null Base Entry"); }
            name = base.getTrimmedName();
            if (model != null && model.customTex != null)
            {
                for (XMLForme f : model.customTex.entries)
                {
                    if (f.name != null)
                    {
                        String name = Database.trim(f.name);
                        if (!ModPokecubeML.addedPokemon.contains(name)) ModPokecubeML.addedPokemon.add(name);
                        boolean has = false;
                        for (XMLPokedexEntry e : entries)
                        {
                            String eName = Database.trim(e.name);
                            if (eName.equals(name))
                            {
                                has = true;
                                break;
                            }
                        }
                        if (!has)
                        {
                            XMLPokedexEntry entry = new XMLPokedexEntry();
                            entry.name = f.name;
                            entry.number = base.getPokedexNb();
                            entries.add(entry);
                        }
                    }
                }
            }
        }
    }

    static HashMap<String, AddedXML>        xmls            = Maps.newHashMap();

    static Set<AddedXML>                    toAdd           = Sets.newHashSet();

    static HashMap<String, XMLPokedexEntry> entries         = Maps.newHashMap();

    /** Map of trimmedname -> xml file found in. This is to allow specifying
     * that a particular model actually exists, even if no specific model or xml
     * is found by that name. */
    public static HashMap<String, String>   resourceEntries = Maps.newHashMap();

    public static void addXMLEntry(String modId, String mobName, List<String> xml)
    {
        String val = "";
        for (String s : xml)
        {
            val += s + "\n";
        }
        toAdd.add(new AddedXML(modId, mobName, val));
    }

    public static void apply()
    {
        apply(null);
    }

    public static void apply(String toApply)
    {
        if (toAdd.isEmpty()) return;
        boolean bar = toApply == null;
        xmls.clear();
        for (AddedXML xml : toAdd)
        {
            boolean add = true;
            AddedXML old = xmls.get(xml.name);
            if (old != null)
            {
                try
                {
                    add = Config.instance.modIdComparator.compare(old.modId, xml.modId) < 0;
                }
                catch (Exception e)
                {
                    try
                    {
                        System.out.println(old.name + " " + old.modId + " " + xml.name + " " + xml.modId + " " + e);
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    add = false;
                }
            }
            if (add)
            {
                xmls.put(xml.name, xml);
            }
        }
        ProgressBar loading = null;
        if (bar)
        {
            loading = ProgressManager.push("XML Overrides", xmls.size());
        }
        for (String s : xmls.keySet())
        {
            if (toApply != null && !toApply.equalsIgnoreCase(s)) continue;
            PokedexEntry entry = Database.getEntry(s);
            entry = apply(xmls.get(s).xml, entry);
            if (bar) loading.step(entry.getName());
        }
        if (bar)
        {
            ProgressManager.pop(loading);
        }
    }

    public static PokedexEntry apply(XMLFile file, PokedexEntry entry)
    {
        try
        {
            ProgressBar loading = ProgressManager.push("XML Files", file.entries.size());
            for (XMLPokedexEntry fileEntry : file.entries)
            {
                loading.step(fileEntry.name);
                XMLPokedexEntry old = PokedexEntryLoader.database.map.get(fileEntry.name);
                PokedexEntry other = Database.getEntry(fileEntry.name);
                if (PokecubeMod.debug) PokecubeMod.log("ResourceEntry: " + fileEntry.name + " " + file.name);
                if (other != null) resourceEntries.put(other.getTrimmedName(), file.name);

                // Ensure that the loaded forms inherit the same texture path as
                // the base forms.
                if (other != null && !other.base)
                {
                    other.texturePath = other.getBaseForme().texturePath;
                    other.setModId(other.getBaseForme().getModId());
                }
                if (old != null) PokedexEntryLoader.mergeNonDefaults(PokedexEntryLoader.missingno, fileEntry, old);
                else
                {
                    PokedexEntryLoader.database.addEntry(fileEntry);
                }
            }
            ProgressManager.pop(loading);
            if (entry != null && file.details != null)
            {
                if (!file.details.offset.isEmpty())
                {
                    String[] args = file.details.offset.split(":");
                    List<double[]> offsets = Lists.newArrayList();
                    for (String s : args)
                    {
                        String[] vec = s.split(",");
                        if (vec.length == 1)
                        {
                            offsets.add(new double[] { 0, Float.parseFloat(vec[0]), 0 });
                        }
                        else if (vec.length == 3)
                        {
                            offsets.add(new double[] { Float.parseFloat(vec[0]), Float.parseFloat(vec[1]),
                                    Float.parseFloat(vec[2]) });
                        }
                        else
                        {
                            throw new IllegalArgumentException("Wrong number of numbers for offset, must be 1 or 3");
                        }
                    }
                    if (!offsets.isEmpty())
                    {
                        entry.passengerOffsets = new double[offsets.size()][];
                        for (int i = 0; i < entry.passengerOffsets.length; i++)
                        {
                            entry.passengerOffsets[i] = offsets.get(i);
                        }
                    }
                    if (!file.details.model_size.isEmpty())
                    {
                        String[] vec = file.details.model_size.split(",");
                        if (vec.length == 3)
                        {
                            entry.modelSize = new Vector3f(Float.parseFloat(vec[0]), Float.parseFloat(vec[1]),
                                    Float.parseFloat(vec[2]));
                        }
                        else
                        {
                            throw new IllegalArgumentException(
                                    "Wrong number of numbers for model_size, must be 1 or 3");
                        }
                    }
                }
                if (file.details.particles != null) entry.particleData = file.details.particles.split(":");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return entry;
    }

    public static PokedexEntry apply(String xml, PokedexEntry entry)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLFile.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XMLFile file = (XMLFile) unmarshaller.unmarshal(new StringReader(xml));
            try
            {
                file.init(entry);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, xml + " " + entry, e);
            }
            return apply(file, entry);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with " + entry + " " + xml, e);
        }
        return entry;
    }

    public static void cleanup()
    {
        xmls = null;
        entries = null;
    }

    public static XMLPokedexEntry getEntry(String name)
    {
        return entries.get(name);
    }
}
