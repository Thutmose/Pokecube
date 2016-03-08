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
    static HashMap<String, String> xmls;

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
        if (xmls != null) for (String s : xmls.keySet())
        {
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
                    System.out.println("Updating Entry stage 1 for " + entry);
                    PokedexEntryLoader.updateEntry(file.entry, true);
                }
                if (file.entry != null)
                {
                    System.out.println("Updating Entry stage 2 for " + entry);
                    PokedexEntryLoader.updateEntry(file.entry, false);
                }

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
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        @XmlElement(name = "model")
        XMLModel        model;
        @XmlElement(name = "details")
        XMLDetails      details;
        @XmlElement(name = "Pokemon")
        XMLPokedexEntry entry;
    }

    @XmlRootElement(name = "model")
    public static class XMLModel
    {

    }

    @XmlRootElement(name = "details")
    public static class XMLDetails
    {
        @XmlElement(name = "RIDDENOFFSET")
        float  offset = -1;
        @XmlElement(name = "PARTICLEEFFECTS")
        String particles;
    }
}
