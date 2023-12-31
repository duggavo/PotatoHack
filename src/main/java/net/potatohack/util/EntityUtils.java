/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.util;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.potatohack.PotatoHack;

public enum EntityUtils
{
	;
	
	protected static final PotatoHack WURST = PotatoHack.INSTANCE;
	protected static final MinecraftClient MC = PotatoHack.MC;
	
	public static Stream<Entity> getAttackableEntities()
	{
		return StreamSupport.stream(MC.world.getEntities().spliterator(), true)
			.filter(IS_ATTACKABLE);
	}
	
	public static Predicate<Entity> IS_ATTACKABLE = e -> e != null
		&& !e.isRemoved()
		&& (e instanceof LivingEntity && ((LivingEntity)e).getHealth() > 0
			|| e instanceof EndCrystalEntity
			|| e instanceof ShulkerBulletEntity)
		&& e != MC.player && !(e instanceof FakePlayerEntity)
		&& !WURST.getFriends().isFriend(e);
	
	public static Stream<AnimalEntity> getValidAnimals()
	{
		return StreamSupport.stream(MC.world.getEntities().spliterator(), true)
			.filter(e -> e instanceof AnimalEntity).map(e -> (AnimalEntity)e)
			.filter(IS_VALID_ANIMAL);
	}
	
	public static Predicate<AnimalEntity> IS_VALID_ANIMAL =
		a -> a != null && !a.isRemoved() && a.getHealth() > 0;
	
	public static Vec3d getLerpedPos(Entity e, float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, e.lastRenderX, e.getX());
		double y = MathHelper.lerp(partialTicks, e.lastRenderY, e.getY());
		double z = MathHelper.lerp(partialTicks, e.lastRenderZ, e.getZ());
		return new Vec3d(x, y, z);
	}
}
