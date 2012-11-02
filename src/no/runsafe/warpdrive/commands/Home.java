package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;
import no.runsafe.warpdrive.database.WarpRepository;

public class Home extends RunsafePlayerCommand
{
	public Home(WarpRepository repository, IOutput output)
	{
		super("home", "destination");
		warpRepository = repository;
		console = output;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.use";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		RunsafeLocation destination = warpRepository.GetPrivate(player.getName(), getArg("destination"));
		if (destination == null)
			return String.format("You do not have a home named %s.", getArg("destination"));
		if (strings.length > 0 && strings[0].equals("-f"))
		{
			player.teleport(destination);
			return String.format("Doing unsafe teleport to home %s", getArg("destination"));
		}
		StaticWarp.safePlayerTeleport(destination, player, false);
		return null;
	}

	WarpRepository warpRepository;
	IOutput console;
}
