/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks.autofish;

import java.awt.Color;
import java.util.stream.Stream;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.potatohack.PotatoHack;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.ColorSetting;
import net.potatohack.settings.Setting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.util.EntityUtils;
import net.potatohack.util.RenderUtils;

public final class AutoFishDebugDraw
{
	private final CheckboxSetting debugDraw = new CheckboxSetting("Debug draw",
		"Shows where bites are occurring and where they will be detected. Useful for optimizing your 'Valid range' setting.",
		false);
	
	private final ColorSetting ddColor = new ColorSetting("DD color",
		"Color of the debug draw, if enabled.", Color.RED);
	
	private final SliderSetting validRange;
	private Vec3d lastSoundPos;
	
	public AutoFishDebugDraw(SliderSetting validRange)
	{
		this.validRange = validRange;
	}
	
	public Stream<Setting> getSettings()
	{
		return Stream.of(debugDraw, ddColor);
	}
	
	public void reset()
	{
		lastSoundPos = null;
	}
	
	public void updateSoundPos(PlaySoundS2CPacket sound)
	{
		lastSoundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
	}
	
	public void render(MatrixStack matrixStack, float partialTicks)
	{
		if(!debugDraw.isChecked())
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		RenderUtils.applyRegionalRenderOffset(matrixStack, regionX, regionZ);
		
		FishingBobberEntity bobber = PotatoHack.MC.player.fishHook;
		if(bobber != null)
			drawValidRange(matrixStack, partialTicks, bobber, regionX, regionZ);
		
		if(lastSoundPos != null)
			drawLastBite(matrixStack, regionX, regionZ);
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void drawValidRange(MatrixStack matrixStack, float partialTicks,
		FishingBobberEntity bobber, int regionX, int regionZ)
	{
		matrixStack.push();
		Vec3d pos = EntityUtils.getLerpedPos(bobber, partialTicks);
		matrixStack.translate(pos.getX() - regionX, pos.getY(),
			pos.getZ() - regionZ);
		
		float[] colorF = ddColor.getColorF();
		RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.5F);
		
		double vr = validRange.getValue();
		Box vrBox = new Box(-vr, -1 / 16.0, -vr, vr, 1 / 16.0, vr);
		RenderUtils.drawOutlinedBox(vrBox, matrixStack);
		
		matrixStack.pop();
	}
	
	private void drawLastBite(MatrixStack matrixStack, int regionX, int regionZ)
	{
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		
		matrixStack.push();
		matrixStack.translate(lastSoundPos.x - regionX, lastSoundPos.y,
			lastSoundPos.z - regionZ);
		
		float[] colorF = ddColor.getColorF();
		RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.5F);
		
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
			VertexFormats.POSITION);
		bufferBuilder.vertex(matrix, -0.125F, 0, -0.125F).next();
		bufferBuilder.vertex(matrix, 0.125F, 0, 0.125F).next();
		bufferBuilder.vertex(matrix, 0.125F, 0, -0.125F).next();
		bufferBuilder.vertex(matrix, -0.125F, 0, 0.125F).next();
		tessellator.draw();
		
		matrixStack.pop();
	}
}
