package no.runsafe.warpdrive.zones;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.networking.PacketSetBossBar;

public class SetZoneCommand extends PlayerAsyncCommand
{
	public SetZoneCommand(IScheduler scheduler)
	{
		super("setzone", "Set your current zone", "runsafe.zone.set", scheduler, new RequiredArgument("zone"));
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList list)
	{
		player.sendPacket(new PacketSetBossBar(list.get("zone")));
		return "Done!";
	}
}
