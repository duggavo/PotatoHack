/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.settings.filterlists;

import java.util.ArrayList;
import java.util.List;

import net.potatohack.settings.filters.*;

public final class AnchorAuraFilterList extends EntityFilterList
{
	private AnchorAuraFilterList(List<EntityFilter> filters)
	{
		super(filters);
	}
	
	public static AnchorAuraFilterList create()
	{
		ArrayList<EntityFilter> builder = new ArrayList<>();
		String damageWarning =
			"\n\nThey can still take damage if they get too close to a valid target or an existing anchor.";
		
		builder.add(new FilterPlayersSetting(
			"Won't target other players when auto-placing anchors."
				+ damageWarning,
			false));
		
		builder.add(new FilterMonstersSetting(
			"Won't target zombies, creepers, etc. when auto-placing anchors."
				+ damageWarning,
			true));
		
		builder.add(new FilterAnimalsSetting(
			"Won't target pigs, cows, etc. when auto-placing anchors."
				+ damageWarning,
			true));
		
		builder.add(new FilterTradersSetting(
			"Won't target villagers, wandering traders, etc. when auto-placing anchors."
				+ damageWarning,
			true));
		
		builder.add(new FilterGolemsSetting(
			"Won't target iron golems, snow golems and shulkers when auto-placing anchors."
				+ damageWarning,
			true));
		
		builder.add(new FilterAllaysSetting(
			"Won't target allays when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterInvisibleSetting(
			"Won't target invisible entities when auto-placing anchors."
				+ damageWarning,
			false));
		
		builder.add(new FilterNamedSetting(
			"Won't target name-tagged entities when auto-placing anchors."
				+ damageWarning,
			false));
		
		builder.add(new FilterArmorStandsSetting(
			"Won't target armor stands when auto-placing anchors."
				+ damageWarning,
			true));
		
		return new AnchorAuraFilterList(builder);
	}
}
