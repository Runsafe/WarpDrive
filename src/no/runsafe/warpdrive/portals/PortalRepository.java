package no.runsafe.warpdrive.portals;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.Repository;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalRepository extends Repository
{
	public PortalRepository(IDatabase database)
	{
		this.database = database;
	}

	public String getTableName()
	{
		return "warpdrive_portals";
	}

	public List<PortalWarp> getPortalWarps()
	{
		List<PortalWarp> warps = new ArrayList<PortalWarp>();

		List<Map<String, Object>> data = this.database.Query(
			"SELECT `ID`,`permission`,`type`,`world`,`x`,`y`,`z`,`destWorld`,`destX`,`destY`,`destZ`,`destYaw`,`destPitch` " +
				"FROM warpdrive_portals"
		);

		for (Map<String, Object> row : data)
		{
			warps.add(new PortalWarp(
				(String)row.get("ID"),
				new RunsafeLocation(
					RunsafeServer.Instance.getWorld((String) row.get("world")),
					(Double)row.get("x"),
					(Double)row.get("y"),
					(Double)row.get("z")
				),
				RunsafeServer.Instance.getWorld((String)row.get("destWorld")),
				(Double)row.get("destX"),
				(Double)row.get("destY"),
				(Double)row.get("destZ"),
				(Float)row.get("destYaw"),
				(Float)row.get("destPitch"),
				PortalType.getPortalType((Integer) row.get("type"))
			));
		}
		return warps;
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE `warpdrive_portals` (" +
				"`ID` VARCHAR(50) NOT NULL," +
				"`permission` VARCHAR(255) NOT NULL DEFAULT ''," +
				"`type` TINYINT NOT NULL DEFAULT '0'," +
				"`world` VARCHAR(255) NOT NULL," +
				"`x` DOUBLE NOT NULL," +
				"`y` DOUBLE NOT NULL," +
				"`z` DOUBLE NOT NULL," +
				"`destWorld` VARCHAR(255) NOT NULL," +
				"`destX` DOUBLE NULL," +
				"`destY` DOUBLE NULL," +
				"`destZ` DOUBLE NULL," +
				"`destYaw` DOUBLE NULL," +
				"`destPitch` DOUBLE NULL," +
				"PRIMARY KEY (`ID`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	private IDatabase database;
}
