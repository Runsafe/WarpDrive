package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;
import no.runsafe.warpdrive.database.WarpRepository;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class Home extends RunsafePlayerCommand
{
	public Home(WarpRepository repository, IOutput output, IConfiguration config)
	{
		super("home");
		warpRepository = repository;
		console = output;
		configuration = config;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.use";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		String home;
		if(strings.length == 0)
		{
			List<String> homes = warpRepository.GetPrivateList(player.getName());
			if(homes.isEmpty())
				return "You do not have any homes set.";
			if(homes.size() == 1)
				home = homes.get(0);
			else
				return String.format("Homes: %s", StringUtils.join(homes, ", "));
		}
		else
		{
			home = strings[0];
		}
		RunsafeLocation destination = warpRepository.GetPrivate(player.getName(), home);
		if (destination == null)
			return String.format("You do not have a home named %s.", home);
		if (strings.length > 1 && strings[1].equals("-f"))
		{
			player.teleport(destination);
			return "Forced unsafe teleport.";
		}
		StaticWarp.safePlayerTeleport(destination, player, false);
		return null;
	}

	WarpRepository warpRepository;
	IOutput console;
	IConfiguration configuration;
}
