package pokecube.modelloader.client.render.smd;

public class SMDModel
{
    Skeleton skeleton;
    Triangles triangles;
    
    public void render()
    {
        triangles.render();
    }
    
}
