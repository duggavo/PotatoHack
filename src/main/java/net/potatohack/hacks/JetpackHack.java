/*
 * Copyright (c) 2023 duggavo.
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.lang.Math;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;

@SearchTags({"jet pack", "AirJump", "air jump"})
public final class JetpackHack extends Hack implements UpdateListener
{
	public final SliderSetting verticalSpeed = new SliderSetting(
		"Vertical Speed",
		"The default 0.42 is vanilla jump speed",
		0.42, 0.05, 2, 0.01, ValueDisplay.DECIMAL);
	public final SliderSetting horizontalSpeed = new SliderSetting(
		"Horizontal Speed",
		"The default 0.2 is vanilla jump horizontal speed",
		0.2, 0, 4, 0.05, ValueDisplay.DECIMAL);

	public JetpackHack()
	{
		super("Jetpack");
		
		setCategory(Category.MOVEMENT);
		addSetting(verticalSpeed);
		addSetting(horizontalSpeed);
	}
	
	@Override
	public void onEnable()
	{
		WURST.getHax().creativeFlightHack.setEnabled(false);
		WURST.getHax().flightHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
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

		Vec3d velocity = player.getVelocity();
		
		// We calculate and set velocity instead of actually jumping
		// because jumping consumes A LOT of food.
		if((player.isFallFlying() || velocity.y != 0) && MC.options.jumpKey.isPressed()) {
			if (MC.options.sprintKey.isPressed()) {
				double yaw = -Math.toRadians(player.getYaw()) - (Math.PI/2);
				double velx = velocity.x, velz = velocity.z;

				if (MC.options.forwardKey.isPressed()) {
					velx += ((float)Math.cos(yaw) * horizontalSpeed.getValue());
					velz -= ((float)Math.sin(yaw) * horizontalSpeed.getValue());
				}

				player.setVelocity(velx, verticalSpeed.getValue(), velz);
			} else {
				player.setVelocity(velocity.x, verticalSpeed.getValue(), velocity.z);
			}
			
		}
	}
}
