package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerAsyncCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
		ConfigurationSection section = configuration.getSection("private.max");
		privateWarpLimit.clear();
		for (String key : section.getKeys(false))
			privateWarpLimit.put(key, section.getInt(key));
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		List<String> homes = warpRepository.GetPrivateList(player.getName());
		if (!homes.contains(parameters.get("name")))
		{
			int limit = 0;
			for (String group : player.getGroups())
			{
				if (privateWarpLimit.containsKey(group))
					limit = Math.max(limit, privateWarpLimit.get(group));
			}
			if (limit == 0)
				limit = privateWarpLimit.get("default");
			if (homes.size() >= limit)
				return String.format("You are only allowed %d homes on this server.", limit);
		}

		warpRepository.Persist(player.getName(), parameters.get("name"), false, player.getLocation());
		return String.format("Current location saved as the home %s.", parameters.get("name"));
	}

	final WarpRepository warpRepository;
	final ConcurrentHashMap<String, Integer> privateWarpLimit = new ConcurrentHashMap<String, Integer>();
}
