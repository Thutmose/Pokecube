package pokecube.core.events.onload;

import net.minecraftforge.fml.common.eventhandler.Event;

public class InitDatabase extends Event
{

    public InitDatabase()
    {
    }

    /** This event is called before initializing the Database, you should use it
     * to add any extra database files you need to add. */
    public static class Pre extends InitDatabase
    {
    }

    /** This is called after the database is initialized, you can edit the
     * values here, or add new entries if needed */
    public static class Post extends InitDatabase
    {
    }

    /** This is called after the first database file has loaded, but before any
     * others. This allows the resourcepacks to be loaded in first, then allows
     * secondary config databases to override them. */
    public static class Load extends InitDatabase
    {

    }

}
