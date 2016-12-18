package pokecube.compat.journeymap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;

@ClientPlugin
public class JourneymapCompat implements IClientPlugin
{
    public JourneymapCompat()
    {
    }

    @Override
    public void initialize(IClientAPI jmClientApi)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getModId()
    {
        return "pokecube";
    }

    @Override
    public void onEvent(ClientEvent event)
    {
        // TODO Auto-generated method stub

    }
}
