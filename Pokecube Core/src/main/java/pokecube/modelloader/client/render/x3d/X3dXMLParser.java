package pokecube.modelloader.client.render.x3d;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class X3dXMLParser
{
    HashMap<String, HashMap<String, String>> partTranslations;
    HashMap<String, HashMap<String, String>> partPoints;
    HashMap<String, ArrayList<String>>       partChildren;
    HashMap<String, ArrayList<Shape>>        shapeMap;
    HashMap<String, Material>                matMap;
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

            partName = doc.getElementsByTagName("meta").item(0).getAttributes().getNamedItem("content").getNodeValue()
                    .replace(".x3d", "");

            partTranslations = new HashMap<String, HashMap<String, String>>();
            partPoints = new HashMap<String, HashMap<String, String>>();
            partChildren = new HashMap<String, ArrayList<String>>();
            matMap = new HashMap<>();
            shapeMap = new HashMap<>();
            Node n;
            NodeList parts = doc.getElementsByTagName("Transform");
            NodeList appearences = doc.getElementsByTagName("Appearance");

            for (int i = 0; i < appearences.getLength(); i++)
            {

                Node appearence = appearences.item(i);
                Node mat = null;
                Node tex = null;
                for (int l = 0; l < appearence.getChildNodes().getLength(); l++)
                {
                    if (appearence.getChildNodes().item(l) == null)
                    {
                        continue;
                    }
                    else if (appearence.getChildNodes().item(l).getNodeName().equals("Material"))
                    {
                        mat = appearence.getChildNodes().item(l);
                    }
                    else if (appearence.getChildNodes().item(l).getNodeName().equals("ImageTexture"))
                    {
                        tex = appearence.getChildNodes().item(l);
                    }
                }
                if (mat != null && tex != null && mat.hasAttributes())
                {
                    if (mat.getAttributes().getNamedItem("DEF") != null
                            && tex.getAttributes().getNamedItem("DEF") != null)
                    {
                        String matName = mat.getAttributes().getNamedItem("DEF").getNodeValue().substring(3);
                        String texName = tex.getAttributes().getNamedItem("DEF").getNodeValue().substring(3);
                        texName = texName.substring(0, texName.lastIndexOf("_png"));

                        Material material = new Material(matName, texName, getVector(mat, "diffuseColor"),
                                getVector(mat, "specularColor"), getVector(mat, "emissiveColor"),
                                getFloat(mat, "ambientIntensity"), getFloat(mat, "shininess"),
                                getFloat(mat, "transparency"));
                        matMap.put(matName, material);
                    }
                }
            }

            int j = 0;
            String name = null;
            for (int i = 0; i < parts.getLength(); i++)
            {
                Node node = parts.item(i);
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
                    parseGroup(node, name);
                    j++;
                }
            }

            if (!triangles)
            {
                System.err.println(model.getResourcePath() + " Is not Triangulated, This may cause issues");
            }
            stream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void parseGroup(Node group, String name)
    {
        for (int i = 0; i < group.getChildNodes().getLength(); i++)
        {
            if (group.getChildNodes().item(i).getNodeName().equals("Shape"))
            {
                Node shapeNode = group.getChildNodes().item(i);
                int triIndex = -1;
                int appIndex = -1;
                Material material = null;

                for (int l = 0; l < shapeNode.getChildNodes().getLength(); l++)
                {
                    if (shapeNode.getChildNodes().item(l).getNodeName().equals("IndexedTriangleSet"))
                    {
                        triIndex = l;
                    }
                    else if (shapeNode.getChildNodes().item(l).getNodeName().equals("Appearance"))
                    {
                        appIndex = l;
                    }
                }
                if (appIndex != -1)
                {
                    Node appearence = shapeNode.getChildNodes().item(appIndex);
                    Node mat = null;
                    for (int l = 0; l < appearence.getChildNodes().getLength(); l++)
                    {
                        if (appearence.getChildNodes().item(l) == null)
                        {
                            continue;
                        }
                        else if (appearence.getChildNodes().item(l).getNodeName().equals("Material"))
                        {
                            mat = appearence.getChildNodes().item(l);
                        }
                    }
                    if (mat != null && mat.hasAttributes())
                    {
                        String matName = null;
                        if (mat.getAttributes().getNamedItem("DEF") != null)
                        {
                            matName = mat.getAttributes().getNamedItem("DEF").getNodeValue().substring(3);
                        }
                        else if (mat.getAttributes().getNamedItem("USE") != null)
                        {
                            matName = mat.getAttributes().getNamedItem("USE").getNodeValue().substring(3);
                        }

                        material = matMap.get(matName);
                    }
                }

                if (triIndex != -1)
                {
                    Node n = shapeNode.getChildNodes().item(triIndex);
                    int points = -1;
                    int tex = -1;
                    int normal = -1;
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
                        if (n.getChildNodes().item(l).getNodeName().equals("Normal"))
                        {
                            normal = l;
                        }
                    }
                    String index = n.getAttributes().getNamedItem("index").getNodeValue();
                    String coordinate = n.getChildNodes().item(points).getAttributes().getNamedItem("point")
                            .getNodeValue();
                    String texture = n.getChildNodes().item(tex).getAttributes().getNamedItem("point").getNodeValue();
                    String normals = null;
                    if (normal != -1)
                        normals = n.getChildNodes().item(normal).getAttributes().getNamedItem("vector").getNodeValue();
                    name = name.replace("group_ME_", "");
                    Shape shape = new Shape(index, coordinate, normals, texture);
                    if (material != null) shape.setMaterial(material);
                    else shape.name = name;
                    addShape(name, shape);
                }
            }
        }

    }

    private void addShape(String name, Shape shape)
    {
        ArrayList<Shape> list;
        if (shapeMap.containsKey(name))
        {
            list = shapeMap.get(name);
        }
        else
        {
            list = Lists.newArrayList();
            shapeMap.put(name, list);
        }
        list.add(shape);
    }

    private Vector3f getVector(Node node, String key)
    {
        String[] var = node.getAttributes().getNamedItem(key).getNodeValue().split(" ");
        return new Vector3f(Float.parseFloat(var[0]), Float.parseFloat(var[1]), Float.parseFloat(var[2]));
    }

    private float getFloat(Node node, String key)
    {
        return Float.parseFloat(node.getAttributes().getNamedItem(key).getNodeValue());
    }
}
