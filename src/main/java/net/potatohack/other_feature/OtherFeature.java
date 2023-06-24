/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.other_feature;

import net.potatohack.Feature;

public abstract class OtherFeature extends Feature
{
	private final String name;
	private final String description;
	
	public OtherFeature(String name, String description)
	{
		this.name = name;
		this.description = description;
	}
	
	@Override
	public final String getName()
	{
		return name;
	}
	
	@Override
	public String getDescription()
	{
		return WURST.translate(description);
	}
	
	@Override
	public boolean isEnabled()
	{
		return false;
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "";
	}
	
	@Override
	public void doPrimaryAction()
	{
		
	}
}
