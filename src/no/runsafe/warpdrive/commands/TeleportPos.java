package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.IntegerArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;

public class TeleportPos extends PlayerCommand
{
	public TeleportPos(Engine engine)
	{
		super(
			"teleportpos", "Teleports you to the given coordinates in your current world.", "runsafe.teleport.coordinate",
			new IntegerArgument("x", true), new IntegerArgument("y", true), new IntegerArgument("z", true)
		);
		this.engine = engine;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		Integer x = parameters.getValue("x");
		Integer y = parameters.getValue("y");
		Integer z = parameters.getValue("z");
		if (x == null || z == null)
			return "Invalid coordinate";
		ILocation target = player.getLocation();
		target.setX(x);
		target.setZ(z);
		if (y == null)
			target = engine.findTop(target);
		else
			target.setY(y);
		player.teleport(target);
		return null;
	}

	private final Engine engine;
}
