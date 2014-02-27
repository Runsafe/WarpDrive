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
		super("homeother", "Teleports you to someone elses house", "runsafe.home.other.use", scheduler, engine, new Player.Any.Required(), new HomeArgument(repository));
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
			return warpRepository.GetPrivateList(player.getName());
		}

		private final WarpRepository warpRepository;
	}

	@Override
	public PlayerTeleportCommand.PlayerTeleport OnAsyncExecute(IPlayer player, IArgumentList params)
	{
		PlayerTeleport target = new PlayerTeleport();
		target.player = player;
		String home;

		IPlayer otherPlayer = params.getValue("player");
		if (otherPlayer == null)
		{
			target.message = "Invalid player.";
			return target;
		}

		if (!params.containsKey("home"))
		{
			List<String> homes = warpRepository.GetPrivateList(otherPlayer.getName());
			if (homes.isEmpty())
			{
				target.message = "That player does not have any homes.";
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
			home = params.get("home");
		target.location = warpRepository.GetPrivate(otherPlayer.getName(), home);
		if (target.location == null)
			target.message = String.format("That player does not have a home named %s.", home);
		else
			target.force = true;
		return target;
	}

	private final WarpRepository warpRepository;
}
