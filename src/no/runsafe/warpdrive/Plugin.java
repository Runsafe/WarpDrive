package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.warpdrive.commands.*;
import no.runsafe.warpdrive.database.WarpRepository;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(WarpRepository.class);
		addComponent(SnazzyWarp.class);
		addComponent(SetWarp.class);
		addComponent(DelWarp.class);
		addComponent(Warp.class);
		addComponent(SetHome.class);
		addComponent(DelHome.class);
		addComponent(Home.class);
		addComponent(Top.class);
	}
}
