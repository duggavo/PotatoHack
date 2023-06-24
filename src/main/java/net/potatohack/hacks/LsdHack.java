/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.potatohack.Category;
import net.potatohack.hack.DontSaveState;
import net.potatohack.hack.Hack;
import net.potatohack.mixinterface.IGameRenderer;

@DontSaveState
public final class LsdHack extends Hack
{
	public LsdHack()
	{
		super("LSD");
		setCategory(Category.FUN);
	}
	
	@Override
	public void onEnable()
	{
		if(!(MC.getCameraEntity() instanceof PlayerEntity))
		{
			setEnabled(false);
			return;
		}
		
		if(MC.gameRenderer.getPostProcessor() != null)
			MC.gameRenderer.disablePostProcessor();
		
		((IGameRenderer)MC.gameRenderer)
			.loadWurstShader(new Identifier("shaders/post/wobble.json"));
	}
	
	@Override
	public void onDisable()
	{
		if(MC.gameRenderer.getPostProcessor() != null)
			MC.gameRenderer.disablePostProcessor();
	}
}
