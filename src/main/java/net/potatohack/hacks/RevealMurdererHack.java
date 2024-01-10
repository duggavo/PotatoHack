/*
 * Copyright (c) 2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.hack.Hack;
import net.potatohack.util.ChatUtils;
import net.potatohack.events.UpdateListener;
import net.potatohack.events.RenderListener;
import net.potatohack.util.FakePlayerEntity;
import net.potatohack.util.RenderUtils;
import net.potatohack.util.RotationUtils;

@SearchTags({"murder mystery"})
public final class RevealMurdererHack extends Hack
	implements UpdateListener, RenderListener
{
	private static Set<Item> knifeItems = Stream.of(
		Items.IRON_SWORD,
		Items.STONE_SWORD,
		Items.IRON_SHOVEL,
		Items.STICK,
		Items.WOODEN_AXE,
		Items.WOODEN_SWORD,
		Item.fromBlock(Blocks.DEAD_BUSH),
		Items.STONE_SHOVEL,
		Items.BLAZE_ROD,
		Items.DIAMOND_SHOVEL,
		Items.FEATHER,
		Items.PUMPKIN_PIE,
		Items.GOLDEN_PICKAXE,
		Items.APPLE,
		Items.NAME_TAG,
		Item.fromBlock(Blocks.SPONGE),
		Items.CARROT_ON_A_STICK,
		Items.BONE,
		Items.CARROT,
		Items.GOLDEN_CARROT,
		Items.COOKIE,
		Items.DIAMOND_AXE,
		Items.GOLDEN_SWORD,
		Items.DIAMOND_SWORD,
		Items.DIAMOND_HOE,
		Items.SHEARS,
		Items.SALMON,
		Items.COOKED_BEEF,
		Items.COOKED_COD,
		Items.COOKED_MUTTON,
		Item.fromBlock(Blocks.REDSTONE_TORCH)
	).collect(Collectors.toSet());

	private static Set<Item> bowItems = Stream.of(
		Items.BOW,
		Items.CROSSBOW
	).collect(Collectors.toSet());

	private Set<PlayerEntity> muderers = new HashSet<>();
	private Set<PlayerEntity> detectives = new HashSet<>();

	public RevealMurdererHack()
	{
		super("RevealMurderer");
		
		setCategory(Category.OTHER);
	}
	
	@Override
	public void onEnable()
	{
		muderers.clear();
		detectives.clear();

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		muderers.clear();
		detectives.clear();

		// not really required, but can help discarding unused memory
		murderPlayers.clear();
		detectivePlayers.clear();


		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}

	private final ArrayList<PlayerEntity> murderPlayers = new ArrayList<>();
	private final ArrayList<PlayerEntity> detectivePlayers = new ArrayList<>();

	@Override
	public void onUpdate()
	{
		murderPlayers.clear();
		detectivePlayers.clear();
		
		MC.world.getPlayers()
			.parallelStream().filter(e -> !e.isRemoved() && e.getHealth() > 0)
			.filter(e -> e != MC.player)
			.filter(e -> !(e instanceof FakePlayerEntity))
			.filter(e -> Math.abs(e.getY() - MC.player.getY()) <= 1e6)
			.forEach((entity)->{
				if (muderers.contains(entity)) {
					murderPlayers.add(entity);
					return;
				} else if (detectives.contains(entity)) {
					detectivePlayers.add(entity);
					return;
				}

				Item item = entity.getStackInHand(Hand.MAIN_HAND).getItem();

				PlayerEntity player = (PlayerEntity)entity;

				if (knifeItems.contains(item)) {
					muderers.add(player);
					ChatUtils.message(player.getName().getString() + " is a murderer. (knife: " + item.getName().getString()  + ")");
				} else if (bowItems.contains(item)) {
					detectives.add(player);
					ChatUtils.message(player.getName().getString() + " is a detective. (bow: " + item.getName().getString()  + ")");
				}
			});
	}

	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		RenderUtils.applyRegionalRenderOffset(matrixStack, regionX, regionZ);
		
		// draw boxes
		renderBoxes(matrixStack, partialTicks, regionX, regionZ);
		
		renderTracers(matrixStack, partialTicks, regionX, regionZ);
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void renderBoxes(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		float extraSize = 0.25F;
		
		for(PlayerEntity e : murderPlayers)
		{
			matrixStack.push();
			
			matrixStack.translate(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			matrixStack.scale(e.getWidth() + extraSize,
				e.getHeight() + extraSize, e.getWidth() + extraSize);
			
			// set color
			float f = Math.max(Math.min(2F - MC.player.distanceTo(e) / 20F, 1F), 0.25F);
			RenderSystem.setShaderColor(1F, 1F, 0, f);			
	
			Box bb = new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);
			RenderUtils.drawOutlinedBox(bb, matrixStack);
			
			matrixStack.pop();
		}
		for(PlayerEntity e : detectivePlayers)
		{
			matrixStack.push();
			
			matrixStack.translate(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			matrixStack.scale(e.getWidth() + extraSize,
				e.getHeight() + extraSize, e.getWidth() + extraSize);
			
			// set color
			float f = Math.max(Math.min(2F - MC.player.distanceTo(e) / 20F, 1F), 0.25F);
			RenderSystem.setShaderColor(0.5F, 0.5F, 1F, f);			
	
			Box bb = new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);
			RenderUtils.drawOutlinedBox(bb, matrixStack);
			
			matrixStack.pop();
		}
	}
	
	private void renderTracers(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
			VertexFormats.POSITION_COLOR);
		
		Vec3d start = RotationUtils.getClientLookVec()
			.add(RenderUtils.getCameraPos()).subtract(regionX, 0, regionZ);
		
		for(PlayerEntity e : murderPlayers)
		{
			Vec3d interpolationOffset = new Vec3d(e.getX(), e.getY(), e.getZ())
				.subtract(e.prevX, e.prevY, e.prevZ).multiply(1 - partialTicks);
			
			Vec3d end = e.getBoundingBox().getCenter()
				.subtract(interpolationOffset).subtract(regionX, 0, regionZ);
			
			float r, g, b;
			
			if(WURST.getFriends().contains(e.getEntityName()))
			{
				r = 0;
				g = 0;
				b = 1;
				
			}else
			{
				float f = MC.player.distanceTo(e) / 20F;
				r = MathHelper.clamp(2 - f, 0, 1);
				g = MathHelper.clamp(f, 0, 1);
				b = 0;
			}
			
			bufferBuilder
				.vertex(matrix, (float)start.x, (float)start.y, (float)start.z)
				.color(r, g, b, 0.5F).next();
			
			bufferBuilder
				.vertex(matrix, (float)end.x, (float)end.y, (float)end.z)
				.color(r, g, b, 0.5F).next();
		}
		
		tessellator.draw();
	}


}