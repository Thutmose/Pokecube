package pokecube.modelloader.client.render.x3d;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import pokecube.modelloader.client.render.model.IPartTexturer;
import pokecube.modelloader.client.render.model.TextureCoordinate;
import pokecube.modelloader.client.render.model.Vertex;

public class Shape
{
    private int                meshId  = 0;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    private Material           material;
    public String              name;
    private double[]           uvShift = { 0, 0 };

    public Shape(String index, String coordinate, String normal, String texture)
    {
        String[] offset = index.split(" ");
        vertices = parseVertices(coordinate).toArray(new Vertex[0]);
        textureCoordinates = parseTextures(texture).toArray(new TextureCoordinate[0]);
        if (normal != null)
        {
            normals = parseVertices(normal).toArray(new Vertex[0]);
        }
        order = new Integer[offset.length];
        for (int i = 0; i < offset.length; i++)
        {
            String s1 = offset[i];
            order[i] = (Integer.parseInt(s1));
        }
    }

    public void setMaterial(Material material)
    {
        this.material = material;
        this.name = material.name;
    }

    public void renderShape(IPartTexturer texturer)
    {
        // Compiles the list of the meshId is invalid.
        compileList(texturer);

        boolean textureShift = false;
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.applyTexture(name);
            if (textureShift = texturer.shiftUVs(name, uvShift))
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glTranslated(uvShift[0], uvShift[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        // Call the list
        GL11.glCallList(meshId);
        GL11.glFlush();

        // Reset Texture Matrix if changed.
        if (textureShift)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    private void compileList(IPartTexturer texturer)
    {
        if (!GL11.glIsList(meshId))
        {
            if (material != null && texturer != null && !texturer.hasMapping(material.name) && material.texture!=null) texturer.addMapping(material.name, material.texture);
            meshId = GL11.glGenLists(1);
            GL11.glNewList(meshId, GL11.GL_COMPILE);
            addTris(texturer);
            GL11.glEndList();
        }
    }

    void addTris(IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate;
        Vector3f[] normalList = new Vector3f[order.length];
        boolean flat = true;
        if (texturer != null) flat = texturer.isFlat(name);
        if (flat)
        {
            // Calculate the normals for each triangle.
            for (int i = 0; i < order.length; i += 3)
            {
                Vector3f v1, v2, v3;
                vertex = vertices[order[i]];
                v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
                vertex = vertices[order[i + 1]];
                v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
                vertex = vertices[order[i + 2]];
                v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
                Vector3f a = new Vector3f(v2);
                a.sub(v1);
                Vector3f b = new Vector3f(v3);
                b.sub(v1);
                Vector3f c = new Vector3f();
                c.cross(a, b);
                c.normalize();
                normalList[i] = c;
                normalList[i + 1] = c;
                normalList[i + 2] = c;
            }
            GL11.glShadeModel(GL11.GL_FLAT);
        }
        else
        {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        if (material != null)
        {
            material.preRender();
        }

        GL11.glBegin(GL11.GL_TRIANGLES);
        int n = 0;
        for (Integer i : order)
        {
            textureCoordinate = textureCoordinates[i];
            GL11.glTexCoord2d(textureCoordinate.u, textureCoordinate.v);
            vertex = vertices[i];
            if (flat)
            {
                Vector3f norm = normalList[n];
                GL11.glNormal3f(norm.x, norm.y, norm.z);
            }
            else
            {
                normal = normals[i];
                GL11.glNormal3f(normal.x, normal.y, normal.z);
            }
            n++;
            GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
        }
        GL11.glEnd();
        
        if (material != null)
        {
            material.postRender();
        }
    }

    private ArrayList<Vertex> parseVertices(String line) throws ModelFormatException
    {
        ArrayList<Vertex> ret = new ArrayList<Vertex>();

        String[] points = line.split(" ");
        if (points.length
                % 3 != 0) { throw new ModelFormatException("Invalid number of elements in the points string"); }
        for (int i = 0; i < points.length; i += 3)
        {
            Vertex toAdd = new Vertex(Float.parseFloat(points[i]), Float.parseFloat(points[i + 1]),
                    Float.parseFloat(points[i + 2]));
            ret.add(toAdd);
        }
        return ret;
    }

    private ArrayList<TextureCoordinate> parseTextures(String line) throws ModelFormatException
    {
        ArrayList<TextureCoordinate> ret = new ArrayList<TextureCoordinate>();
        String[] points = line.split(" ");
        if (points.length % 2 != 0) { throw new ModelFormatException(
                "Invalid number of elements in the points string " + points.length); }
        for (int i = 0; i < points.length; i += 2)
        {
            TextureCoordinate toAdd = new TextureCoordinate(Float.parseFloat(points[i]),
                    1 - Float.parseFloat(points[i + 1]));
            ret.add(toAdd);
        }
        return ret;
    }
}
