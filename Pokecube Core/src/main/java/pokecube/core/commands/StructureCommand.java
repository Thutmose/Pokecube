package pokecube.core.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.worldgen.XMLWorldgenHandler;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.world.gen.WorldGenMultiTemplate;
import pokecube.core.world.gen.WorldGenMultiTemplate.Template;
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class StructureCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandName()
    {
        return "pokebuild";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pokebuild <structure||!reload> <optional|direction>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        String structure = args[0];
        if (structure.equals("!reload"))
        {
            XMLWorldgenHandler.reloadWorldgen();
            CommandTools.sendMessage(cSender, "Reloaded Structures");
            return;
        }

        EnumFacing dir = EnumFacing.HORIZONTALS[new Random().nextInt(EnumFacing.HORIZONTALS.length)];
        int offset = 0;

        if (args.length > 1)
        {
            dir = EnumFacing.byName(args[1]);
        }
        WorldServer world = (WorldServer) cSender.getEntityWorld();
        IWorldGenerator generator = WorldGenTemplates.namedTemplates.get(structure);
        SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "all");
        SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rule);

        if (generator == null)
        {
            if (PokecubeTemplates.getTemplate(structure) != null)
                generator = new TemplateGen(structure, matcher, 1, offset);
        }

        if (generator != null)
        {
            BlockPos pos = cSender.getPosition();
            Chunk chunk = world.getChunkFromBlockCoords(pos);

            int chunkX = chunk.xPosition;
            int chunkZ = chunk.zPosition;

            if (generator instanceof TemplateGen)
            {
                TemplateGen gen = (TemplateGen) generator;
                generator = gen = new TemplateGen(gen.template, matcher, 1, gen.offset);
                gen.dir = dir;
                gen.origin = pos;
            }
            else if (generator instanceof WorldGenMultiTemplate)
            {
                WorldGenMultiTemplate gen = (WorldGenMultiTemplate) generator;
                WorldGenMultiTemplate old = gen;
                generator = gen = new WorldGenMultiTemplate();
                for (Template gen1 : old.subTemplates)
                {
                    Template newTemplate = new Template();
                    newTemplate.template = new TemplateGen(gen1.template.template, matcher, 1, gen1.template.offset);
                    gen.subTemplates.add(newTemplate);
                }
                gen.dir = dir;
                gen.origin = pos;
            }
            for (int x = chunkX - 3; x <= chunkX + 3; x++)
                for (int z = chunkZ - 3; z <= chunkZ + 3; z++)
                {
                    generator.generate(world.rand, x, z, world, world.getChunkProvider().chunkGenerator,
                            world.getChunkProvider());
                }
        }
        else throw new CommandException("No Structure of that name found in config/pokecube/structures!");
    }
}
