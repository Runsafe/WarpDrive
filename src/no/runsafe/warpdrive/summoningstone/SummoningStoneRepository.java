package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.database.*;
import no.runsafe.warpdrive.WarpDrive;

import java.util.ArrayList;
import java.util.List;

public class SummoningStoneRepository extends Repository
{
	public String getTableName()
	{
		return "summoningStones";
	}

	public List<ILocation> getStoneList()
	{
		List<ILocation> stones = new ArrayList<ILocation>();
		for (IRow node : database.query("SELECT world, x, y, z FROM summoningStones"))
			stones.add(node.Location());
		return stones;
	}

	public void wipeStoneList()
	{
		database.execute("DELETE FROM summoningStones");
	}

	public void deleteSummoningStone(int ID)
	{
		WarpDrive.debug.debugFine("Delete stone %s from the database.", ID);
		database.execute("DELETE FROM summoningStones WHERE ID = ?", ID);
	}

	public int addSummoningStone(ILocation location)
	{
		ITransaction transaction = database.isolate();

		transaction.execute(
			"INSERT INTO summoningStones (world, x, y, z) VALUES(?, ?, ?, ?)",
			location.getWorld().getName(),
			location.getX(),
			location.getY(),
			location.getZ()
		);
		Integer id = transaction.queryInteger("SELECT LAST_INSERT_ID()");
		transaction.Commit();
		return id == null ? 0 : id;
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `summoningStones` (" +
				"`ID` int(10) NOT NULL AUTO_INCREMENT," +
				"`world` VARCHAR(255) NOT NULL," +
				"`x` DOUBLE NOT NULL," +
				"`y` DOUBLE NOT NULL," +
				"`z` DOUBLE NOT NULL," +
				"PRIMARY KEY (`ID`)" +
			")"
		);

		return update;
	}
}
