package no.runsafe.warpdrive;

import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.ISignChange;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.commands.Warp;
import no.runsafe.warpdrive.smartwarp.SnazzyWarp;

public class WarpSignCreator implements ISignChange
{
	public WarpSignCreator(IConsole console)
	{
		this.console = console;
	}

	@Override
	public boolean OnSignChange(IPlayer player, IBlock runsafeBlock, String[] strings)
	{
		String head = strings[0].toLowerCase();
		if (head.contains(SnazzyWarp.signTag) || head.contains(SnazzyWarp.signHeader.toLowerCase()))
		{
			if (!player.hasPermission("runsafe.snazzysign.create"))
				return false;

			//Check for valid input for outer and inner radius
			try
			{
				if (Integer.parseInt(strings[2]) < 0 || Integer.parseInt(strings[3]) < 0)
				{
					player.sendColouredMessage("&2cInvalid warp sign. Outer (top) and Inner (bottom) radius must be a positive integer.");
					return false;
				}
			}
			catch (NumberFormatException exception)
			{
				player.sendColouredMessage("&2cInvalid warp sign. Outer (top) and Inner (bottom) radius must be a positive integer.");
				return false;
			}

			player.sendColouredMessage("&aSnazzy warp sign created for world: &r%s", strings[1]);
			console.logInformation("%s created a snazzy warp sign named %s.", player.getPrettyName(), strings[1]);
			strings[0] = SnazzyWarp.signHeader;
			return true;
		}

		if (head.contains(Warp.signTag) || head.contains(Warp.signHeader.toLowerCase()))
		{
			if (player.hasPermission("runsafe.warpsign.create"))
			{
				console.logInformation("%s created a warp sign for the warp %s.", player.getPrettyName(), strings[1]);
				strings[0] = Warp.signHeader;
				return true;
			}
			return false;
		}
		return true;
	}

	private final IConsole console;
}
