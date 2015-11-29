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
package l2r.gameserver.model.events.impl.character;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.IBaseEvent;
import l2r.gameserver.model.skills.L2Skill;

/**
 * An instantly executed event when L2Character is attacked by L2Character.
 * @author UnAfraid
 */
public class OnCreatureDamageReceived implements IBaseEvent
{
	private final L2Character _attacker;
	private final L2Character _target;
	private final double _damage;
	private final L2Skill _skill;
	private final boolean _crit;
	private final boolean _damageOverTime;
	
	public OnCreatureDamageReceived(L2Character attacker, L2Character target, double damage, L2Skill skill, boolean crit, boolean damageOverTime)
	{
		_attacker = attacker;
		_target = target;
		_damage = damage;
		_skill = skill;
		_crit = crit;
		_damageOverTime = damageOverTime;
	}
	
	public final L2Character getAttacker()
	{
		return _attacker;
	}
	
	public final L2Character getTarget()
	{
		return _target;
	}
	
	public double getDamage()
	{
		return _damage;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	public boolean isCritical()
	{
		return _crit;
	}
	
	public boolean isDamageOverTime()
	{
		return _damageOverTime;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_DAMAGE_RECEIVED;
	}
}