package pokecube.modelloader.client.custom.x3d;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class X3dXMLParser
{

    HashMap<String, HashMap<String, String>> partTranslations;
    HashMap<String, HashMap<String, String>> partPoints;
    HashMap<String, ArrayList<String>>       partChildren;
    String                                   partName;
    ResourceLocation                         model;
    public boolean                           triangles = true;

    public X3dXMLParser(ResourceLocation model)
    {
        this.model = model;
    }

    public void parse()
    {

        try
        {

            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = res.getInputStream();
            Document doc = dBuilder.parse(stream);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            partName = doc.getElementsByTagName("meta").item(0).getAttributes().getNamedItem("content").getNodeValue()
                    .replace(".x3d", "");

            partTranslations = new HashMap<String, HashMap<String, String>>();
            partPoints = new HashMap<String, HashMap<String, String>>();
            partChildren = new HashMap<String, ArrayList<String>>();
            Node n;
            int j = 0;

            String name = null;
            for (int i = 0; i < doc.getElementsByTagName("Transform").getLength(); i++)
            {
                Node node = doc.getElementsByTagName("Transform").item(i);
                String tag = node.getChildNodes().item(1).getNodeName();
                HashMap<String, String> items = new HashMap<String, String>();
                if (tag.equals("Transform"))
                {
                    name = node.getAttributes().getNamedItem("DEF").getNodeValue();

                    items.put("translation", node.getAttributes().getNamedItem("translation").getNodeValue());
                    items.put("scale", node.getAttributes().getNamedItem("scale").getNodeValue());
                    items.put("rotation", node.getAttributes().getNamedItem("rotation").getNodeValue());
                    name = name.replace("_TRANSFORM", "");
                    partTranslations.put(name, items);
                    items = new HashMap<String, String>();

                    for (int m = 0; m < node.getChildNodes().getLength(); m++)
                    {
                        n = node.getChildNodes().item(m);
                        if (n.hasAttributes())
                        {
                            String childName = n.getAttributes().getNamedItem("DEF").getNodeValue()
                                    .replace("_TRANSFORM", "");
                            if (childName.equals(name) || childName.equals(name + "_ifs")) continue;

                            ArrayList<String> children = partChildren.get(name);
                            if (children == null)
                            {
                                children = new ArrayList<String>();
                                partChildren.put(name, children);
                            }
                            children.add(childName);
                        }
                    }
                }

                if (tag.equals("Group"))
                {
                    node = doc.getElementsByTagName("Group").item(j);
                    int shapeIndex = -1;
                    for (int l = 0; l < node.getChildNodes().getLength(); l++)
                    {
                        if (node.getChildNodes().item(l).getNodeName().equals("Shape"))
                        {
                            shapeIndex = l;
                        }
                    }
                    Node shape = node.getChildNodes().item(shapeIndex);
                    int triIndex = -1;
                    int faceIndex = -1;
                    for (int l = 0; l < shape.getChildNodes().getLength(); l++)
                    {
                        if (shape.getChildNodes().item(l).getNodeName().equals("IndexedFaceSet"))
                        {
                            faceIndex = l;
                        }
                        if (shape.getChildNodes().item(l).getNodeName().equals("IndexedTriangleSet"))
                        {
                            triIndex = l;
                        }
                    }
                    if (triIndex != -1)
                    {
                        n = shape.getChildNodes().item(triIndex);
                        int points = -1;
                        int tex = -1;
                        for (int l = 0; l < n.getChildNodes().getLength(); l++)
                        {
                            if (n.getChildNodes().item(l).getNodeName().equals("Coordinate"))
                            {
                                points = l;
                            }
                            if (n.getChildNodes().item(l).getNodeName().equals("TextureCoordinate"))
                            {
                                tex = l;
                            }
                        }
//                        String name = node.getAttributes().getNamedItem("DEF").getNodeValue();
                        String index = n.getAttributes().getNamedItem("index").getNodeValue();
                        items.put("index", index);
                        items.put("coordinates",
                                n.getChildNodes().item(points).getAttributes().getNamedItem("point").getNodeValue());
                        items.put("textures",
                                n.getChildNodes().item(tex).getAttributes().getNamedItem("point").getNodeValue());
                        name = name.replace("group_ME_", "");
                        partPoints.put(name, items);
                    }
                    else if (faceIndex != -1)
                    {
                        triangles = false;
                        n = shape.getChildNodes().item(faceIndex);
                        int points = -1;
                        int tex = -1;
                        for (int l = 0; l < n.getChildNodes().getLength(); l++)
                        {
                            if (n.getChildNodes().item(l).getNodeName().equals("Coordinate"))
                            {
                                points = l;
                            }
                            if (n.getChildNodes().item(l).getNodeName().equals("TextureCoordinate"))
                            {
                                tex = l;
                            }
                        }
//                        String name = node.getAttributes().getNamedItem("DEF").getNodeValue();
                        String index = n.getAttributes().getNamedItem("coordIndex").getNodeValue();
                        items.put("index", index);
                        items.put("coordinates",
                                n.getChildNodes().item(points).getAttributes().getNamedItem("point").getNodeValue());
                        items.put("textures",
                                n.getChildNodes().item(tex).getAttributes().getNamedItem("point").getNodeValue());
                        name = name.replace("group_ME_", "");
                        partPoints.put(name, items);
                    }
                    j++;
                }
            }

            if(!triangles)
            {
                new Exception(
                        "Warning, This mode is buggy, please re-export "+model.getResourcePath()+" with triangulated faces. ")
                                .printStackTrace();
            }
            stream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
