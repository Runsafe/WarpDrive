package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.ITabComplete;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.database.WarpRepository;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class HomeOther extends PlayerTeleportCommand
{
	public HomeOther(WarpRepository repository, IScheduler scheduler, Engine engine)
	{
		super("homeother", "Teleports you to someone elses house", "runsafe.home.other.use", scheduler, engine, new Player().require(), new HomeArgument(repository));
		warpRepository = repository;
	}

	public static class HomeArgument extends OptionalArgument implements ITabComplete
	{
		public HomeArgument(WarpRepository warpRepository)
		{
			super("home");
			this.warpRepository = warpRepository;
		}

		@Override
		public List<String> getAlternatives(IPlayer player, String arg)
		{
			return warpRepository.GetPrivateList(player);
		}

		private final WarpRepository warpRepository;
	}

	@Override
	public PlayerTeleportCommand.PlayerTeleport OnAsyncExecute(IPlayer player, IArgumentList params)
	{
		PlayerTeleport target = new PlayerTeleport();
		target.player = player;
		String home = params.getValue("home");

		IPlayer otherPlayer = params.getValue("player");
		if (otherPlayer == null)
		{
			target.message = "&cInvalid player.";
			return target;
		}

		if (home == null)
		{
			List<String> homes = warpRepository.GetPrivateList(otherPlayer);
			if (homes.isEmpty())
			{
				target.message = "&cThat player does not have any homes.";
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

		target.location = warpRepository.GetPrivate(otherPlayer, home);
		if (target.location == null)
			target.message = String.format("&cHome %s is in an invalid location.", home);
		else
			target.force = true;
		return target;
	}

	private final WarpRepository warpRepository;
}
