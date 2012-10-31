package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.commands.*;
import no.runsafe.warpdrive.database.WarpRepository;
import org.bukkit.Material;
import org.bukkit.World;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		IConfiguration config = getComponent(IConfiguration.class);

		addComponent(WarpRepository.class);

		if (config.getConfigValueAsBoolean("snazzy.enable"))
			addComponent(SnazzyWarp.class);

		if (config.getConfigValueAsBoolean("public.enable"))
		{
			addComponent(SetWarp.class);
			addComponent(DelWarp.class);
			addComponent(Warp.class);
		}
		if (config.getConfigValueAsBoolean("private.enable"))
		{
			addComponent(SetHome.class);
			addComponent(DelHome.class);
			addComponent(Home.class);
		}
	}
}
