package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.sr.achievementEngine.AchievementsHandler;
import gr.sr.achievementEngine.AchievementsManager;
import gr.sr.achievementEngine.base.Achievement;
import gr.sr.achievementEngine.base.Condition;
import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;
import gr.sr.main.Conditions;

public class L2AchievementManagerInstance extends L2Npc
{
	public L2AchievementManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// No null pointers
		if ((player == null))
		{
			return;
		}
		
		// Restrictions Section
		if (!Conditions.checkPlayerBasicConditions(player))
		{
			return;
		}
		
		if (command.startsWith("showMyAchievements"))
		{
			AchievementsHandler.getAchievemntData(player);
			showMyAchievements(player);
		}
		else if (command.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			showAchievementInfo(id, player);
		}
		else if (command.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			if (!AchievementsManager.checkConditions(id, player))
			{
				return;
			}
			
			AchievementsManager.getInstance().rewardForAchievement(id, player);
			AchievementsHandler.saveAchievementData(player, id);
			showMyAchievements(player);
		}
		else if (command.startsWith("showMyStats"))
		{
			showMyStats(player);
		}
		else if (command.startsWith("showMainWindow"))
		{
			showChatWindow(player, 0);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player == null)
		{
			return;
		}
		
		if (!CustomNpcsConfigs.ENABLE_ACHIEVEMENT_MANAGER)
		{
			player.sendMessage("Achievement manager is disabled by admin.");
			return;
		}
		
		if (player.getLevel() < CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL)
		{
			player.sendMessage("You need to be " + CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL + " level or higher to use my services.");
			return;
		}
		
		if (player.isInCombat())
		{
			player.sendMessage("Cannot use while in combat.");
			return;
		}
		
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html noscrollbar><title>Achievement Manager</title><body>");
		tb.append("<table width=285  height=358 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		tb.append("<tr><td valign=\"top\" align=\"center\">");
		tb.append("<table>");
		tb.append("<tr>");
		tb.append("<td><center>");
		tb.append("<table width=280><tr><td></td><td></td><td><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></td></tr><tr><td height=3></td></tr></table>");
		tb.append("<br><table width=275>");
		tb.append("<tr><td align=center><br><br><br>Are you looking for a <font color=\"D2B48C\">challenge?</font></td></tr>");
		tb.append("<tr><td align=center>Then this is your place,complete achievements</td></tr>");
		tb.append("<tr><td align=center>and recieve <font color=\"D2B48C\">rewards</font>. You can complete and be</td></tr>");
		tb.append("<tr><td align=center>rewarded only once for each <font color=\"D2B48C\">achievement.</font><br><br><br></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_showMyAchievements\" value=\"My Achievements\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"><br></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_showMyStats\" value=\"Statistics\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"><br><br></td></tr>");
		tb.append("</table>");
		tb.append("<table width=280><tr><td height=20></td></tr><tr><td></td><td></td><td><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></td></tr><tr><td height=5></td></tr></table>");
		tb.append("</center></td>	");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</td></tr><tr><td height=10></td></tr></table>");
		tb.append("</body></html>		");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h npc_%objectId%_achievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_getReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=120 align=\"left\">Condition To Meet:</td><td width=55 align=\"center\">Value:</td><td width=95 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyStats(L2PcInstance player)
	{
		
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		
		player.sendMessage("You have completed " + completedCount + " from " + AchievementsManager.getInstance().getAchievementList().size() + " achievements");
	}
	
	private String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=280 border=0 bgcolor=\"444444\">";
		}
		return "<table width=280 border=0>";
	}
	
	private String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=120 align=\"left\">" + c.getType().getText() + "</td><td width=55 align=\"center\">" + c.getValue() + "</td><td width=95 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s += completed;
			}
			else
			{
				s += notcompleted;
			}
		}
		return s;
	}
}