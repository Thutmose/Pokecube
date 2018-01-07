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
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.world.gen.WorldGenMultiTemplate;
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;
import pokecube.core.world.gen.template.PokecubeTemplates;
import thut.core.common.commands.CommandTools;

public class StructureCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "pokebuild";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pokebuild <structure||!reload> <optional|direction>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        if (args.length == 0) throw new CommandException(getUsage(cSender));
        String structure = args[0];
        if (structure.equals("!reload"))
        {
            WorldgenHandler.reloadWorldgen();
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
        SpawnBiomeMatcher matcher = SpawnBiomeMatcher.ALLMATCHER;
        matcher.reset();

        if (generator == null)
        {
            if (PokecubeTemplates.getTemplate(structure) != null)
                generator = new TemplateGen(structure, matcher, 1, offset);
            else throw new CommandException("No Structure of that name found in config/pokecube/structures!");
        }

        if (generator != null)
        {
            BlockPos pos = cSender.getPosition();
            Chunk chunk = world.getChunkFromBlockCoords(pos);

            int chunkX = chunk.x;
            int chunkZ = chunk.z;

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
                gen.dir = dir;
                gen.origin = pos;
                gen.chance = 1;
            }
            int dr = 4;
            for (int x = chunkX - dr; x <= chunkX + dr; x++)
                for (int z = chunkZ - dr; z <= chunkZ + dr; z++)
                {
                    generator.generate(world.rand, x, z, world, world.getChunkProvider().chunkGenerator,
                            world.getChunkProvider());
                    if (generator instanceof TemplateGen)
                    {
                        TemplateGen gen = (TemplateGen) generator;
                        gen.chance = 0;
                    }
                    if (generator instanceof WorldGenMultiTemplate)
                    {
                        WorldGenMultiTemplate gen = (WorldGenMultiTemplate) generator;
                        gen.chance = 0;
                    }
                }
            CommandTools.sendMessage(cSender, "Generated " + structure + " " + generator);
        }
    }
}
