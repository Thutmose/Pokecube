package pokecube.modelloader.client.custom.x3d;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import thut.api.maths.Vector3;

public class X3dObject implements IExtendedModelPart
{
    public ArrayList<Vertex>            vertices           = new ArrayList<Vertex>();
    public ArrayList<Vertex>            vertexNormals      = new ArrayList<Vertex>();
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();
    public ArrayList<Integer>           order              = new ArrayList<Integer>();

    public HashMap<String, IExtendedModelPart> childParts = new HashMap<String, IExtendedModelPart>();
    public final String                        name;
    public IExtendedModelPart                  parent     = null;

    public Vector4 preRot    = new Vector4();
    public Vector4 postRot   = new Vector4();
    public Vector4 postRot1  = new Vector4();
    public Vector3 preTrans  = Vector3.getNewVectorFromPool();
    public Vector3 postTrans = Vector3.getNewVectorFromPool();

    public Vector3 offset    = Vector3.getNewVectorFromPool();
    public Vector4 rotations = new Vector4();
    public Vertex  scale     = new Vertex(1, 1, 1);

    public X3dObject(String name)
    {
        this.name = name;
    }

    public int red        = 255, green = 255, blue = 255, alpha = 255;
    public int brightness = 15728640;

    @Override
    public void addChild(IExtendedModelPart subPart)
    {
        this.childParts.put(subPart.getName(), subPart);
        subPart.setParent(this);
    }

    @Override
    public void setParent(IExtendedModelPart parent)
    {
        this.parent = parent;
    }

    @Override
    public void setPreTranslations(Vector3 point)
    {
        preTrans.set(point);
    }

    @Override
    public void setPostRotations(Vector4 angles)
    {
        postRot = angles;
    }

    @Override
    public void setPostTranslations(Vector3 point)
    {
        postTrans.set(point);
    }

    @Override
    public void setPreRotations(Vector4 angles)
    {
        preRot = angles;
    }

    public void render()
    {
        rotateToParent();
        GL11.glTranslated(offset.x, offset.y, offset.z);
        GL11.glTranslated(preTrans.x, preTrans.y, preTrans.z);
        preRot.glRotate();
        GL11.glTranslated(postTrans.x, postTrans.y, postTrans.z);
        GL11.glTranslated(-offset.x, -offset.y, -offset.z);
        GL11.glPushMatrix();

        GL11.glTranslated(offset.x, offset.y, offset.z);
//        rotations.glRotate();
        postRot.glRotate();
        postRot1.glRotate();
        GL11.glScalef(scale.x, scale.y, scale.z);
        PTezzelator tez = PTezzelator.instance;
        tez.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.BLOCK);
        addForRender(tez);
        tez.end();
        GL11.glPopMatrix();
    }

    public void addForRender(PTezzelator tessellator)
    {
        try
        {
            if (parent != null && parent instanceof X3dObject) brightness = ((X3dObject) parent).brightness;

            short j = (short) (brightness >> 16 & 65535);
            short k = (short) (brightness & 65535);
            for (Integer i : order)
            {
                Vertex vertex = vertices.get(i);
                TextureCoordinate textureCoordinate = i < textureCoordinates.size() ? textureCoordinates.get(i) : null;

                if (textureCoordinate != null)
                    tessellator.vertex(vertex.x, vertex.y, vertex.z).color(red, green, blue, alpha)
                            .tex(textureCoordinate.u, textureCoordinate.v).tex2(j, k).endVertex();
                else tessellator.vertex(vertex.x, vertex.y, vertex.z).endVertex();
            }
        }
        catch (Exception e)
        {
            int m = 0;
            for (Integer i : order)
            {
                m = Math.max(i, m);
            }
            System.err.println(m);
            e.printStackTrace();
        }
    }

    @Override
    public String getType()
    {
        return "x3d";
    }

    @Override
    public void renderAll()
    {
        render();
        for (IExtendedModelPart o : childParts.values())
        {
            GL11.glPushMatrix();
            GL11.glTranslated(offset.x, offset.y, offset.z);
            o.renderAll();
            GL11.glPopMatrix();
        }
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        for (IExtendedModelPart o : childParts.values())
        {
            for (String s : groupNames)
            {
                if (s.equalsIgnoreCase(o.getName()))
                {
                    o.renderOnly(groupNames);
                }
            }
        }
        for (String s : groupNames)
        {
            if (s.equalsIgnoreCase(name)) render();
        }
    }

    @Override
    public void renderPart(String partName)
    {
        if (this.name.equalsIgnoreCase(partName)) render();
        if (childParts.containsKey(partName)) childParts.get(partName).renderPart(partName);
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        for (String s : childParts.keySet())
        {
            for (String s1 : excludedGroupNames)
                if (!s.equalsIgnoreCase(s1))
                {
                    childParts.get(s).renderAllExcept(excludedGroupNames);
                }
        }
        for (String s1 : excludedGroupNames)
            if (s1.equalsIgnoreCase(name)) render();
    }

    @Override
    public int[] getRGBAB()
    {
        return new int[] { red, green, blue, alpha, brightness };
    }

    @Override
    public void setRGBAB(int[] array)
    {
        red = array[0];
        blue = array[1];
        green = array[2];
        alpha = array[3];
        brightness = array[4];
    }

    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return childParts;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Vector3 getDefaultTranslations()
    {
        return offset;
    }

    @Override
    public Vector4 getDefaultRotations()
    {
        return rotations;
    }

    @Override
    public IExtendedModelPart getParent()
    {
        return parent;
    }

    @Override
    public void setPostRotations2(Vector4 rotations)
    {
        postRot1 = rotations;
    }

    private void rotateToParent()
    {
        if (parent != null)
        {
            if (parent instanceof X3dObject)
            {
                X3dObject parent = ((X3dObject) this.parent);
                parent.rotateToParent();
                parent.postRot.glRotate();
                parent.postRot1.glRotate();
            }
        }
    }

    @Override
    public void resetToInit()
    {
        preRot.set(0, 1, 0, 0);
        postRot.set(0, 1, 0, 0);
        postRot1.set(0, 1, 0, 0);
        preTrans.clear();
        postTrans.clear();
    }
}
