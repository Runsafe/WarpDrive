package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Database;
import no.runsafe.framework.features.Events;
import no.runsafe.warpdrive.commands.*;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
import no.runsafe.warpdrive.database.SmartWarpRepository;
import no.runsafe.warpdrive.database.WarpRepository;
import no.runsafe.warpdrive.portals.PortalEngine;
import no.runsafe.warpdrive.portals.PortalRepository;
import no.runsafe.warpdrive.smartwarp.SmartWarpDrive;
import no.runsafe.warpdrive.smartwarp.SmartWarpScanner;
import no.runsafe.warpdrive.smartwarp.SnazzyWarp;
import no.runsafe.warpdrive.summoningstone.EventHandler;
import no.runsafe.warpdrive.summoningstone.SummoningEngine;
import no.runsafe.warpdrive.summoningstone.SummoningStoneCleaner;
import no.runsafe.warpdrive.summoningstone.SummoningStoneRepository;
import no.runsafe.warpdrive.zones.SetZoneCommand;

public class WarpDrive extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		// Framework features
		addComponent(Commands.class);
		addComponent(Database.class);
		addComponent(Events.class);

		debug = getComponent(IDebug.class); // Set a debugger.

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
		addComponent(TeleportSelf.class);
		addComponent(TeleportOther.class);
		addComponent(WipeHomes.class);
		addComponent(TeleportToEntityID.class);
		addComponent(SmartWarp.class);
		addComponent(RescanSmartWarp.class);
		addComponent(SetPortal.class);
		addComponent(SetRegionPortal.class);
		addComponent(SetZoneCommand.class);

		addComponent(WarpSignCreator.class);

		// Portals
		addComponent(PortalRepository.class);
		addComponent(PortalEngine.class);

		// Summoning Stones
		addComponent(SummoningStoneRepository.class);
		addComponent(SummoningEngine.class);
		addComponent(SummoningStoneCleaner.class);
		addComponent(EventHandler.class);
	}

	public static IDebug debug;
}
