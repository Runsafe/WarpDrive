package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;

import java.util.HashMap;

public class TeleportPos extends PlayerCommand
{
	public TeleportPos()
	{
		super("teleportpos", "Teleports you to the given coordinates in your current world.", "runsafe.teleport.coordinate", "x", "y", "z");
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters, String[] args)
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
		RunsafeLocation target = player.getLocation();
		target.setX(x);
		target.setZ(z);
		if (y.isNaN())
			target = StaticWarp.findTop(target);
		else
			target.setY(y);
		player.teleport(target);
		return null;
	}
}
