/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.potatohack.PotatoHack;
import net.potatohack.mixinterface.IArmorItem;

@Mixin(ArmorItem.class)
public class ArmorItemMixin extends Item implements IArmorItem
{
	@Shadow
	protected float toughness;
	
	private ArmorItemMixin(PotatoHack wurst, Settings item$Settings_1)
	{
		super(item$Settings_1);
	}
	
	@Override
	public float getToughness()
	{
		return toughness;
	}
}
