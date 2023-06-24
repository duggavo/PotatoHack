/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.commands;

import net.potatohack.DontBlock;
import net.potatohack.Feature;
import net.potatohack.command.CmdError;
import net.potatohack.command.CmdException;
import net.potatohack.command.CmdSyntaxError;
import net.potatohack.command.Command;
import net.potatohack.settings.ColorSetting;
import net.potatohack.settings.Setting;
import net.potatohack.util.CmdUtils;
import net.potatohack.util.ColorUtils;
import net.potatohack.util.json.JsonException;

@DontBlock
public final class SetColorCmd extends Command
{
	public SetColorCmd()
	{
		super("setcolor",
			"Changes a color setting of a feature. Allows you\n"
				+ "to set RGB values through keybinds.",
			".setcolor <feature> <setting> <RGB>",
			"Example: .setcolor ClickGUI AC #FF0000");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		ColorSetting colorSetting = getAsColor(feature, setting);
		setColor(colorSetting, args[2]);
	}
	
	private ColorSetting getAsColor(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof ColorSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " is not a color setting.");
		
		return (ColorSetting)setting;
	}
	
	private void setColor(ColorSetting setting, String value)
		throws CmdSyntaxError
	{
		try
		{
			setting.setColor(ColorUtils.parseHex(value));
			
		}catch(JsonException e)
		{
			throw new CmdSyntaxError("Invalid color: " + value);
		}
	}
}
