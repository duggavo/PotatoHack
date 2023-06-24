/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.other_features;

import net.potatohack.DontBlock;
import net.potatohack.SearchTags;
import net.potatohack.other_feature.OtherFeature;

@SearchTags({"last server"})
@DontBlock
public final class LastServerOtf extends OtherFeature
{
	public LastServerOtf()
	{
		super("LastServer",
			"Wurst adds a \"Last Server\" button to the server selection screen that automatically brings you back to the last server you played on.\n\n"
				+ "Useful when you get kicked and/or have a lot of servers.");
	}
}
