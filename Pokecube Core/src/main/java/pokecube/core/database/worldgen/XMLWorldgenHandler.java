package pokecube.core.database.worldgen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;

public class XMLWorldgenHandler
{
    @XmlRootElement(name = "Structures")
    public static class XMLStructures
    {
        @XmlElement(name = "Structure")
        public List<XMLStructure> structures = Lists.newArrayList();
    }

    @XmlRootElement(name = "Structure")
    public static class XMLStructure
    {
        @XmlAttribute
        public String name;
        @XmlAttribute
        float         chance;
        @XmlAttribute
        int           offset;
        @XmlAttribute
        String        biomes;
        @XmlAttribute
        public String biomeType;
    }

    public static XMLStructures defaults = new XMLStructures();
    static
    {
        XMLStructure ruin_1 = new XMLStructure();
        ruin_1.name = "ruin_1";
        ruin_1.chance = 0.002f;
        ruin_1.offset = -3;
        ruin_1.biomes = "BTplains";
        ruin_1.biomeType = "ruin";
        defaults.structures.add(ruin_1);
    }

    public static void loadDefaults(File defaultsFile)
    {
        if (!defaultsFile.exists())
        {
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLStructures.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                // output pretty printed
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(defaults, defaultsFile);
            }
            catch (JAXBException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLStructures.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                FileReader reader = new FileReader(defaultsFile);
                defaults = (XMLStructures) unmarshaller.unmarshal(reader);
                reader.close();
            }
            catch (JAXBException | IOException e)
            {
                e.printStackTrace();
            }
        }
        for (XMLStructure struct : defaults.structures)
        {
            try
            {
                WorldGenTemplates.TemplateGen template = new TemplateGen(struct.name, struct.biomes, struct.chance,
                        struct.offset);
                WorldGenTemplates.templates.add(template);
            }
            catch (Exception e)
            {
                System.out.println(struct.name + " " + struct.biomes + " " + struct.chance + " " + struct.offset);
                e.printStackTrace();
            }
        }
    }
}
