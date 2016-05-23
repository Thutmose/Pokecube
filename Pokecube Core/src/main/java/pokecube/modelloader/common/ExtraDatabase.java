package pokecube.modelloader.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;

public class ExtraDatabase
{
    @XmlRootElement(name = "details")
    public static class XMLDetails
    {
        @XmlElement(name = "RIDDENOFFSET")
        float  offset = -1;
        @XmlElement(name = "PARTICLEEFFECTS")
        String particles;
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        @XmlElement(name = "details")
        XMLDetails      details;
        @XmlElement(name = "Pokemon")
        List<XMLPokedexEntry> entries = Lists.newArrayList();
    }

    static HashMap<String, String>          xmls;

    static HashMap<String, XMLPokedexEntry> entries = Maps.newHashMap();

    public static void addXML(String name, ArrayList<String> xml)
    {
        if (xmls == null) xmls = Maps.newHashMap();
        String val = "";
        for (String s : xml)
        {
            val += s + "\n";
        }
        xmls.put(name, val);
    }

    public static void apply()
    {
        apply(null);
    }

    public static void apply(String toApply)
    {
        boolean bar = toApply == null;

        ProgressBar loading = null;
        if (bar)
        {
            loading = ProgressManager.push("XML Overrides", xmls.size());
        }

        if (xmls != null) for (String s : xmls.keySet())
        {
            if (toApply != null && !toApply.equalsIgnoreCase(s)) continue;

            PokedexEntry entry = Database.getEntry(s);
            if (bar) loading.step(entry.getName());
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLFile.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                XMLFile file = (XMLFile) unmarshaller.unmarshal(new StringReader(xmls.get(s)));
                for(XMLPokedexEntry fileEntry: file.entries)
                {
                    if (entry == null && fileEntry != null)
                    {
                        String name = fileEntry.name;
                        int number = fileEntry.number;
                        entry = new PokedexEntry(number, name);
                        PokedexEntryLoader.updateEntry(fileEntry, true);
                    }
                    else if(entry!=null && fileEntry!=null && !entry.getName().equals(fileEntry.name))
                    {
                        String name = fileEntry.name;
                        int number = fileEntry.number;
                        entry = new PokedexEntry(number, name);
                        PokedexEntryLoader.updateEntry(fileEntry, true);
                    }
                    if (fileEntry != null) PokedexEntryLoader.addOverrideEntry(fileEntry);
    
                    if (entry != null && file.details != null)
                    {
                        if (file.details.offset != -1) entry.mountedOffset = file.details.offset;
                        if (file.details.particles != null) entry.particleData = file.details.particles.split(":");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (bar)
        {
            ProgressManager.pop(loading);
        }
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
