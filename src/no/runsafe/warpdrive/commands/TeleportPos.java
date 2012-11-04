package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.ICommand;
import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;

import java.util.Collection;

public class TeleportPos extends RunsafePlayerCommand
{
	public TeleportPos()
	{
		super("teleportpos", "x", "y", "z");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.teleport.coordinate";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		Double x = Double.valueOf(getArg("x"));
		Double y;
		try
		{
			y = Double.valueOf(getArg("y"));
		}
		catch (NumberFormatException e)
		{
			y = Double.NaN;
		}
		Double z = Double.valueOf(getArg("z"));
		if (x.isNaN() || z.isNaN())
			return "Invalid coordinate";
		RunsafeLocation target = executor.getLocation();
		target.setX(x);
		target.setZ(z);
		if (y.isNaN())
			target = StaticWarp.findTop(target);
		else
			target.setY(y);
		executor.teleport(target);
		return null;
	}
}
