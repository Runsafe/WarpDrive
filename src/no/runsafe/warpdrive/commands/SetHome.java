package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.List;

public class SetHome extends RunsafeAsyncPlayerCommand implements IConfigurationChanged
{
	public SetHome(IScheduler scheduler, WarpRepository repository)
	{
		super("sethome", scheduler, "name");
		warpRepository = repository;
	}


	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		privateWarpLimit = configuration.getConfigValueAsInt("private.max");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.set";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		if(privateWarpLimit > 0)
		{
			List<String> homes = warpRepository.GetPrivateList(player.getName());
			if(!homes.contains(getArg("name")) && homes.size() >= privateWarpLimit)
				return String.format("You are only allowed %d homes on this server.", privateWarpLimit);
		}
		warpRepository.Persist(player.getName(), getArg("name"), false, player.getLocation());
		return String.format("Current location saved as the home %s.", getArg("name"));
	}

	final WarpRepository warpRepository;
	int privateWarpLimit;
}
