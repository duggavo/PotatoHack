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
import net.potatohack.hack.Hack;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;

@SearchTags({"camera distance", "CamDistance", "cam distance"})
public final class CameraDistanceHack extends Hack
{
	private final SliderSetting distance =
		new SliderSetting("Distance", 12, -0.5, 150, 0.5, ValueDisplay.DECIMAL);
	
	public CameraDistanceHack()
	{
		super("CameraDistance");
		setCategory(Category.RENDER);
		addSetting(distance);
	}
	
	public double getDistance()
	{
		return distance.getValueF();
	}
	
	// See CameraMixin.changeClipToSpaceDistance()
}
