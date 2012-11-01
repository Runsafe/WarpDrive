package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.StaticWarp;

public class Top extends RunsafeAsyncPlayerCommand
{
	public Top(IScheduler scheduler)
	{
		super("top", scheduler);
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.teleport.top";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		RunsafeLocation top = StaticWarp.findTop(player.getLocation());
		top.setY(top.getY() + 1);
		player.teleport(top);
		return null;
	}
}
