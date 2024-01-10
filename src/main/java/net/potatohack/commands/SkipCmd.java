/*
 * Copyright (c) 2014-2023 duggavo.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.commands;

import net.potatohack.command.CmdException;
import net.potatohack.command.CmdError;
import net.potatohack.command.Command;

public class SkipCmd extends Command {
	public SkipCmd() {
		super("skip", "Skips the current Notebot song, if playing a playlist");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if (!WURST.getHax().noteBotHack.isEnabled()) {
			throw new CmdError("Note bot is not enabled.");
		}

		WURST.getHax().noteBotHack.skipSong();
	}
}
