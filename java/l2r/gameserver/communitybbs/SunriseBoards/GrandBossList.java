package l2r.gameserver.communitybbs.SunriseBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2r.L2DatabaseFactory;
import l2r.gameserver.data.sql.NpcTable;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class GrandBossList
{
	private final StringBuilder _GrandBossList = new StringBuilder();
	
	public GrandBossList()
	{
		loadFromDB();
	}
	
	private void loadFromDB()
	{
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT boss_id, status FROM grandboss_data");
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				int npcid = result.getInt("boss_id");
				int status = result.getInt("status");
				if ((npcid == 29066) || (npcid == 29067) || (npcid == 29068) || (npcid == 29019))
				{
					continue;
				}
				
				pos++;
				String name = NpcTable.getInstance().getTemplate(npcid).getName();
				boolean rstatus = status == 0;
				addGrandBossToList(pos, name, rstatus);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addGrandBossToList(int pos, String npcname, boolean rstatus)
	{
		_GrandBossList.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=111111 width=835>");
		_GrandBossList.append("<tr>");
		_GrandBossList.append("<td FIXWIDTH=30>" + pos + "</td>");
		_GrandBossList.append("<td FIXWIDTH=30>" + npcname + "</td>");
		_GrandBossList.append("<td FIXWIDTH=30 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_GrandBossList.append("</tr>");
		_GrandBossList.append("</table>");
		_GrandBossList.append("<img src=\"L2UI.Squaregray\" width=\"735\" height=\"1\">");
	}
	
	public String loadGrandBossList()
	{
		return _GrandBossList.toString();
	}
}
