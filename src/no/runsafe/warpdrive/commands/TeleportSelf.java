package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;

public class TeleportSelf extends PlayerCommand implements IContextPermissionProvider, IBranchingExecution
{
	public TeleportSelf(Engine engine, IScheduler scheduler)
	{
		super(
			"teleport", "Teleport to another player", null,
			new Player().onlineOnly().require()
		);
		this.engine = engine;
		this.warned = new TimedCache<>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, IArgumentList parameters, String[] args)
	{
		if (executor instanceof IPlayer)
		{
			IPlayer target = parameters.getValue("player");
			if (target == null)
				return null;
			return "runsafe.teleport.world." + target.getWorldName();
		}
		return null;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		IPlayer to = parameters.getValue("player");
		if (to == null)
			return null;

		IPlayer warning = warned.Cache(player);
		boolean force = warning != null && warning.equals(to);

		ILocation targetLocation = to.getLocationBehindPlayer(2);
		if (targetLocation == null)
		{
			return null;
		}
		if (to.getWorldName().equals(player.getWorldName()))
		{
			if (to.isCreative() && player.isCreative())
			{
				player.teleport(targetLocation);
				return null;
			}
		}
		if (force)
		{
			player.teleport(targetLocation);
			return String.format("&cPerformed unsafe teleport to %s.", to.getPrettyName());
		}
		if (engine.safePlayerTeleport(targetLocation, player))
			return null;

		warned.Cache(player, to);
		return String.format("&cUnable to safely teleport you to %1$s&c, repeat command to force.", to.getPrettyName());
	}

	private final Engine engine;
	private final TimedCache<IPlayer, IPlayer> warned;
}
