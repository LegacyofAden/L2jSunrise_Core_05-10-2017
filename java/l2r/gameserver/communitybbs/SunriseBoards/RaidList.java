package l2r.gameserver.communitybbs.SunriseBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import l2r.L2DatabaseFactory;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;

import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class RaidList
{
	private final StringBuilder _raidList = new StringBuilder();
	
	public RaidList(String rfid)
	{
		loadFromDB(rfid);
	}
	
	private void loadFromDB(String rfid)
	{
		int type = Integer.parseInt(rfid);
		int stpoint = 0;
		int pos = 0;
		int tempCounter = 0;
		
		for (int count = 1; count != type; count++)
		{
			stpoint += SmartCommunityConfigs.RAID_LIST_RESULTS;
		}
		
		pos = stpoint;
		List<L2NpcTemplate> raids = new ArrayList<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT boss_id FROM raidboss_spawnlist");
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				raids.add(NpcTable.getInstance().getTemplate(result.getInt("boss_id")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		raids.sort((o1, o2) -> NpcTable.getInstance().getTemplate(o1.getId()).getLevel() - NpcTable.getInstance().getTemplate(o2.getId()).getLevel());
		
		for (int i = stpoint; i < raids.size(); i++)
		{
			L2NpcTemplate npc = raids.get(i);
			int npcid = npc.getId();
			String npcname = npc.getName();
			int rlevel = npc.getLevel();
			if (tempCounter >= SmartCommunityConfigs.RAID_LIST_RESULTS)
			{
				break;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT respawn_time, respawn_delay, respawn_random FROM raidboss_spawnlist WHERE boss_id=" + npcid);
				ResultSet result = statement.executeQuery();
				
				while (result.next())
				{
					pos++;
					tempCounter++;
					long respawn = result.getLong("respawn_time");
					boolean rstatus = respawn == 0;
					int mindelay = result.getInt("respawn_delay");
					int maxdelay = result.getInt("respawn_random");
					mindelay = mindelay / 60 / 60;
					maxdelay = maxdelay / 60 / 60;
					addRaidToList(pos, npcname, rlevel, mindelay, maxdelay, rstatus);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void addRaidToList(int pos, String npcname, int rlevel, int mindelay, int maxdelay, boolean rstatus)
	{
		_raidList.append("<table border=0 cellspacing=0 cellpadding=2  bgcolor=111111 width=750 height=" + SmartCommunityConfigs.RAID_LIST_ROW_HEIGHT + ">");
		_raidList.append("<tr>");
		_raidList.append("<td FIXWIDTH=5></td>");
		_raidList.append("<td FIXWIDTH=20>" + pos + "</td>");
		_raidList.append("<td FIXWIDTH=270>" + npcname + "</td>");
		_raidList.append("<td FIXWIDTH=50>" + rlevel + "</td>");
		_raidList.append("<td FIXWIDTH=120 align=center>" + mindelay + " - " + maxdelay + "</td>");
		_raidList.append("<td FIXWIDTH=50 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_raidList.append("<td FIXWIDTH=5></td>");
		_raidList.append("</tr>");
		_raidList.append("</table>");
		_raidList.append("<img src=\"L2UI.Squaregray\" width=\"735\" height=\"1\">");
	}
	
	public String loadRaidList()
	{
		return _raidList.toString();
	}
}
