/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.potatohack.Category;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;
import net.potatohack.util.BlockBreaker;
import net.potatohack.util.BlockUtils;
import net.potatohack.util.RotationUtils;

public final class KaboomHack extends Hack implements UpdateListener
{
	private final SliderSetting power =
		new SliderSetting("Power", 128, 32, 512, 32, ValueDisplay.INTEGER);
	
	public KaboomHack()
	{
		super("Kaboom");
		
		setCategory(Category.BLOCKS);
		addSetting(power);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check fly-kick
		if(!MC.player.getAbilities().creativeMode && !MC.player.isOnGround())
			return;
		
		// do explosion particles
		new Explosion(MC.world, MC.player, MC.player.getX(), MC.player.getY(),
			MC.player.getZ(), 6F, false, Explosion.DestructionType.KEEP)
				.affectWorld(true);
		
		// get valid blocks
		ArrayList<BlockPos> blocks = getBlocksByDistanceReversed(6);
		
		// break all blocks
		for(int i = 0; i < power.getValueI(); i++)
			BlockBreaker.breakBlocksWithPacketSpam(blocks);
		
		// disable
		setEnabled(false);
	}
	
	private ArrayList<BlockPos> getBlocksByDistanceReversed(double range)
	{
		Vec3d eyesVec = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeSq = Math.pow(range + 0.5, 2);
		int rangeI = (int)Math.ceil(range);
		
		BlockPos center = BlockPos.ofFloored(RotationUtils.getEyesPos());
		BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
		BlockPos max = center.add(rangeI, rangeI, rangeI);
		
		return BlockUtils.getAllInBox(min, max).stream()
			.filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
			.sorted(Comparator.comparingDouble(
				pos -> -eyesVec.squaredDistanceTo(Vec3d.of(pos))))
			.collect(Collectors.toCollection(ArrayList::new));
	}
}
