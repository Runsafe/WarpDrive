package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;

import java.util.Map;

public class TeleportPos extends PlayerCommand
{
	public TeleportPos(Engine engine)
	{
		super(
			"teleportpos", "Teleports you to the given coordinates in your current world.", "runsafe.teleport.coordinate",
			new RequiredArgument("x"), new RequiredArgument("y"), new RequiredArgument("z")
		);
		this.engine = engine;
	}

	@Override
	public String OnExecute(IPlayer player, Map<String, String> parameters)
	{
		Double x = Double.valueOf(parameters.get("x"));
		Double y;
		try
		{
			y = Double.valueOf(parameters.get("y"));
		}
		catch (NumberFormatException e)
		{
			y = Double.NaN;
		}
		Double z = Double.valueOf(parameters.get("z"));
		if (x.isNaN() || z.isNaN())
			return "Invalid coordinate";
		ILocation target = player.getLocation();
		target.setX(x);
		target.setZ(z);
		if (y.isNaN())
			target = engine.findTop(target);
		else
			target.setY(y);
		player.teleport(target);
		return null;
	}

	private final Engine engine;
}
