/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.events.RightClickListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;

@SearchTags({"air place"})
public final class AirPlaceHack extends Hack implements RightClickListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	public AirPlaceHack()
	{
		super("AirPlace");
		setCategory(Category.BLOCKS);
		addSetting(range);
	}
	
	@Override
	public void onEnable()
	{
		WURST.getHax().autoFishHack.setEnabled(false);
		
		EVENTS.add(RightClickListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(RightClickListener.class, this);
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		HitResult hitResult = MC.player.raycast(range.getValue(), 0, false);
		if(!(hitResult instanceof BlockHitResult blockHitResult))
			return;
		
		IMC.getInteractionManager().rightClickBlock(
			blockHitResult.getBlockPos(), blockHitResult.getSide(),
			blockHitResult.getPos());
	}
}
