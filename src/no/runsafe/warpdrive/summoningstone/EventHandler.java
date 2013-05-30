package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.event.entity.IEntityPortalEnterEvent;
import no.runsafe.framework.event.player.IPlayerPortalEvent;
import no.runsafe.framework.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.entity.PassiveEntity;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.entity.RunsafeItem;
import no.runsafe.framework.server.event.entity.RunsafeEntityPortalEnterEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerPortalEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.item.meta.RunsafeBookMeta;
import no.runsafe.framework.server.item.meta.RunsafeItemMeta;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;

public class EventHandler implements IPlayerPortalEvent, IEntityPortalEnterEvent, IPlayerRightClickBlock
{
	public EventHandler(SummoningEngine engine, SummoningStoneRepository repository)
	{
		this.engine = engine;
		this.repository = repository;
	}

	@Override
	public void OnPlayerPortalEvent(RunsafePlayerPortalEvent event)
	{
		RunsafeLocation from = event.getFrom();

		if (from != null)
		{
			int stoneID = this.engine.getStoneAtLocation(from);
			if (stoneID > -1) event.setCancelled(true);
		}
	}

	@Override
	public void OnEntityPortalEnter(RunsafeEntityPortalEnterEvent event)
	{
		if (event.getBlock().getTypeId() == Material.ENDER_PORTAL.getId())
		{
			int stoneID = this.engine.getStoneAtLocation(event.getLocation());

			if (stoneID > -1)
			{
				RunsafeEntity entity = event.getEntity();
				if (entity.getEntityType() == PassiveEntity.DroppedItem)
				{
					RunsafeItemStack item = ((RunsafeItem) entity).getItemStack();
					if (item.getItemId() == Material.WRITTEN_BOOK.getId())
					{
						RunsafeItemMeta meta = item.getItemMeta();

						if (meta != null)
							if (meta instanceof RunsafeBookMeta)
								RunsafeServer.Instance.broadcastMessage(((RunsafeBookMeta) meta).getAuthor());
					}
				}
				entity.remove();
			}
		}
	}

	@Override
	public boolean OnPlayerRightClick(RunsafePlayer runsafePlayer, RunsafeItemStack itemStack, RunsafeBlock runsafeBlock)
	{
		// Player has set fire to emerald block?
		if (itemStack.getType() == Material.FLINT_AND_STEEL && runsafeBlock.getTypeId() == Material.EMERALD_BLOCK.getId())
		{
			RunsafeLocation stoneLocation = runsafeBlock.getLocation();
			if (SummoningStone.isSummoningStone(stoneLocation))
			{
				int stoneID = this.repository.addSummoningStone(stoneLocation);
				SummoningStone summoningStone = new SummoningStone(stoneLocation);
				summoningStone.activate();
				summoningStone.setTimerID(this.engine.registerExpireTimer(stoneID));

				this.engine.registerStone(stoneID, summoningStone);
				return false;
			}
		}
		return true;
	}

	private SummoningEngine engine;
	private SummoningStoneRepository repository;
}
