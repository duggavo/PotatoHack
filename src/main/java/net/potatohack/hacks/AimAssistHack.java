/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.Comparator;
import java.util.stream.Stream;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.potatohack.Category;
import net.potatohack.events.RenderListener;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;
import net.potatohack.settings.filterlists.EntityFilterList;
import net.potatohack.settings.filters.*;
import net.potatohack.util.EntityUtils;
import net.potatohack.util.RotationUtils;
import net.potatohack.util.RotationUtils.Rotation;

public final class AimAssistHack extends Hack
		implements UpdateListener, RenderListener {
	private final SliderSetting range = new SliderSetting("Range", 4.5, 0, 6, 0.05, ValueDisplay.DECIMAL);

	private final SliderSetting rotationSpeed = new SliderSetting("Rotation Speed", 500, 10, 1500, 10,
			ValueDisplay.DEGREES.withSuffix("/s"));

	private final SliderSetting fov = new SliderSetting("FOV",
			"Field Of View - how far away from your crosshair an entity can be before it's ignored.\n"
					+ "360\u00b0 = aims at entities all around you.",
			120, 30, 360, 10, ValueDisplay.DEGREES);

	private final EntityFilterList entityFilters = new EntityFilterList(FilterPlayersSetting.genericCombat(false),
			FilterSleepingSetting.genericCombat(false),
			FilterFlyingSetting.genericCombat(0),
			FilterMonstersSetting.genericCombat(false),
			FilterPigmenSetting.genericCombat(false),
			FilterEndermenSetting.genericCombat(false),
			FilterAnimalsSetting.genericCombat(true),
			FilterBabiesSetting.genericCombat(true),
			FilterPetsSetting.genericCombat(true),
			FilterTradersSetting.genericCombat(true),
			FilterGolemsSetting.genericCombat(false),
			FilterInvisibleSetting.genericCombat(true),
			FilterNamedSetting.genericCombat(false),
			FilterShulkerBulletSetting.genericCombat(false),
			FilterArmorStandsSetting.genericCombat(true),
			FilterCrystalsSetting.genericCombat(true));

	private final CheckboxSetting depthTest = new CheckboxSetting("Depth Test (expensive)", false);

	private final SliderSetting predictMovement = new SliderSetting(
		"Predict movement",
		"Controls the strength of AimAssist's latency correction algorithm.",
		10, 0, 500, 1, ValueDisplay.INTEGER.withSuffix("ms"));

	private final SliderSetting recoilCorrection = new SliderSetting(
		"Recoil correction",
		"Controls the strength of AimAssist's recoil correction algorithm.",
		0.5, 0, 30, 0.01, ValueDisplay.DECIMAL.withSuffix("°"));


	private Entity target;
	private float nextYaw;
	private float nextPitch;
	private double maxDistance;

	public AimAssistHack() {
		super("AimAssist");
		setCategory(Category.COMBAT);

		addSetting(range);
		addSetting(rotationSpeed);
		addSetting(fov);
		addSetting(depthTest);
		addSetting(predictMovement);
		addSetting(recoilCorrection);

		entityFilters.forEach(this::addSetting);
	}

	@Override
	protected void onEnable() {
		// disable other killauras
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().crystalAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().multiAuraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}

	@Override
	protected void onDisable() {
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		target = null;
	}

	private int tick = 0;
	private double rnd1 = 0;
	private double rnd2 = 0;
	private double rnd3 = 0;
	private float speedMul = 0;

	@Override
	public void onUpdate() {
		if (tick > 5) {
			tick = 0;
			rnd1 = Math.random() / 2 - 0.25;
			rnd2 = Math.random() / 2 - 0.25;
			rnd3 = Math.random() / 2 - 0.25;
			speedMul = (float)(1 + Math.random() / 2 - 0.25);
		}
		tick++;

		maxDistance = range.getValue();
		if (maxDistance == 0) {
			maxDistance = 50;
		}

		// don't aim when a container/inventory screen is open
		if (MC.currentScreen instanceof HandledScreen)
			return;

		Stream<Entity> stream = EntityUtils.getAttackableEntities();

		double rangeSq = Math.pow(maxDistance, 2);
		stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);

		if (fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
					e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);

		stream = entityFilters.applyTo(stream);

		if (depthTest.isChecked()) {
			stream = stream.filter(e -> {
				Vec3d posOffset = e.getVelocity().multiply(
					predictMovement.getValue()*20/1000,
					predictMovement.getValue()*20/1000 / 2,
					predictMovement.getValue()*20/1000
				);

				HitResult res = MC.world.raycast(new RaycastContext(
						MC.getCameraEntity().getEyePos(),
						e.getEyePos().add(posOffset),
						RaycastContext.ShapeType.OUTLINE,
						RaycastContext.FluidHandling.NONE,
						MC.getCameraEntity()));

				if (res.getType().toString() != "BLOCK") {
					return true;
				}

				HitResult res2 = MC.world.raycast(new RaycastContext(
						MC.getCameraEntity().getEyePos(),
						e.getPos().add(posOffset).offset(Direction.UP, 0.1),
						RaycastContext.ShapeType.OUTLINE,
						RaycastContext.FluidHandling.NONE,
						MC.getCameraEntity()));

				if (res2.getType().toString() != "BLOCK") {
					return true;
				}

				return false;
			});
		}

		target = stream
				.min(Comparator.comparingDouble(e -> RotationUtils
						.getAngleToLookVec(e.getBoundingBox().getCenter())))
				.orElse(null);
	}

	private void faceEntityClient(Entity entity, Vec3d posOffset) {
		// get needed rotation
		Rotation neededHead = RotationUtils.getNeededRotations(entity.getEyePos().add(posOffset).offset(Direction.DOWN, 0.25)
				.add(rnd1, rnd2, rnd3));
		Rotation neededFeet = RotationUtils.getNeededRotations(entity.getPos().add(posOffset).offset(Direction.UP, 0.25)
				.add(rnd1, rnd2, rnd3));

		neededHead = Rotation.wrapped(neededHead.getYaw(), neededHead.getPitch()+recoilCorrection.getValueF());
		neededFeet = Rotation.wrapped(neededFeet.getYaw(), neededFeet.getPitch()+recoilCorrection.getValueF());

		if (neededHead.getPitch() > MC.player.prevPitch) {
			// turn towards head
			Rotation next = RotationUtils.slowlyTurnTowards(neededHead,
					rotationSpeed.getValueI() / 20F*speedMul);
			nextYaw = next.getYaw();
			nextPitch = next.getPitch();
		} else if (neededFeet.getPitch() < MC.player.prevPitch) {
			// turn towards feet
			Rotation next = RotationUtils.slowlyTurnTowards(neededFeet,
					rotationSpeed.getValueI() / 20F*speedMul);
			nextYaw = next.getYaw();
			nextPitch = next.getPitch();
		} else {
			// turn towards center
			Rotation next = RotationUtils.slowlyTurnTowards(neededHead,
					rotationSpeed.getValueI() / 20F*speedMul);
			nextYaw = next.getYaw();
			nextPitch = 1337;
		}
	}

	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks) {
		// don't aim when a container/inventory screen is open
		if (MC.currentScreen instanceof HandledScreen)
			return;

		if (target == null)
			return;

		WURST.getHax().autoSwordHack.setSlot();

		Vec3d posOffset = target.getVelocity().multiply(
			predictMovement.getValue()*20/1000,
			predictMovement.getValue()*20/1000 / 2,
			predictMovement.getValue()*20/1000
		);

		Rotation playerRot = Rotation.wrapped(MC.player.getYaw(), MC.player.getPitch() - recoilCorrection.getValueF()); 

		// check if already facing the entity
		if (RotationUtils.isFacingBox(target.getBoundingBox().offset(posOffset), maxDistance, playerRot)) {
			return;
		}

		faceEntityClient(target, posOffset);

		float oldYaw = MC.player.prevYaw;
		float oldPitch = MC.player.prevPitch;
		MC.player.setYaw(MathHelper.lerp(partialTicks, oldYaw, nextYaw));
		if (nextPitch != 1337) 
			MC.player.setPitch(MathHelper.lerp(partialTicks, oldPitch, nextPitch));
	}
}
