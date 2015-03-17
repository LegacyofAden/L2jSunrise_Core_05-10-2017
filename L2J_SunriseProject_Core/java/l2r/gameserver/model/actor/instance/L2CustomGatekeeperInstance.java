package l2r.gameserver.model.actor.instance;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;
import gr.sr.datatables.SunriseTable;
import gr.sr.main.Conditions;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */

public final class L2CustomGatekeeperInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2CustomGatekeeperInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CustomGatekeeperInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	private static int getPlayersInZoneCount(int zoneId)
	{
		int playersCount = 0;
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
		
		if (zone != null)
		{
			for (L2Character character : zone.getCharactersInside())
			{
				if (character.isPlayer())
				{
					playersCount++;
				}
			}
		}
		return playersCount;
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (!CustomNpcsConfigs.ENABLE_CUSTOM_GATEKEEPER)
		{
			player.sendMessage("Custom gatekeeper is disabled by admin.");
			return;
		}
		
		if (!CustomNpcsConfigs.ALLOW_TELEPORT_WHILE_COMBAT && player.isInCombat())
		{
			player.sendMessage("Cannot use while in combat.");
			return;
		}
		
		if (!CustomNpcsConfigs.ALLOW_TELEPORT_WITH_KARMA && (player.getKarma() > 0))
		{
			player.sendMessage("Cannot use while hava karma.");
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/teleporter/custom/main.htm");
		if (CustomNpcsConfigs.ENABLE_PLAYERS_COUNT)
		{
			switch (CustomNpcsConfigs.ZONE_TYPE_FOR_PLAYERS_COUNT)
			{
				case "ChaoticZone":
					html.replace("%players_count%", String.valueOf(getPlayersInZoneCount(15501)));
					break;
				case "FlagZone":
					html.replace("%players_count%", String.valueOf(getPlayersInZoneCount(15502)));
					break;
				default:
					break;
			}
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param player
	 * @param command
	 */
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
			return;
		}
		
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/teleporter/custom/" + subCommand[1]);
			if (CustomNpcsConfigs.ENABLE_PLAYERS_COUNT)
			{
				switch (CustomNpcsConfigs.ZONE_TYPE_FOR_PLAYERS_COUNT)
				{
					case "ChaoticZone":
						html.replace("%players_count%", String.valueOf(getPlayersInZoneCount(15501)));
						break;
					case "FlagZone":
						html.replace("%players_count%", String.valueOf(getPlayersInZoneCount(15502)));
						break;
					default:
						break;
				}
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		// Teleport
		else if (command.startsWith("teleportTo"))
		{
			if (player.isTransformed())
			{
				if ((player.getTransformationId() == 9) || (player.getTransformationId() == 8))
				{
					player.untransform();
				}
			}
			
			int itemIdToGet = CustomNpcsConfigs.TELEPORT_ITEM_ID;
			int price = CustomNpcsConfigs.TELEPORT_ITEM_AMOUNT;
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			if (command.startsWith("teleportToGlobal"))
			{
				try
				{
					Integer[] c = new Integer[3];
					boolean onlyForNobless = false;
					c[0] = SunriseTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[0];
					c[1] = SunriseTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[1];
					c[2] = SunriseTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[2];
					onlyForNobless = SunriseTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[3] == 1;
					
					if (onlyForNobless && !player.isNoble() && !player.isGM())
					{
						player.sendMessage("Only noble chars can teleport there.");
						return;
					}
					
					player.destroyItemByItemId("Npc Teleport", itemIdToGet, price, player, true);
					player.teleToLocation(c[0], c[1], c[2]);
				}
				catch (Exception e)
				{
					SecurityActions.startSecurity(player, SecurityType.CUSTON_GATEKEEPER);
				}
			}
		}
	}
}