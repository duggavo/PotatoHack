/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.potatohack.event.EventManager;
import net.potatohack.events.VelocityFromEntityCollisionListener.VelocityFromEntityCollisionEvent;
import net.potatohack.events.VelocityFromFluidListener.VelocityFromFluidEvent;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, CommandOutput
{
	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
		opcode = Opcodes.INVOKEVIRTUAL,
		ordinal = 0),
		method = "updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z")
	private void setVelocityFromFluid(Entity entity, Vec3d velocity)
	{
		VelocityFromFluidEvent event =
			new VelocityFromFluidEvent((Entity)(Object)this);
		EventManager.fire(event);
		
		if(!event.isCancelled())
			entity.setVelocity(velocity);
	}
	
	@Inject(at = @At("HEAD"),
		method = "Lnet/minecraft/entity/Entity;pushAwayFrom(Lnet/minecraft/entity/Entity;)V",
		cancellable = true)
	private void onPushAwayFrom(Entity entity, CallbackInfo ci)
	{
		VelocityFromEntityCollisionEvent event =
			new VelocityFromEntityCollisionEvent((Entity)(Object)this);
		EventManager.fire(event);
		
		if(event.isCancelled())
			ci.cancel();
	}
}
