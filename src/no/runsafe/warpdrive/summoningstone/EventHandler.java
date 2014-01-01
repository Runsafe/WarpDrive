package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.entity.IEntityPortalEnterEvent;
import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.event.player.IPlayerPortalEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.minecraft.RunsafeEntityType;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.PassiveEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.entity.RunsafeItem;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityPortalEnterEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerPortalEvent;
import no.runsafe.framework.minecraft.item.RunsafeItemStack;
import no.runsafe.framework.minecraft.item.meta.RunsafeBook;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.warpdrive.WarpDrive;
import org.bukkit.Effect;

public class EventHandler implements IPlayerPortalEvent, IEntityPortalEnterEvent, IPlayerRightClickBlock, IPlayerJoinEvent
{
	public EventHandler(SummoningEngine engine, SummoningStoneRepository repository)
	{
		this.engine = engine;
		this.repository = repository;
	}

	@Override
	public void OnPlayerPortalEvent(RunsafePlayerPortalEvent event)
	{
		ILocation from = event.getFrom();

		if (from != null)
		{
			int stoneID = engine.getStoneAtLocation(from);
			if (stoneID > -1) event.cancel();
		}
	}

	@Override
	public void OnEntityPortalEnter(RunsafeEntityPortalEnterEvent event)
	{
		if (event.getBlock().is(Item.Unavailable.EnderPortal))
		{
			ILocation location = event.getLocation();
			int stoneID = engine.getStoneAtLocation(location);

			if (stoneID > -1)
			{
				RunsafeEntity entity = event.getEntity();
				RunsafeEntityType type = entity.getEntityType();
				if (type == PassiveEntity.DroppedItem)
				{
					RunsafeItemStack item = ((RunsafeItem) entity).getItemStack();
					if (item.is(Item.Special.Crafted.WrittenBook))
					{
						RunsafeBook book = (RunsafeBook) item;
						IWorld world = location.getWorld();
						engine.registerPendingSummon(book.getAuthor(), stoneID);
						world.playEffect(location, Effect.GHAST_SHRIEK, 0);
						ILocation target = location.clone();
						target.offset(0.5, 0, 0.5);
						world.createExplosion(target, 0, false, false);
					}
				}

				if (type != LivingEntity.Player)
					entity.remove();
			}
		}
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer runsafePlayer, RunsafeMeta itemStack, IBlock runsafeBlock)
	{
		if (itemStack == null)
			return true;

		WarpDrive.debug.debugFine("Detected right click event from player: " + runsafePlayer.getName());

		if ((itemStack.is(Item.Tool.FlintAndSteel) || itemStack.is(Item.Miscellaneous.FireCharge)) && runsafeBlock.is(Item.BuildingBlock.Emerald))
		{
			WarpDrive.debug.debugFine("Detected FLINT_AND_STEEL click on EMERALD_BLOCK");
			ILocation stoneLocation = runsafeBlock.getLocation();
			if (engine.canCreateStone(stoneLocation.getWorld()) && SummoningStone.isSummoningStone(stoneLocation))
			{
				WarpDrive.debug.debugFine("Location is safe to create a summoning stone.");
				int stoneID = repository.addSummoningStone(stoneLocation);
				SummoningStone summoningStone = new SummoningStone(stoneLocation);
				summoningStone.activate();
				summoningStone.setTimerID(engine.registerExpireTimer(stoneID));

				engine.registerStone(stoneID, summoningStone);
				return false;
			}
		}
		else if (itemStack.is(Item.Miscellaneous.EyeOfEnder) && runsafeBlock.is(Item.Decoration.EnderPortalFrame))
		{
			if (engine.isRitualWorld(runsafePlayer.getWorld()))
			{
				if (engine.playerHasPendingSummon(runsafePlayer))
				{
					engine.acceptPlayerSummon(runsafePlayer);
					runsafePlayer.removeItem(Item.Miscellaneous.EyeOfEnder, 1);
				}
				else
				{
					runsafePlayer.sendColouredMessage("&cYou have no pending summons to accept.");
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		IPlayer player = event.getPlayer();
		if (engine.playerHasPendingSummon(player))
			player.sendColouredMessage("&3You have a pending summon, head to the ritual stone to accept.");
	}

	private final SummoningEngine engine;
	private final SummoningStoneRepository repository;
}
