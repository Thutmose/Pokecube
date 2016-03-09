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
    public SMDModel model;

    public SMDParser(File model)
    {
        InputStream stream = null;
        try
        {
            stream = new FileInputStream(model);
            parseModel(stream);
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public SMDModel parseModel(InputStream stream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        // Read first line, it is always "version 1" anyway.
        model = new SMDModel();
        String line = reader.readLine();
        Skeleton skelly = null;
        SkeletonFrame currentFrame = null;
        SkeletonAnimation pose = null;
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
                        skelly = new Skeleton(model);
                        model.skeleton = skelly;
                        triangles = new Triangles(model);
                    }
                    Bone bone = new Bone(line, skelly);
                    skelly.addBone(bone);
                }
                else if (section.equals("skeleton"))
                {
                    if (pose == null)
                    {
                        pose = new SkeletonAnimation(skelly);
                        pose.animationName = "default";
                        model.poses.put("default", pose);
                    }
                    if (line.contains("time"))
                    {
                        currentFrame = new SkeletonFrame(Integer.parseInt(line.split(" ")[1].trim()), pose);
                        pose.frames.add(currentFrame);
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
        model.triangles = triangles;
        skelly.init();
        skelly.setPose(pose);
        return model;
    }

    public void parseAnimation(InputStream stream, String name) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        // Read first line, it is always "version 1" anyway.
        String line = reader.readLine();
        Skeleton skelly = model.skeleton;
        SkeletonAnimation anim = new SkeletonAnimation(skelly);
        anim.animationName = name;
        SkeletonFrame currentFrame = null;
        String section = "";
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
                if (section.equals("skeleton"))
                {
                    if (line.contains("time"))
                    {
                        currentFrame = new SkeletonFrame(Integer.parseInt(line.split(" ")[1].trim()), anim);
                        anim.frames.add(currentFrame);
                    }
                    else
                    {
                        currentFrame.addFromLine(line);
                    }
                }
            }
        }
        model.poses.put(name, anim);
    }
}
