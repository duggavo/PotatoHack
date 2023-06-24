/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;

@SearchTags({"AutoSwim", "auto swim"})
public final class FishHack extends Hack implements UpdateListener
{
	public FishHack()
	{
		super("Fish");
		setCategory(Category.MOVEMENT);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		WURST.getHax().dolphinHack.setEnabled(false);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		if(!player.isWet() || player.isSneaking())
			return;
		
		Vec3d velocity = player.getVelocity();
		player.setVelocity(velocity.x, velocity.y + 0.005, velocity.z);
	}
}
