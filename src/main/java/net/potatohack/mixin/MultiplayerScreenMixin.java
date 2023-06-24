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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.mixinterface.IMultiplayerScreen;
import net.potatohack.serverfinder.CleanUpScreen;
import net.potatohack.serverfinder.ServerFinderScreen;
import net.potatohack.util.LastServerRememberer;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen implements IMultiplayerScreen
{
	@Shadow
	protected MultiplayerServerListWidget serverListWidget;
	
	private ButtonWidget lastServerButton;
	
	private MultiplayerScreenMixin(PotatoHack wurst, Text text_1)
	{
		super(text_1);
	}
	
	@Override
	public MultiplayerServerListWidget getServerListSelector()
	{
		return serverListWidget;
	}
	
	@Override
	public void connectToServer(ServerInfo server)
	{
		connect(server);
	}
	
	@Inject(at = {@At("TAIL")}, method = {"init()V"})
	private void onInit(CallbackInfo ci)
	{
		if(!PotatoHack.INSTANCE.isEnabled())
			return;
		
		lastServerButton = addDrawableChild(ButtonWidget
			.builder(Text.literal("Last Server"),
				b -> LastServerRememberer
					.joinLastServer((MultiplayerScreen)(Object)this))
			.dimensions(width / 2 - 154, 10, 90, 20).build());
		
		lastServerButton.active = LastServerRememberer.getLastServer() != null;
		
		addDrawableChild(
			ButtonWidget
				.builder(Text.literal("Server Finder"),
					b -> client.setScreen(new ServerFinderScreen(
						(MultiplayerScreen)(Object)this)))
				.dimensions(width / 2 + 154 + 4, height - 54, 100, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Clean Up"),
				b -> client.setScreen(
					new CleanUpScreen((MultiplayerScreen)(Object)this)))
			.dimensions(width / 2 + 154 + 4, height - 30, 100, 20).build());
	}
	
	@Inject(at = {@At("TAIL")}, method = {"tick()V"})
	public void onTick(CallbackInfo ci)
	{
		if(lastServerButton == null)
			return;
		
		lastServerButton.active = LastServerRememberer.getLastServer() != null;
	}
	
	@Inject(at = {@At("HEAD")},
		method = {"connect(Lnet/minecraft/client/network/ServerInfo;)V"})
	private void onConnect(ServerInfo entry, CallbackInfo ci)
	{
		LastServerRememberer.setLastServer(entry);
	}
	
	@Shadow
	private void connect(ServerInfo entry)
	{
		
	}
}
