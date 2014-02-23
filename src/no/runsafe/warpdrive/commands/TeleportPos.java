package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.IntegerArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class TeleportPos extends PlayerCommand implements IBranchingExecution
{
	public TeleportPos()
	{
		super(
			"teleportpos", "Teleport to a x,y,z coordinate in your current world.", "runsafe.teleport.coordinate",
			new IntegerArgument("x", true), new IntegerArgument("y", true), new IntegerArgument("z", true)
		);
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		Integer x = parameters.getValue("x");
		Integer y = parameters.getValue("y");
		Integer z = parameters.getValue("z");
		if (x == null || y == null || z == null)
			return "Invalid coordinate";
		ILocation target = player.getLocation();
		target.setX(x);
		target.setZ(z);
		target.setY(y);
		player.teleport(target);
		return null;
	}
}
