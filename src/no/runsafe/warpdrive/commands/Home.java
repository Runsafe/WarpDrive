package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.database.WarpRepository;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class Home extends PlayerTeleportCommand
{
	public Home(WarpRepository repository, IScheduler scheduler, Engine engine)
	{
		super(
			"home",
			"Teleports you to a home location",
			"runsafe.home.use",
			scheduler,
			engine,
			new HomeArgument(HOME_NAME, repository)
		);
		warpRepository = repository;
	}

	private static final String HOME_NAME = "home";

	@Override
	public PlayerTeleportCommand.PlayerTeleport OnAsyncExecute(IPlayer player, IArgumentList params)
	{
		PlayerTeleport target = new PlayerTeleport();
		target.player = player;
		String home = params.getValue(HOME_NAME);
		if (home == null)
		{
			List<String> homes = warpRepository.GetPrivateList(player);
			if (homes.isEmpty())
			{
				target.message = "&cYou do not have any homes set.";
				return target;
			}
			if (homes.size() == 1)
				home = homes.get(0);
			else
			{
				target.message = String.format("&2&lHomes:&r %s", StringUtils.join(homes, ", "));
				return target;
			}
		}

		target.location = warpRepository.GetPrivate(player, home);
		if (target.location == null)
			target.message = String.format("&cHome %s is in an invalid location.", home);
		else
			target.force = true;
		return target;
	}

	private final WarpRepository warpRepository;
}
