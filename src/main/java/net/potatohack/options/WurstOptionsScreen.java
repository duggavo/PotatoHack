/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.options;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;
import net.potatohack.PotatoHack;
import net.potatohack.commands.FriendsCmd;
import net.potatohack.hacks.XRayHack;
import net.potatohack.mixinterface.IScreen;
import net.potatohack.other_features.VanillaSpoofOtf;
import net.potatohack.settings.CheckboxSetting;

public class WurstOptionsScreen extends Screen
{
	private Screen prevScreen;
	
	public WurstOptionsScreen(Screen prevScreen)
	{
		super(Text.literal(""));
		this.prevScreen = prevScreen;
	}
	
	@Override
	public void init()
	{
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Back"), b -> client.setScreen(prevScreen))
			.dimensions(width / 2 - 100, height / 4 + 144 - 16, 200, 20)
			.build());
		
		addSettingButtons();
		addManagerButtons();
		addLinkButtons();
	}
	
	private void addSettingButtons()
	{
		PotatoHack wurst = PotatoHack.INSTANCE;
		FriendsCmd friendsCmd = wurst.getCmds().friendsCmd;
		CheckboxSetting middleClickFriends = friendsCmd.getMiddleClickFriends();
		VanillaSpoofOtf vanillaSpoofOtf = wurst.getOtfs().vanillaSpoofOtf;
		CheckboxSetting forceEnglish =
			wurst.getOtfs().translationsOtf.getForceEnglish();
		
		new WurstOptionsButton(-154, 24,
			() -> "Click Friends: "
				+ (middleClickFriends.isChecked() ? "ON" : "OFF"),
			middleClickFriends.getWrappedDescription(200),
			b -> middleClickFriends
				.setChecked(!middleClickFriends.isChecked()));
		
		new WurstOptionsButton(-154, 48,
			() -> "Spoof Vanilla: "
				+ (vanillaSpoofOtf.isEnabled() ? "ON" : "OFF"),
			vanillaSpoofOtf.getWrappedDescription(200),
			b -> vanillaSpoofOtf.doPrimaryAction());
		
		new WurstOptionsButton(-154, 72,
			() -> "Translations: " + (!forceEnglish.isChecked() ? "ON" : "OFF"),
			"§cThis is an experimental feature!\n"
				+ "We don't have many translations yet. If you\n"
				+ "speak both English and some other language,\n"
				+ "please help us by adding more translations.",
			b -> forceEnglish.setChecked(!forceEnglish.isChecked()));
	}
	
	private void addManagerButtons()
	{
		XRayHack xRayHack = PotatoHack.INSTANCE.getHax().xRayHack;
		
		new WurstOptionsButton(-50, 24, () -> "Keybinds",
			"Keybinds allow you to toggle any hack\n"
				+ "or command by simply pressing a\n" + "button.",
			b -> client.setScreen(new KeybindManagerScreen(this)));
		
		new WurstOptionsButton(-50, 48, () -> "X-Ray Blocks",
			"Manager for the blocks\n" + "that X-Ray will show.",
			b -> xRayHack.openBlockListEditor(this));
		
		new WurstOptionsButton(-50, 72, () -> "Zoom",
			"The Zoom Manager allows you to\n"
				+ "change the zoom key, how far it\n"
				+ "will zoom in and more.",
			b -> client.setScreen(new ZoomManagerScreen(this)));
	}
	
	private void addLinkButtons()
	{
		OperatingSystem os = Util.getOperatingSystem();
		
		new WurstOptionsButton(54, 24, () -> "Official Website",
			"PotatoHack.net", b -> os.open(
				"https://potatohack.net/potato_options/website"));
						
		new WurstOptionsButton(54, 72, () -> "Forum", "forum.PotatoHack.net",
			b -> os.open("https://potatohack.net/potato_options/forum"));
		
		new WurstOptionsButton(54, 96, () -> "Donate",
			"PotatoHack.net/donate", b -> os.open(
				"https://potatohack.net/potato_options/donate"));
	}
	
	@Override
	public void close()
	{
		client.setScreen(prevScreen);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		renderBackground(context);
		renderTitles(context);
		super.render(context, mouseX, mouseY, partialTicks);
		renderButtonTooltip(context, mouseX, mouseY);
	}
	
	private void renderTitles(DrawContext context)
	{
		TextRenderer tr = client.textRenderer;
		int middleX = width / 2;
		int y1 = 40;
		int y2 = height / 4 + 24 - 28;
		
		context.drawCenteredTextWithShadow(tr, "Wurst Options", middleX, y1,
			0xffffff);
		
		context.drawCenteredTextWithShadow(tr, "Settings", middleX - 104, y2,
			0xcccccc);
		context.drawCenteredTextWithShadow(tr, "Managers", middleX, y2,
			0xcccccc);
		context.drawCenteredTextWithShadow(tr, "Links", middleX + 104, y2,
			0xcccccc);
	}
	
	private void renderButtonTooltip(DrawContext context, int mouseX,
		int mouseY)
	{
		for(Drawable d : ((IScreen)this).getButtons())
		{
			if(!(d instanceof ClickableWidget))
				continue;
			
			ClickableWidget button = (ClickableWidget)d;
			
			if(!button.isSelected() || !(button instanceof WurstOptionsButton))
				continue;
			
			WurstOptionsButton woButton = (WurstOptionsButton)button;
			
			if(woButton.tooltip.isEmpty())
				continue;
			
			context.drawTooltip(textRenderer, woButton.tooltip, mouseX, mouseY);
			break;
		}
	}
	
	private final class WurstOptionsButton extends ButtonWidget
	{
		private final Supplier<String> messageSupplier;
		private final List<Text> tooltip;
		
		public WurstOptionsButton(int xOffset, int yOffset,
			Supplier<String> messageSupplier, String tooltip,
			PressAction pressAction)
		{
			super(WurstOptionsScreen.this.width / 2 + xOffset,
				WurstOptionsScreen.this.height / 4 - 16 + yOffset, 100, 20,
				Text.literal(messageSupplier.get()), pressAction,
				ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			
			this.messageSupplier = messageSupplier;
			
			if(tooltip.isEmpty())
				this.tooltip = Arrays.asList();
			else
			{
				String[] lines = tooltip.split("\n");
				
				Text[] lines2 = new Text[lines.length];
				for(int i = 0; i < lines.length; i++)
					lines2[i] = Text.literal(lines[i]);
				
				this.tooltip = Arrays.asList(lines2);
			}
			
			addDrawableChild(this);
		}
		
		@Override
		public void onPress()
		{
			super.onPress();
			setMessage(Text.literal(messageSupplier.get()));
		}
	}
}
