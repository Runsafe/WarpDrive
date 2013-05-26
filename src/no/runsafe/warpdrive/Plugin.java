package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.warpdrive.commands.*;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
import no.runsafe.warpdrive.database.SmartWarpRepository;
import no.runsafe.warpdrive.database.WarpRepository;
import no.runsafe.warpdrive.portals.PortalEngine;
import no.runsafe.warpdrive.portals.PortalRepository;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Engine.class);

		// Database
		addComponent(WarpRepository.class);
		addComponent(SmartWarpChunkRepository.class);
		addComponent(SmartWarpRepository.class);

		// Warping
		addComponent(SmartWarpScanner.class);
		addComponent(SmartWarpDrive.class);
		addComponent(SnazzyWarp.class);

		// Commands
		addComponent(SetWarp.class);
		addComponent(DelWarp.class);
		addComponent(Warp.class);
		addComponent(SetHome.class);
		addComponent(DelHome.class);
		addComponent(Home.class);
		addComponent(Top.class);
		addComponent(TeleportPos.class);
		addComponent(Teleport.class);
		addComponent(WipeHomes.class);
		addComponent(TeleportToEntityID.class);
		addComponent(SmartWarp.class);

		addComponent(WarpSignCreator.class);

		// Portals
		addComponent(PortalRepository.class);
		addComponent(PortalEngine.class);
	}
}
