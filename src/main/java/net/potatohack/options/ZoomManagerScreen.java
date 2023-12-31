/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.options;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.other_features.ZoomOtf;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.SliderSetting;

public class ZoomManagerScreen extends Screen implements PressAKeyCallback
{
	private Screen prevScreen;
	private ButtonWidget keyButton;
	private ButtonWidget scrollButton;
	
	public ZoomManagerScreen(Screen par1GuiScreen)
	{
		super(Text.literal(""));
		prevScreen = par1GuiScreen;
	}
	
	@Override
	public void init()
	{
		ZoomOtf zoom = PotatoHack.INSTANCE.getOtfs().zoomOtf;
		SliderSetting level = zoom.getLevelSetting();
		CheckboxSetting scroll = zoom.getScrollSetting();
		String zoomKeyName = PotatoHack.INSTANCE.getZoomKey()
			.getBoundKeyTranslationKey().replace("key.keyboard.", "");
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Back"), b -> client.setScreen(prevScreen))
			.dimensions(width / 2 - 100, height / 4 + 144 - 16, 200, 20)
			.build());
		
		addDrawableChild(keyButton = ButtonWidget
			.builder(Text.literal("Zoom Key: " + zoomKeyName),
				b -> client.setScreen(new PressAKeyScreen(this)))
			.dimensions(width / 2 - 79, height / 4 + 24 - 16, 158, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("More"), b -> level.increaseValue())
			.dimensions(width / 2 - 79, height / 4 + 72 - 16, 50, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Less"), b -> level.decreaseValue())
			.dimensions(width / 2 - 25, height / 4 + 72 - 16, 50, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Default"),
				b -> level.setValue(level.getDefaultValue()))
			.dimensions(width / 2 + 29, height / 4 + 72 - 16, 50, 20).build());
		
		addDrawableChild(
			scrollButton = ButtonWidget
				.builder(
					Text.literal(
						"Use Mouse Wheel: " + onOrOff(scroll.isChecked())),
					b -> toggleScroll())
				.dimensions(width / 2 - 79, height / 4 + 96 - 16, 158, 20)
				.build());
	}
	
	private void toggleScroll()
	{
		ZoomOtf zoom = PotatoHack.INSTANCE.getOtfs().zoomOtf;
		CheckboxSetting scroll = zoom.getScrollSetting();
		
		scroll.setChecked(!scroll.isChecked());
		scrollButton.setMessage(
			Text.literal("Use Mouse Wheel: " + onOrOff(scroll.isChecked())));
	}
	
	private String onOrOff(boolean on)
	{
		return on ? "ON" : "OFF";
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
		ZoomOtf zoom = PotatoHack.INSTANCE.getOtfs().zoomOtf;
		SliderSetting level = zoom.getLevelSetting();
		
		renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, "Zoom Manager",
			width / 2, 40, 0xffffff);
		context.drawTextWithShadow(textRenderer,
			"Zoom Level: " + level.getValueString(), width / 2 - 75,
			height / 4 + 44, 0xcccccc);
		
		super.render(context, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void setKey(String key)
	{
		PotatoHack.INSTANCE.getZoomKey()
			.setBoundKey(InputUtil.fromTranslationKey(key));
		client.options.write();
		KeyBinding.updateKeysByCode();
		keyButton.setMessage(Text.literal("Zoom Key: " + key));
	}
}
