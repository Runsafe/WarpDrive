package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerAsyncCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.HashMap;
import java.util.List;

public class SetHome extends PlayerAsyncCommand implements IConfigurationChanged
{
	public SetHome(IScheduler scheduler, WarpRepository repository)
	{
		super("sethome", "Saves your current location as a home", "runsafe.home.set", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		privateWarpLimit = configuration.getConfigValueAsInt("private.max");
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		if (privateWarpLimit > 0)
		{
			List<String> homes = warpRepository.GetPrivateList(player.getName());
			if (!homes.contains(parameters.get("name")) && homes.size() >= privateWarpLimit)
				return String.format("You are only allowed %d homes on this server.", privateWarpLimit);
		}
		warpRepository.Persist(player.getName(), parameters.get("name"), false, player.getLocation());
		return String.format("Current location saved as the home %s.", parameters.get("name"));
	}

	final WarpRepository warpRepository;
	int privateWarpLimit;
}
