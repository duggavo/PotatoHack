/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.event.EventManager;
import net.potatohack.events.ChatInputListener.ChatInputEvent;

@Mixin(ChatHud.class)
public class ChatHudMixin
{
	@Shadow
	private List<ChatHudLine.Visible> visibleMessages;
	@Shadow
	private MinecraftClient client;
	
	@Inject(at = @At("HEAD"),
		method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
		cancellable = true)
	private void onAddMessage(Text message,
		@Nullable MessageSignatureData signature,
		@Nullable MessageIndicator indicator, CallbackInfo ci)
	{
		ChatInputEvent event = new ChatInputEvent(message, visibleMessages);
		
		EventManager.fire(event);
		if(event.isCancelled())
		{
			ci.cancel();
			return;
		}
		
		message = event.getComponent();
		indicator = PotatoHack.INSTANCE.getOtfs().noChatReportsOtf
			.modifyIndicator(message, signature, indicator);
		shadow$logChatMessage(message, indicator);
		shadow$addMessage(message, signature, client.inGameHud.getTicks(),
			indicator, false);
		ci.cancel();
	}
	
	@Shadow
	private void shadow$logChatMessage(Text message,
		@Nullable MessageIndicator indicator)
	{
		
	}
	
	@Shadow
	private void shadow$addMessage(Text message,
		@Nullable MessageSignatureData signature, int ticks,
		@Nullable MessageIndicator indicator, boolean refresh)
	{
		
	}
}
