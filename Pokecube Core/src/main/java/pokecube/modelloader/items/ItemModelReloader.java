package pokecube.modelloader.items;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.client.custom.LoadedModel;
import pokecube.modelloader.client.custom.animation.AnimationLoader;
import pokecube.modelloader.client.custom.tbl.TblModel;
import pokecube.modelloader.client.tabula.TabulaPackLoader;
import pokecube.modelloader.client.tabula.TabulaPackLoader.TabulaModelSet;
import pokecube.modelloader.client.tabula.components.AnimationComponent;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModel;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModelParser;

public class ItemModelReloader extends Item
{
    public static ItemModelReloader instance;

    public ItemModelReloader()
    {
        super();
        instance = this;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        if (!world.isRemote) return itemstack;
        
        System.out.println();
        
        for(PokedexEntry e: Database.allFormes)
        {
            if(e.getNb() == 201)
                System.out.println(e);
        }
        
        
//        if (!player.isSneaking()) AnimationLoader.load();
//        else
//        {
//            
//        }
        return itemstack;
    }

}
