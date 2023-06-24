/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.nochatreports.ForcedChatReportsScreen;

@Mixin(DisconnectedRealmsScreen.class)
public class DisconnectedRealmsScreenMixin extends RealmsScreen
{
	@Shadow
	@Final
	private Text reason;
	@Shadow
	@Final
	private Screen parent;
	
	private DisconnectedRealmsScreenMixin(PotatoHack wurst, Text title)
	{
		super(title);
	}
	
	@Inject(at = @At("TAIL"), method = {"init()V"})
	private void onInit(CallbackInfo ci)
	{
		if(!PotatoHack.INSTANCE.isEnabled())
			return;
		
		System.out.println("Realms disconnected: " + reason);
		
		if(ForcedChatReportsScreen.isCausedByNoChatReports(reason))
			client.setScreen(new ForcedChatReportsScreen(parent));
	}
}
