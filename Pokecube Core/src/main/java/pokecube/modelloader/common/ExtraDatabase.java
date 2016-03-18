package pokecube.modelloader.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Maps;

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
        XMLPokedexEntry entry;
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
        if (xmls != null) for (String s : xmls.keySet())
        {
            if (toApply != null && !toApply.equalsIgnoreCase(s)) continue;

            PokedexEntry entry = Database.getEntry(s);
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLFile.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                XMLFile file = (XMLFile) unmarshaller.unmarshal(new StringReader(xmls.get(s)));
                if (entry == null && file.entry != null)
                {
                    String name = file.entry.name;
                    int number = file.entry.number;
                    entry = new PokedexEntry(number, name);
                    PokedexEntryLoader.updateEntry(file.entry, true);
                }
                if (file.entry != null) PokedexEntryLoader.addOverrideEntry(file.entry);

                if (entry != null && file.details != null)
                {
                    if (file.details.offset != -1) entry.mountedOffset = file.details.offset;
                    if (file.details.particles != null) entry.particleData = file.details.particles.split(":");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
