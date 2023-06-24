/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.event.EventManager;
import net.potatohack.events.DeathListener.DeathEvent;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen
{
	private DeathScreenMixin(PotatoHack wurst, Text text_1)
	{
		super(text_1);
	}
	
	@Inject(at = {@At(value = "TAIL")}, method = {"tick()V"})
	private void onTick(CallbackInfo ci)
	{
		EventManager.fire(DeathEvent.INSTANCE);
	}
}
