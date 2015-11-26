package pokecube.core.asm;

import java.util.Arrays;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import pokecube.core.interfaces.PokecubeMod;

public class PokecubeCoreContainer extends DummyModContainer {

    public PokecubeCoreContainer()
    {
        super(new ModMetadata());
        ModMetadata myMeta = super.getMetadata();
        myMeta.authorList = Arrays.asList("Thutmose");
        myMeta.description = "Uncategorized framework";
        myMeta.modId = PokecubeMod.ID;
        myMeta.version = PokecubeMod.VERSION;
        myMeta.name = PokecubeMod.ID;
        myMeta.url = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2218222";
    }
    
    @Override
    public boolean registerBus(com.google.common.eventbus.EventBus bus, LoadController controller)
    {
    	bus.register(this);
        return true;
    }
}
