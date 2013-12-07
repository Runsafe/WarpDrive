package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.warpdrive.Engine;

import java.util.Map;

public class Top extends PlayerCommand
{
	public Top(Engine engine)
	{
		super("top", "Teleports you to the surface", "runsafe.teleport.top");
		this.engine = engine;
	}

	@Override
	public String OnExecute(IPlayer player, Map<String, String> parameters)
	{
		RunsafeLocation top = engine.findTop(player.getLocation());
		top.setY(top.getY() + 1);
		player.teleport(top);
		return null;
	}

	private final Engine engine;
}
