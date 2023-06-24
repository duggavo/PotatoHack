/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.nochatreports;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.potatohack.PotatoHack;
import net.potatohack.other_feature.OtfList;
import net.potatohack.util.ChatUtils;
import net.potatohack.util.LastServerRememberer;

public final class NcrModRequiredScreen extends Screen
{
	private static final List<String> DISCONNECT_REASONS = Arrays.asList(
		// Older versions of NCR have a bug that sends the raw translation key.
		"disconnect.nochatreports.server",
		"You do not have No Chat Reports, and this server is configured to require it on client!");
	
	private final Screen prevScreen;
	private final Text reason;
	private MultilineText reasonFormatted = MultilineText.EMPTY;
	private int reasonHeight;
	
	private ButtonWidget signatureButton;
	private final Supplier<String> sigButtonMsg;
	
	private ButtonWidget vsButton;
	private final Supplier<String> vsButtonMsg;
	
	public NcrModRequiredScreen(Screen prevScreen)
	{
		super(Text.literal(ChatUtils.POTATO_PREFIX).append(
			Text.translatable("gui.wurst.nochatreports.ncr_mod_server.title")));
		this.prevScreen = prevScreen;
		
		reason =
			Text.translatable("gui.wurst.nochatreports.ncr_mod_server.message");
		
		OtfList otfs = PotatoHack.INSTANCE.getOtfs();
		
		sigButtonMsg = () -> PotatoHack.INSTANCE
			.translate("button.wurst.nochatreports.signatures_status")
			+ blockedOrAllowed(otfs.noChatReportsOtf.isEnabled());
		
		vsButtonMsg =
			() -> "VanillaSpoof: " + onOrOff(otfs.vanillaSpoofOtf.isEnabled());
	}
	
	private String onOrOff(boolean on)
	{
		return PotatoHack.INSTANCE.translate("options." + (on ? "on" : "off"))
			.toUpperCase();
	}
	
	private String blockedOrAllowed(boolean blocked)
	{
		return PotatoHack.INSTANCE.translate(
			"gui.wurst.generic.allcaps_" + (blocked ? "blocked" : "allowed"));
	}
	
	@Override
	protected void init()
	{
		reasonFormatted =
			MultilineText.create(textRenderer, reason, width - 50);
		reasonHeight = reasonFormatted.count() * textRenderer.fontHeight;
		
		int buttonX = width / 2 - 100;
		int belowReasonY =
			(height - 78) / 2 + reasonHeight / 2 + textRenderer.fontHeight * 2;
		int signaturesY = Math.min(belowReasonY, height - 68);
		int reconnectY = signaturesY + 24;
		int backButtonY = reconnectY + 24;
		
		addDrawableChild(signatureButton = ButtonWidget
			.builder(Text.literal(sigButtonMsg.get()), b -> toggleSignatures())
			.dimensions(buttonX - 48, signaturesY, 148, 20).build());
		
		addDrawableChild(vsButton = ButtonWidget
			.builder(Text.literal(vsButtonMsg.get()), b -> toggleVanillaSpoof())
			.dimensions(buttonX + 102, signaturesY, 148, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Reconnect"),
				b -> LastServerRememberer.reconnect(prevScreen))
			.dimensions(buttonX, reconnectY, 200, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.translatable("gui.toMenu"),
				b -> client.setScreen(prevScreen))
			.dimensions(buttonX, backButtonY, 200, 20).build());
	}
	
	private void toggleSignatures()
	{
		PotatoHack.INSTANCE.getOtfs().noChatReportsOtf.doPrimaryAction();
		signatureButton.setMessage(Text.literal(sigButtonMsg.get()));
	}
	
	private void toggleVanillaSpoof()
	{
		PotatoHack.INSTANCE.getOtfs().vanillaSpoofOtf.doPrimaryAction();
		vsButton.setMessage(Text.literal(vsButtonMsg.get()));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		
		int centerX = width / 2;
		int reasonY = (height - 68) / 2 - reasonHeight / 2;
		int titleY = reasonY - textRenderer.fontHeight * 2;
		
		context.drawCenteredTextWithShadow(textRenderer, title, centerX, titleY,
			0xAAAAAA);
		reasonFormatted.drawCenterWithShadow(context, centerX, reasonY);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	public static boolean isCausedByLackOfNCR(Text disconnectReason)
	{
		OtfList otfs = PotatoHack.INSTANCE.getOtfs();
		if(otfs.noChatReportsOtf.isActive()
			&& !otfs.vanillaSpoofOtf.isEnabled())
			return false;
		
		String text = disconnectReason.getString();
		if(text == null)
			return false;
		
		text = StringHelper.stripTextFormat(text);
		return DISCONNECT_REASONS.contains(text);
	}
}
