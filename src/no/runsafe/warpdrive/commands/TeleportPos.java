package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WholeNumber;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

import static java.lang.Math.abs;

public class TeleportPos extends PlayerCommand implements IBranchingExecution
{
	public TeleportPos()
	{
		super(
			"teleportpos", "Teleport to a x,y,z coordinate in your current world.", "runsafe.teleport.coordinate",
			new WholeNumber("x").require(), new WholeNumber("y").require(), new WholeNumber("z").require()
		);
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		int x = parameters.getRequired("x");
		int y = parameters.getRequired("y");
		int z = parameters.getRequired("z");
		if (abs(x) > 30000000 || y > 255 || y < 0 || abs(z) > 30000000)
			return "&cOutside the world boundaries.";
		ILocation target = player.getLocation();
		if (target == null)
		{
			return null;
		}
		target.setX(x);
		target.setZ(z);
		target.setY(y);
		player.teleport(target);
		return null;
	}
}
