package pokecube.modelloader.client.custom;

public interface IPartTexturer
{
    void applyTexture(String part);

    void bindObject(Object thing);

    boolean shiftUVs(String part, double[] toFill);

    boolean isFlat(String part);

    void addMapping(String part, String tex);
}
