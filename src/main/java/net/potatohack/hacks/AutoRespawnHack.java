/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.events.DeathListener;
import net.potatohack.hack.Hack;

@SearchTags({"auto respawn", "AutoRevive", "auto revive"})
public final class AutoRespawnHack extends Hack implements DeathListener
{
	public AutoRespawnHack()
	{
		super("AutoRespawn");
		setCategory(Category.COMBAT);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(DeathListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(DeathListener.class, this);
	}
	
	@Override
	public void onDeath()
	{
		MC.player.requestRespawn();
		MC.setScreen(null);
	}
}
