package no.runsafe.warpdrive.commands;

import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.PlayerTeleportCommand;
import no.runsafe.warpdrive.database.WarpRepository;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;

public class Home extends PlayerTeleportCommand
{
	public Home(WarpRepository repository, IScheduler scheduler, Engine engine)
	{
		super("home", "Teleports you to a home location", "runsafe.home.use", scheduler, engine);
		warpRepository = repository;
	}

	@Override
	public PlayerTeleportCommand.PlayerTeleport OnAsyncExecute(RunsafePlayer player, HashMap<String, String> params, String[] args)
	{
		PlayerTeleport target = new PlayerTeleport();
		target.player = player;
		String home;
		if (args.length == 0)
		{
			List<String> homes = warpRepository.GetPrivateList(player.getName());
			if (homes.isEmpty())
			{
				target.message = "You do not have any homes set.";
				return target;
			}
			if (homes.size() == 1)
				home = homes.get(0);
			else
			{
				target.message = String.format("Homes: %s", StringUtils.join(homes, ", "));
				return target;
			}
		}
		else
		{
			home = args[0];
		}
		target.location = warpRepository.GetPrivate(player.getName(), home);
		if (target.location == null)
		{
			target.message = String.format("You do not have a home named %s.", home);
		}
		else if (args.length > 1 && args[1].equals("-f"))
		{
			target.message = "Forced unsafe teleport.";
			target.force = true;
		}
		else if (!engine.targetFloorIsSafe(target.location, true))
		{
			target.location = null;
			target.message = String.format("That home may be unsafe! type \"/home %s -f\" to go there.", home);
		}
		return target;
	}

	@Override
	public PlayerTeleport OnAsyncExecute(RunsafePlayer player, HashMap<String, String> stringStringHashMap)
	{
		return null;
	}

	private final WarpRepository warpRepository;
}
