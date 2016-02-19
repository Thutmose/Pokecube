package pokecube.compat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiGifCapture;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.TypeEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class WikiWriter {
    private static PrintWriter out;
    private static FileWriter fwriter;

    static String pokemobDir = "/Thutmose_I/pokecube-revival/wiki/pokemobs/";
    static String gifDir = "/Thutmose_I/pokecube-revival/wiki/gifs/";
    
    static String formatLinkName(String link, String name)
    {
    	return "[["+link+"|"+name+"]]";
    }
	
	static String formatLink(String dir, String name)
	{
		return "[["+dir+name+"|"+name+"]]";
	}
    
    static void writeWiki()
    {
		int n = 0;
		for(n = 1; n<750; n++)
		{
			PokedexEntry entry = Database.getEntry(n);
			if(entry!=null)
				outputPokemonWikiInfo2(entry);
		}
		writeWikiPokemobList();
		writeWikiHome();
    }
    
    static void writeWikiHome()
    {
		try {
			String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.csv", "Home.wiki");
			fwriter = new FileWriter(fileName);
	     	out = new PrintWriter(fwriter);
	        
	     	out.println("=Welcome to the Pokemob Wiki by Thutmose");

	     	out.println("==List of Mobs");
	     	out.println(formatLinkName("pokemobList", "List of Pokemobs"));

	     	out.println("==List of Blocks");
	     	
	     	out.println("==List of Items");
	     	
			out.close();
			fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	static void writeWikiPokemobList()
	{
		try {
			String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.csv", "pokemobList.wiki");
			fwriter = new FileWriter(fileName);
	     	out = new PrintWriter(fwriter);
	        
	     	out.println("=List of Pokemobs Currently in Pokecube");
	     	int n = 0;
	     	boolean ended = false;
			int m = 0;
			for(m = 1; m<750; m++)
			{
				PokedexEntry e = Database.getEntry(m);
	     		if(e==null)
	     			continue;
	     		ended = false;
	     		out.print("|="+formatLink(pokemobDir, e.getTranslatedName()));
	     		if(n%4==3)
	     		{
	     			out.print("|\n");
	     			ended = true;
	     		}
	     		n++;
	     	}
	     	if(!ended)
	     	{
     			out.print("|\n");
	     	}
	     	
			out.close();
			fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	static void outputPokemonWikiInfo2(PokedexEntry entry)
	{
		try {
			String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.csv", "pokemobs/"+entry.getTranslatedName()+".wiki");
			File temp = new File(fileName.replace(entry.getName()+".wiki", "")); 
			if(!temp.exists())
			{
				temp.mkdirs();
			}
			
			fwriter = new FileWriter(fileName);
	     	out = new PrintWriter(fwriter);

	     	String typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry.getType1()));
	     	if(entry.getType2()!=PokeType.unknown)
	     		typeString += "/"+WordUtils.capitalize(PokeType.getTranslatedName(entry.getType2()));
	     	
	     	//Print links to other pokemon
	     	PokedexEntry nex = Pokedex.getInstance().getNext(entry, 1);
	     	PokedexEntry pre = Pokedex.getInstance().getPrevious(entry, 1);
	     	String otherPokemon = "<-|=->";
	     	String next = "";
	     	if(nex!=entry)
	     	{
	     		next = formatLink(pokemobDir, nex.getTranslatedName());//"[["+pokemobDir+nex.getTranslatedName()+"]]";
	     	}
	     	String prev = "";
	     	if(pre!=entry)
	     	{
	     		prev = formatLink(pokemobDir, pre.getTranslatedName());
	     	}
	     	otherPokemon = "|="+prev+otherPokemon+next;
	     	
	     	out.println(otherPokemon);
	     	
	     	//Print the name and header
	     	out.println("="+entry.getName());
	     	String numString = entry.getPokedexNb()+"";
	     	if(entry.getPokedexNb() < 10)
	     		numString = "00"+numString;
	     	else if(entry.getPokedexNb()<100)
	     		numString = "0"+numString;
	     	out.println("|=Type: "+typeString+"\\\\"+"Number: "+numString+"\\\\|="+"{{"+gifDir+numString+".gif}}\\\\");
	     	
	     	//Print the description
	     	out.println("==Description");
	     	String desc = entry.getName()+" is a "+typeString+" pokemob.";
	     	if(entry.canEvolve())
	     	{
	     		for(EvolutionData d: entry.evolutions)
	     		{
	     			if(Database.getEntry(d.evolutionNb)==null)
	     				continue;
	     			String evoString = formatLink(pokemobDir, Database.getEntry(d.evolutionNb).getTranslatedName());
	     			if(d.level>0)
	     			{
	     				evoString+= " at Level "+d.level;
	     			}
	     			else if(d.item!=null && d.gender==0)
	     			{
	     				evoString+=" when given "+d.item.getDisplayName();
	     			}
	     			else if(d.item!=null && d.gender==1)
	     			{
	     				evoString+=" when male and given "+d.item.getDisplayName();
	     			}
	     			else if(d.item!=null && d.gender==2)
	     			{
	     				evoString+=" when female and given "+d.item.getDisplayName();
	     			}
	     			else if(d.traded && d.item != null)
	     			{
	     				evoString+=" when traded and given "+d.item.getDisplayName();
	     			}
	     			else if(d.happy)
	     			{
	     				evoString+=" when Happy";
	     			}
	     			else if(d.traded)
	     			{
	     				evoString+=" when traded";
	     			}
		     		desc += " "+entry.getTranslatedName()+" Evolves into "+evoString+".";
	     		}
	     		
	     	}
	     	if(entry.evolvesFrom!=null)
	     	{
	     		String evoString = formatLink(pokemobDir, entry.evolvesFrom.getTranslatedName());
	     		desc += " "+entry.getTranslatedName()+" Evolves from "+evoString+".";
	     	}
	     	out.println(desc);
	     	
	     	//Print move list
	     	out.println("==Natural Moves List");
	     	out.println("|=Level|=Move|");
	     	List<String> moves = new ArrayList<String>(entry.getMoves());
	     	List<String> used = new ArrayList<String>();
	     	for(int i = 1; i<=100; i++)
	     	{
	     		List<String> newMoves = entry.getMovesForLevel(i, i-1);
	     		if(!newMoves.isEmpty())
	     		{
	     			for(String s: newMoves)
	     			{
		    	     	out.println("|="+(i==1?"-":i)+"|="+MovesUtils.getTranslatedMove(s)+"|");
		    	     	for(String s1:moves)
		    	     	{
		    	     		if(s1.equalsIgnoreCase(s))
		    	     			used.add(s1);
		    	     	}
	     			}
	     		}
	     	}
	     	moves.removeAll(used);
	     	
	     	if(moves.size()>0)
	     	{
		     	out.println("==TM or Egg Moves List");
		     	boolean ended = false;
		     	int n = 0;
		     	for(String s: moves)
		     	{
		     		ended = false;
		     		out.print("|="+MovesUtils.getTranslatedMove(s));
		     		if(n%4==3)
		     		{
		     			out.print("|\n");
		     			ended = true;
		     		}
		     		n++;
		     	}
		     	if(!ended)
		     	{
	     			out.print("|\n");
		     	}
	     	}
	     	if(!entry.related.isEmpty())
	     	{
		     	out.println("==Compatable for Breeding");
		     	int n = 0;
		     	boolean ended = false;
		     	for(PokedexEntry e: entry.related)
		     	{
		     		if(e==null)
		     			continue;
		     		ended = false;
		     		out.print("|="+formatLink(pokemobDir, e.getTranslatedName()));
		     		if(n%4==3)
		     		{
		     			out.print("|\n");
		     			ended = true;
		     		}
		     		n++;
		     	}
		     	if(!ended)
		     	{
	     			out.print("|\n");
		     	}
	     	}
	     	SpawnData data = entry.getSpawnData();
	     	if(data==null && Database.getEntry(entry.getChildNb())!=null)
	     	{
	     		data = Database.getEntry(entry.getChildNb()).getSpawnData();
	     	}
	     	if(data!=null)
	     	{
	     		out.println("==Biomes Found in");
		     	int n = 0;
		     	boolean ended = false;

		     	for(TypeEntry t: data.allTypes)
		     	{
		     		for(BiomeGenBase b: BiomeGenBase.getBiomeGenArray())
		     		{
		     			if(t.isValid(b))
		     			{
				     		ended = false;
				     		out.print("|="+b.biomeName);
				     		if(n%4==3)
				     		{
				     			out.print("|\n");
				     			ended = true;
				     		}
				     		n++;
		     			}
		     		}
		     	}
		     	for(TypeEntry t: data.anyTypes)
		     	{
		     		for(BiomeGenBase b: BiomeGenBase.getBiomeGenArray())
		     		{
		     			if(t.isValid(b))
		     			{
				     		ended = false;
				     		out.print("|="+b.biomeName);
				     		if(n%4==3)
				     		{
				     			out.print("|\n");
				     			ended = true;
				     		}
				     		n++;
		     			}
		     		}
		     	}
		     	
		     	if(!ended)
		     	{
	     			out.print("|\n");
		     	}
	     	}
	     	
	     	out.println(formatLinkName("pokemobList", "List of Pokemobs")+"-------"+formatLinkName("Home", "Home")+"\\\\");
			out.close();
			fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean gifCaptureState;
	private static int currentCaptureFrame;
	private static int currentPokemob = 1;
	private static int numberTaken = 1;
//	private static final int NUM_CAPTURE_FRAMES = 30;
//	private static final int NUM_POKEMOBS = 424;
//	private static final int WINDOW_XPOS = 1;
//	private static final int WINDOW_YPOS = 1;
//	private static final int WINDOW_WIDTH = 200;
//	private static final int WINDOW_HEIGHT = 200;
	
	static private void openPokedex()
	{
		Minecraft.getMinecraft().thePlayer.openGui(
				PokecubeCore.instance, 20, Minecraft.getMinecraft().thePlayer.worldObj, 0, 0, 0);
	}
	
//	static private void setPokedexBeginning()
//	{
//		GuiGifCapture.pokedexEntry = Pokedex.getInstance().getEntry(1);
//	}
//	
//	static private void cyclePokedex()
//	{
//		GuiGifCapture.pokedexEntry = Pokedex.getInstance().getNext(GuiGifCapture.pokedexEntry, 1);
//		if(GuiGifCapture.pokedexEntry!=null)
//			currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
//	}
	
	static public void beginGifCapture()
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			gifCaptureState = true;
			openPokedex();
			//setPokedexBeginning();
			
			System.out.println("Beginning gif capture...");
		}
	}
	
	static public boolean isCapturingGif()
	{
		return gifCaptureState;
	}
	
	public static void setCaptureTarget(int number)
	{
		GuiGifCapture.pokedexEntry = Database.getEntry(number);
	}
	
	static public void doCapturePokemobGif()
	{
		if(gifCaptureState && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			doCapturePokemobGifClient();
		}
	}
	
	static private void doCapturePokemobGifClient()
	{
		int h = Minecraft.getMinecraft().displayHeight;
		int w = Minecraft.getMinecraft().displayWidth;
		int x = w/2;//WINDOW_XPOS;
		int y = h/2;//WINDOW_YPOS;
		
		int xb,yb;
		
		xb  = GuiGifCapture.x;
		yb  = GuiGifCapture.y;
		int width = 100 * w / xb;
		int height = 60 *  h / yb;
		
		x += -3*w/32;//- w/4 - w/64 + w/128;
		y += -3*w/32;//- h/4 - h/8 + h/32 + h/64;
		if(GuiGifCapture.pokedexEntry!=null)
			currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
		else
			return;
		String pokename = Compat.CUSTOMSPAWNSFILE.replace("spawns.csv", new String("" + currentPokemob + "_"));
		
		GL11.glReadBuffer(GL11.GL_FRONT);
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		String currentFrameSuffix = new String();
		
		if(currentCaptureFrame < 10) currentFrameSuffix = "0";
		
		currentFrameSuffix += currentCaptureFrame + ".png";
		
		File file = new File(pokename + currentFrameSuffix);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for(int i = 0; i < width; i++) 
		{
		    for(int j = 0; j < height; j++)
		    {
		        int k = (i + (width * j)) * 4;
		        int r = buffer.get(k) & 0xFF;
		        int g = buffer.get(k + 1) & 0xFF;
		        int b = buffer.get(k + 2) & 0xFF;
		        image.setRGB(i, height - (j + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		   
		try {
		    ImageIO.write(image, "png", file);
		    System.out.println("Attempting to write " + pokename + currentFrameSuffix);
		} catch (IOException e) { e.printStackTrace(); }
		
		currentCaptureFrame++;
		
		if(currentCaptureFrame > 28)// NUM_CAPTURE_FRAMES)
		{
			currentCaptureFrame = 0;
			numberTaken++;
		//	cyclePokedex();
			if(numberTaken > 0)//;//NUM_POKEMOBS)
			{
				currentPokemob = 1;
				numberTaken = 1;
				gifCaptureState = false;
				
				System.out.println("Gif capture complete!");
			}
		}
	}
}
