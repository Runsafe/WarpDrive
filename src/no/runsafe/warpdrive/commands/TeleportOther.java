package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;

public class TeleportOther extends ExecutableCommand implements IContextPermissionProvider, IBranchingExecution
{
	public TeleportOther(Engine engine, IScheduler scheduler)
	{
		super(
			"teleport", "Teleport player1 to player2", null,
			new Player("player1").onlineOnly().require(), new Player("player2").onlineOnly().require()
		);
		this.engine = engine;
		this.warned = new TimedCache<>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, IArgumentList parameters, String[] args)
	{
		if (executor instanceof IPlayer)
		{
			IPlayer target = parameters.getValue("player2");

			if (target == null)
				target = parameters.getValue("player1");
			if (target == null)
				return null;

			return "runsafe.teleport.world." + target.getWorldName();
		}
		return null;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer move = parameters.getValue("player1");
		IPlayer to = parameters.getValue("player2");
		if (move == null || to == null)
			return null;

		IPlayer warning = warned.Cache(move);
		boolean force = warning != null && warning.equals(to);

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
			return String.format("&aPerformed unsafe teleport of %s &ato %s&a.", move.getPrettyName(), to.getPrettyName());
		}
		if (engine.safePlayerTeleport(to.getLocation(), move))
			return String.format("&aSafely teleported %1$s&a to %2$s&a.", move.getPrettyName(), to.getPrettyName());

		warned.Cache(move, to);
		return String.format("&cUnable to safely teleport %1$s to %2$s&c, repeat command to force.", move.getPrettyName(), to.getPrettyName());
	}

	private final Engine engine;
	private final TimedCache<IPlayer, IPlayer> warned;
}
