package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;

public class TeleportOther extends ExecutableCommand implements IContextPermissionProvider, IBranchingExecution
{
	public TeleportOther(Engine engine, IScheduler scheduler)
	{
		super(
			"teleport", "Teleport player1 to player2", null,
			new OnlinePlayerRequired("player1"), new OnlinePlayerRequired("player2")
		);
		this.engine = engine;
		this.warned = new TimedCache<String, String>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, IArgumentList parameters, String[] args)
	{
		if (executor instanceof IPlayer)
		{
			IPlayer target;
			if (!parameters.containsKey("player2"))
				target = parameters.getPlayer("player1");
			else
				target = parameters.getPlayer("player2");
			if (target == null)
				return null;

			return "runsafe.teleport.world." + target.getWorldName();
		}
		return null;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer move = parameters.getPlayer("player1");
		IPlayer to = parameters.getPlayer("player2");
		if (move == null || to == null)
			return null;

		String warning = warned.Cache(move.getName());
		boolean force = warning != null && warning.equals(to.getName());

		if (to.getWorldName().equals(move.getWorldName()))
		{
			if (to.isCreative() && move.isCreative())
			{
				move.teleport(to.getLocation());
				return null;
			}
		}
		if (force)
		{
			move.teleport(to.getLocation());
			return String.format("Performed unsafe teleport of %s to %s.", move.getPrettyName(), to.getPrettyName());
		}
		if (engine.safePlayerTeleport(to.getLocation(), move))
			return String.format("Safely teleported %1$s to %2$s.", move.getPrettyName(), to.getPrettyName());

		warned.Cache(move.getName(), to.getName());
		return String.format("Unable to safely teleport %1$s to %2$s, repeat command to force.", move.getPrettyName(), to.getPrettyName());
	}

	private final Engine engine;
	private final TimedCache<String, String> warned;
}
