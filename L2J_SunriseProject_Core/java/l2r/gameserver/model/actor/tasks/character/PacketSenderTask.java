/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.tasks.character;

import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.CharInfo;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExDominionWarStart;
import l2r.gameserver.network.serverpackets.UserInfo;

/**
 * This class dedicated to manage packets.
 * @author vGodFather
 */
public class PacketSenderTask
{
	// Players
	public static void sendUserInfoImpl(L2PcInstance player)
	{
		if (player.entering)
		{
			return;
		}
		
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		
		if (TerritoryWarManager.getInstance().isTWInProgress() && (TerritoryWarManager.getInstance().checkIsRegistered(-1, player.getObjectId()) || TerritoryWarManager.getInstance().checkIsRegistered(-1, player.getClan())))
		{
			player.broadcastPacket(new ExDominionWarStart(player));
		}
	}
	
	public static void updateAndBroadcastStatus(L2PcInstance player, boolean fullUpdate)
	{
		player.refreshOverloaded(false);
		player.refreshExpertisePenalty();
		
		if (player.entering)
		{
			return;
		}
		
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		if (fullUpdate)
		{
			broadcastUserInfo(player, true);
		}
		else
		{
			player.sendUserInfo(true);
		}
	}
	
	public static void broadcastUserInfo(L2PcInstance player, boolean force)
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		player.sendUserInfo(force);
		player.sendCharInfo(force);
	}
	
	public static void sendCharInfoImpl(L2PcInstance player)
	{
		// Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
		player.broadcastPacket(new CharInfo(player));
		player.broadcastPacket(new ExBrExtraUserInfo(player));
	}
}
