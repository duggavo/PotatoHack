/*
 * Copyright (c) 2014-2023 duggavo.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.stream.Stream;
import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.item.ItemStack;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.PauseAttackOnContainersSetting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;
import net.potatohack.util.BlockUtils;
import net.potatohack.util.EntityUtils;
import net.potatohack.util.RotationUtils;
import net.potatohack.util.RotationUtils.Rotation;

@SearchTags({ "lava" })
public class LavaKillHack extends Hack
		implements UpdateListener {
	private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(true);
	private final CheckboxSetting takeFromInventory = new CheckboxSetting("Take from inventory",
			"Whether or not to take lava buckets from inventory.", true);
	private final SliderSetting range = new SliderSetting("Range",
			"Determines how far LavaKill will reach to place lava.",
			4, 3, 5.5, 0.1, ValueDisplay.DECIMAL);

	public LavaKillHack() {
		super("LavaKill");
		setCategory(Category.COMBAT);

		addSetting(pauseOnContainers);
		addSetting(takeFromInventory);
		addSetting(range);
	}

	@Override
	public void onEnable() {
		EVENTS.add(UpdateListener.class, this);
	}

	@Override
	public void onDisable() {
		EVENTS.remove(UpdateListener.class, this);
	}

	double rangeSq;

	@Override
	public void onUpdate() {
		if (pauseOnContainers.shouldPause())
			return;

		Stream<Entity> stream = EntityUtils.getAttackableEntities();

		rangeSq = Math.pow(range.getValue(), 2);
		stream = stream.filter(e -> e.isPlayer() && MC.player.squaredDistanceTo(e) <= rangeSq
			&& MC.player.squaredDistanceTo(e) >= 2);

		Entity target = stream.min(Comparator.comparingDouble(e -> MC.player.squaredDistanceTo(e))).orElse(null);

		if (target == null)
			return;

		BlockHitResult hitres = MC.world.raycast(new RaycastContext(target.getPos(),
			target.getPos().subtract(0, 2, 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, target));
		if (hitres == null) return;

		BlockPos blockToClick = hitres.getBlockPos();
		blockToClick = blockToClick.up(1);

		if (!selectItem(new ItemStack(Items.LAVA_BUCKET)))
			return;

		placeBlockLegit(blockToClick);

	}

	private boolean selectItem(ItemStack stack) {
		PlayerInventory inv = MC.player.getInventory();
		int slot = inv.getSlotWithStack(stack);
		if (slot >= 0 && slot <= 8) {
			inv.selectedSlot = slot;
			return true;
		} else if (slot >= 9 && slot <= 35) {
			if (takeFromInventory.isChecked()) {
				IMC.getInteractionManager().windowClick_SWAP(slot, inv.selectedSlot);
				return true;
			}
		}
		return false;
	}

	private boolean placeBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = Vec3d.ofCenter(pos);
		double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
		
		for(Direction side : Direction.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			// check if neighbor can be right clicked
			if(!BlockUtils.canBeClicked(neighbor))
				continue;
			
			Vec3d dirVec = Vec3d.of(side.getVector());
			Vec3d hitVec = posVec.add(dirVec.multiply(0.5));
			
			// check if hitVec is within range
			if(eyesPos.squaredDistanceTo(hitVec) > rangeSq)
				continue;
			
			// check if side is visible (facing away from player)
			if(distanceSqPosVec > eyesPos.squaredDistanceTo(posVec.add(dirVec)))
				continue;
			
			// check line of sight
			if(MC.world
				.raycast(new RaycastContext(eyesPos, hitVec,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, MC.player))
				.getType() != HitResult.Type.MISS)
				continue;
			
			// face block
			Rotation rotation = RotationUtils.getNeededRotations(hitVec);
			MC.player.setYaw(rotation.getYaw());
			MC.player.setPitch(rotation.getPitch());
			
			// place block
			IMC.getInteractionManager().rightClickBlock(neighbor,
				side.getOpposite(), hitVec);
			MC.player.swingHand(Hand.MAIN_HAND);
			IMC.setItemUseCooldown(4);
			
			return true;
		}
		
		return false;
	}


}
