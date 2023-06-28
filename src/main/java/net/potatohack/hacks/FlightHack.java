/*
 * Copyright (c) 2023 duggavo.
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
import net.potatohack.events.IsPlayerInWaterListener;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;

@SearchTags({"FlyHack", "fly hack", "flying"})
public final class FlightHack extends Hack
	implements UpdateListener, IsPlayerInWaterListener
{
	public final SliderSetting horizontalSpeed = new SliderSetting(
		"Horizontal Speed", 2, 0.05, 10, 0.05, ValueDisplay.DECIMAL);
	
	public final SliderSetting verticalSpeed = new SliderSetting(
		"Vertical Speed",
		"\u00a7c\u00a7lWARNING:\u00a7r Setting this too high can cause fall damage, even with NoFall.",
		1.4, 0.05, 5, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting slowSneaking = new CheckboxSetting(
		"Slow sneaking",
		"Reduces your horizontal speed while you are sneaking to prevent you from glitching out.",
		true);
	
	private final CheckboxSetting antiKick = new CheckboxSetting("Anti-Kick",
		"Makes you fall a little bit every now and then to prevent you from getting kicked.",
		true);
	
	private final SliderSetting antiKickInterval =
		new SliderSetting("Anti-Kick Interval",
			"How often Anti-Kick should prevent you from getting kicked.\n"
				+ "Most servers will kick you after 80 ticks.",
			50, 5, 80, 1, ValueDisplay.INTEGER.withSuffix(" ticks"));
	
	private final SliderSetting antiKickDistance = new SliderSetting(
		"Anti-Kick Distance",
		"How far Anti-Kick should make you fall.\n"
			+ "Most servers require at least 0.032m to stop you from getting kicked.",
		0.07, 0.01, 0.2, 0.001, ValueDisplay.DECIMAL.withSuffix("m"));
	
	private int tickCounter = 0;
	private double antiKickPreviousSpeed = 0;
	
	public FlightHack()
	{
		super("Flight");
		setCategory(Category.MOVEMENT);
		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(slowSneaking);
		addSetting(antiKick);
		addSetting(antiKickInterval);
		addSetting(antiKickDistance);
	}
	
	@Override
	public void onEnable()
	{
		tickCounter = 0;
		antiKickPreviousSpeed = 0;
		
		WURST.getHax().creativeFlightHack.setEnabled(false);
		WURST.getHax().jetpackHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		Vec3d vel = player.getVelocity();
		player.getAbilities().flying = false;
		
		double velx = vel.x, velz = vel.z;
		double speed = horizontalSpeed.getValue() / 10;

		double yaw = -Math.toRadians(player.getYaw()) - (Math.PI/2);

		boolean didMove = false;

		if (MC.options.forwardKey.isPressed()) {
			didMove = true;
			velx += ((float)Math.cos(yaw) * speed);
			velz -= ((float)Math.sin(yaw) * speed);
		}
		if (MC.options.backKey.isPressed()) {
			didMove = true;
			velx -= ((float)Math.cos(yaw) * speed);
			velz += ((float)Math.sin(yaw) * speed);
		}
		if (MC.options.leftKey.isPressed()) {
			didMove = true;
			velx += ((float)Math.cos(yaw+(Math.PI/2)) * speed);
			velz -= ((float)Math.sin(yaw+(Math.PI/2)) * speed);
		}
		if (MC.options.rightKey.isPressed()) {
			didMove = true;
			velx += ((float)Math.cos(yaw-(Math.PI/2)) * speed);
			velz -= ((float)Math.sin(yaw-(Math.PI/2)) * speed);
		}

		if (didMove) {
			// Make sure that the speed never exceeds limit
			double currentSpeed = Math.sqrt(velx*velx + velz*velz);

			double maxSpeed = horizontalSpeed.getValue();
			if (MC.options.sneakKey.isPressed() && slowSneaking.isChecked())
				maxSpeed = 0.85;

			double multiplier = maxSpeed / currentSpeed;
			if (multiplier < 1) {
				velx *= multiplier;
				velz *= multiplier;
			}
		} else {
			velx *= 0.6;
			velz *= 0.6;
		}


		if (MC.options.jumpKey.isPressed())
			player.setVelocity(vel.x, getVertSpeed(vel.y, verticalSpeed.getValue()), vel.z);
		else if (MC.options.sneakKey.isPressed())
			player.setVelocity(vel.x, getVertSpeed(vel.y, -verticalSpeed.getValue()), vel.z);
		else
			player.setVelocity(velx, 0, velz);

		if(antiKick.isChecked())
			doAntiKick(vel);
	}
	
	private double getVertSpeed(double speed, double toAdd) {
		if (MC.player.isTouchingWater() && !WURST.getHax().fishHack.isEnabled()) {
			speed += 0.005; // Remove underwater gravity
		} else if (!MC.player.isTouchingWater()) {
			speed += 0.08; // Remove gravity
		}
		speed += toAdd / 10;
		
		speed = Math.max(speed, -toAdd);
		speed = Math.min(speed, toAdd);

		return speed;
	}

	private void doAntiKick(Vec3d vel)
	{
		if(tickCounter > antiKickInterval.getValueI() + 2)
			tickCounter = 0;
		
		switch(tickCounter)
		{
			case 0 ->
			{
				if(MC.options.sneakKey.isPressed())
					tickCounter = 3;
				else {
					antiKickPreviousSpeed = vel.y;
					if (antiKickPreviousSpeed <= antiKickDistance.getValue()) {
						antiKickPreviousSpeed = 0;
					} 
					MC.player.setVelocity(vel.x,
						-antiKickDistance.getValue(), vel.z);
				}
			}
			case 1 -> MC.player.setVelocity(vel.x,
				antiKickDistance.getValue(), vel.z);
			case 2 -> MC.player.setVelocity(vel.x, antiKickPreviousSpeed, vel.z);
		}
		
		tickCounter++;
	}
	
	@Override
	public void onIsPlayerInWater(IsPlayerInWaterEvent event)
	{
		event.setInWater(false);
	}
}
