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
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author UnAfraid
 */
public class AdminCommandHandler implements IHandler<IAdminCommandHandler, String>
{
	private final Map<String, IAdminCommandHandler> _datatable;
	
	protected AdminCommandHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IAdminCommandHandler handler)
	{
		for (String element : handler.getAdminCommandList())
		{
			_datatable.put(element.toLowerCase(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IAdminCommandHandler handler)
	{
		for (String element : handler.getAdminCommandList())
		{
			_datatable.remove(element.toLowerCase());
		}
	}
	
	@Override
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		if (adminCommand.contains(" "))
		{
			adminCommand = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		return _datatable.get(adminCommand);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}
