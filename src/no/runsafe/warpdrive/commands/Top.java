package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;

public class Top extends PlayerCommand
{
	public Top(Engine engine)
	{
		super("top", "Teleports you to the surface", "runsafe.teleport.top");
		this.engine = engine;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		ILocation playerLocation = player.getLocation();
		if (playerLocation == null)
		{
			return null;
		}
		ILocation top = engine.findTop(playerLocation);
		top.setY(top.getY() + 1);
		player.teleport(top);
		return null;
	}

	private final Engine engine;
}
