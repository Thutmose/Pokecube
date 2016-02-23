package pokecube.modelloader.client.render.smd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pokecube.modelloader.client.render.smd.Skeleton.Bone;
import pokecube.modelloader.client.render.smd.SkeletonAnimation.SkeletonFrame;
import pokecube.modelloader.client.render.smd.Triangles.Triangle;

public class SMDParser
{
    // private ResourceLocation resource;
    private SMDModel model;

    public static void main(String[] args)
    {
        new SMDParser(new File("./Cube.smd"));
    }

    // public SMDParser(ResourceLocation model)
    public SMDParser(File model)
    {
        // this.resource = model;
        InputStream stream = null;
        try
        {
            stream = new FileInputStream(model);
            // IResource res =
            // Minecraft.getMinecraft().getResourceManager().getResource(resource);
            // stream = res.getInputStream();
            parse(stream);
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
        }
    }

    public void parse(InputStream stream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        // Read first line, it is always "version 1" anyway.
        String line = reader.readLine();
        Skeleton skelly = null;
        SkeletonFrame currentFrame = null;
        String section = "";
        Triangle currentTriangle = null;
        Triangles triangles = null;
        boolean end = true;
        while ((line = reader.readLine()) != null)
        {
            if (end)
            {
                section = line;
                end = false;
            }
            else if (line.equals("end"))
            {
                end = true;
            }
            else
            {
                if (section.equals("nodes"))
                {
                    if (skelly == null)
                    {
                        skelly = new Skeleton();
                        triangles = new Triangles(skelly);
                    }
                    Bone bone = new Bone(line);
                    skelly.addBone(bone);
                }
                else if (section.equals("skeleton"))
                {
                    if (skelly.defaultPose == null)
                    {
                        skelly.defaultPose = new SkeletonAnimation(skelly);
                    }
                    if (line.contains("time"))
                    {
                        currentFrame = new SkeletonFrame(Integer.parseInt(line.split(" ")[1].trim()),
                                skelly.defaultPose);
                        skelly.defaultPose.frames.add(currentFrame);
                    }
                    else
                    {
                        currentFrame.addFromLine(line);
                    }
                }
                else if (section.equals("triangles"))
                {
                    line = line.replaceAll("\\s+", " ");
                    String[] args = line.split(" ");
                    boolean test = false;
                    try
                    {
                        Integer.parseInt(args[0]);
                        test = true;
                        currentTriangle.addVertex(line);
                    }
                    catch (NumberFormatException e)
                    {
                        if (!test)
                        {
                            currentTriangle = new Triangle(line, triangles);
                            triangles.triangles.add(currentTriangle);
                        }
                    }
                }
            }
        }
        skelly.initChildren();
        System.out.println(skelly.boneMap);
        System.out.println(skelly.defaultPose.frames);
        System.out.println(triangles.triangles.size());
    }
}
