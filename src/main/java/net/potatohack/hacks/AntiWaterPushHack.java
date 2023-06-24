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
import net.potatohack.events.IsPlayerInWaterListener;
import net.potatohack.events.UpdateListener;
import net.potatohack.events.VelocityFromFluidListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.CheckboxSetting;

@SearchTags({"anti water push", "NoWaterPush", "no water push"})
public final class AntiWaterPushHack extends Hack implements UpdateListener,
	VelocityFromFluidListener, IsPlayerInWaterListener
{
	private final CheckboxSetting preventSlowdown = new CheckboxSetting(
		"Prevent slowdown", "Allows you to walk underwater at full speed.\n"
			+ "Some servers consider this a speedhack.",
		false);
	
	public AntiWaterPushHack()
	{
		super("AntiWaterPush");
		setCategory(Category.MOVEMENT);
		addSetting(preventSlowdown);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(VelocityFromFluidListener.class, this);
		EVENTS.add(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(VelocityFromFluidListener.class, this);
		EVENTS.remove(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!preventSlowdown.isChecked())
			return;
		
		if(!MC.options.jumpKey.isPressed())
			return;
		
		if(!MC.player.isOnGround())
			return;
		
		if(!IMC.getPlayer().isTouchingWaterBypass())
			return;
		
		MC.player.jump();
	}
	
	@Override
	public void onVelocityFromFluid(VelocityFromFluidEvent event)
	{
		if(event.getEntity() == MC.player)
			event.cancel();
	}
	
	@Override
	public void onIsPlayerInWater(IsPlayerInWaterEvent event)
	{
		if(preventSlowdown.isChecked())
			event.setInWater(false);
	}
	
	public boolean isPreventingSlowdown()
	{
		return preventSlowdown.isChecked();
	}
}
