package pokecube.modelloader;

import pokecube.core.database.PokedexEntry;

public interface IMobProvider
{
    /** Locations of model inside the resources.<br>
     * Example for a default location would be<br>
     * "models/pokemobs/" */
    String getModelDirectory(PokedexEntry entry);

    /** Locations of texture inside the resources.<br>
     * Example for a default location would be<br>
     * "models/pokemobs/" */
    String getTextureDirectory(PokedexEntry entry);

    /** Should return the @Mod object associated with this provider, used for
     * locating the modid */
    Object getMod();
}
