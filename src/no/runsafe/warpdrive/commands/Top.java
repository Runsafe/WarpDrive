package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.Engine;

import java.util.HashMap;

public class Top extends PlayerCommand
{
	public Top(Engine engine)
	{
		super("top", "Teleports you to the surface", "runsafe.teleport.top");
		this.engine = engine;
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		RunsafeLocation top = engine.findTop(player.getLocation());
		top.setY(top.getY() + 1);
		player.teleport(top);
		return null;
	}

	private final Engine engine;
}
