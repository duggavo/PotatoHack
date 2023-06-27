/*
 * Copyright (c) 2023 duggavo.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.block.FluidBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.potatohack.Category;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;
import net.potatohack.util.BlockUtils;

public final class FastFallHack extends Hack implements UpdateListener {

	private final SliderSetting fallSpeed = new SliderSetting("Fall speed",
		1, 0.25, 7, 0.05, ValueDisplay.DECIMAL);
		
	private final SliderSetting minHeight = new SliderSetting("Min height",
		"Won't push you down when you are too close to the ground.", 0, 0, 4, 0.1,
		ValueDisplay.DECIMAL.withLabel(0, "disabled"));
	
	public FastFallHack()
	{
		super("FastFall");
		
		setCategory(Category.MOVEMENT);
		addSetting(fallSpeed);
		addSetting(minHeight);
	}
	
	@Override
	public void onEnable()
	{
		WURST.getHax().glideHack.setEnabled(false);
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
		ClientPlayerEntity player = MC.player;
		Vec3d vel = player.getVelocity();
		
		if (vel.y >= 0 || player.isOnGround() || player.isClimbing()
			|| player.isTouchingWater() || player.isInLava()) {
			return;
		}
		
		if (minHeight.getValue() != 0)
		{
			Box box = player.getBoundingBox();
			box = box.union(box.offset(0, -minHeight.getValue(), 0));
			if(!MC.world.isSpaceEmpty(box))
				return;
			
			BlockPos min = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
			BlockPos max = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
			Stream<BlockPos> stream = StreamSupport
				.stream(BlockUtils.getAllInBox(min, max).spliterator(), true);
			
			if(stream.map(BlockUtils::getBlock)
				.anyMatch(b -> b instanceof FluidBlock))
				return;
		}
		
		player.setVelocity(vel.x, Math.min(-fallSpeed.getValue(), vel.y), vel.z);
	}
}
