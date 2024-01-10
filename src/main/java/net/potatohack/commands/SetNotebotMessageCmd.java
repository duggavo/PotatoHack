/*
 * Copyright (c) 2014-2023 duggavo.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.potatohack.command.CmdException;
import net.potatohack.command.Command;

public class SetNotebotMessageCmd extends Command {
	public SetNotebotMessageCmd() {
		super("setnotebotmessage", "Sets the notebot chat message.\nUse $name and $author to send the music and author.");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		WURST.getHax().noteBotHack.chatMessage = Arrays.stream(
			Arrays.copyOf(args, args.length)).collect(Collectors.joining(" ")
		);
	}
}
