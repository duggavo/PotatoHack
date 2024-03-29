/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Box;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.hack.Hack;
import net.potatohack.mixinterface.IKeyBinding;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;

@SearchTags({"safe walk"})
public final class SafeWalkHack extends Hack
{
	private final CheckboxSetting sneak =
		new CheckboxSetting("Sneak at edges", "Visibly sneak at edges.", false);
	
	private final SliderSetting edgeDistance = new SliderSetting(
		"Sneak edge distance",
		"How close SafeWalk will let you get to the edge before sneaking.\n\n"
			+ "This setting is only used when \"Sneak at edges\" is enabled.",
		0.05, 0.05, 0.25, 0.001, ValueDisplay.DECIMAL.withSuffix("m"));
	
	private boolean sneaking;
	
	private final String renderName =
		Math.random() < 0.01 ? "SafuWalk" : getName();

	public SafeWalkHack()
	{
		super("SafeWalk");
		setCategory(Category.MOVEMENT);
		addSetting(sneak);
		addSetting(edgeDistance);
	}
	
	@Override
	protected void onEnable()
	{
		WURST.getHax().parkourHack.setEnabled(false);
		sneaking = false;
	}
	
	@Override
	protected void onDisable()
	{
		if(sneaking)
			setSneaking(false);
	}

	@Override
	public String getRenderName()
	{
		return renderName;
	}
	
	public void onClipAtLedge(boolean clipping)
	{
		ClientPlayerEntity player = MC.player;
		
		if(!isEnabled() || !sneak.isChecked() || !player.isOnGround())
		{
			if(sneaking)
				setSneaking(false);
			
			return;
		}
		
		Box box = player.getBoundingBox();
		Box adjustedBox = box.stretch(0, -player.stepHeight, 0)
			.expand(-edgeDistance.getValue(), 0, -edgeDistance.getValue());
		
		if(MC.world.isSpaceEmpty(player, adjustedBox))
			clipping = true;
		
		setSneaking(clipping);
	}
	
	private void setSneaking(boolean sneaking)
	{
		KeyBinding sneakKey = MC.options.sneakKey;
		
		if(sneaking)
			sneakKey.setPressed(true);
		else
			((IKeyBinding)sneakKey).resetPressedState();
		
		this.sneaking = sneaking;
	}
	
	// See ClientPlayerEntityMixin
}
