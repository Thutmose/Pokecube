package pokecube.modelloader.client.render.model;

public interface IPartTexturer
{
    /** Applies the texture for the part.<br>
     * This method will bind the texture to render engine for the part.
     * 
     * @param part */
    void applyTexture(String part);

    /** Binds the object under consideration.
     * 
     * @param thing */
    void bindObject(Object thing);

    /** Shifts the UVs for the texture animation
     * 
     * @param part
     * @param toFill
     * @return */
    boolean shiftUVs(String part, double[] toFill);

    /** Should the part use flat shading. Defaults to true
     * 
     * @param part
     * @return */
    boolean isFlat(String part);

    /** Adds a mapping of part texture.
     * 
     * @param part
     *            - The part or material to be textured
     * @param tex
     *            - The name of the texture. */
    void addMapping(String part, String tex);

    /** Adds mapping for a custom state's texture
     * 
     * @param part
     *            - Part or Material name
     * @param state
     *            - State to be mapped, either AI state, or an integer.
     * @param tex
     *            - Texture being mapped. */
    void addCustomMapping(String part, String state, String tex);
}
