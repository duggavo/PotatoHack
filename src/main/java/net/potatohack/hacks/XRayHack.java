/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.clickgui.screens.EditBlockListScreen;
import net.potatohack.events.GetAmbientOcclusionLightLevelListener;
import net.potatohack.events.RenderBlockEntityListener;
import net.potatohack.events.SetOpaqueCubeListener;
import net.potatohack.events.ShouldDrawSideListener;
import net.potatohack.events.TesselateBlockListener;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.mixinterface.ISimpleOption;
import net.potatohack.settings.BlockListSetting;
import net.potatohack.util.BlockUtils;
import net.potatohack.util.ChatUtils;

@SearchTags({"XRay", "x ray", "OreFinder", "ore finder"})
public final class XRayHack extends Hack implements UpdateListener,
	SetOpaqueCubeListener, GetAmbientOcclusionLightLevelListener,
	ShouldDrawSideListener, TesselateBlockListener, RenderBlockEntityListener
{
	private final BlockListSetting ores = new BlockListSetting("Ores", "",
		"minecraft:ancient_debris", "minecraft:anvil", "minecraft:beacon",
		"minecraft:bone_block", "minecraft:bookshelf",
		"minecraft:brewing_stand", "minecraft:chain_command_block",
		"minecraft:chest", "minecraft:clay", "minecraft:coal_block",
		"minecraft:coal_ore", "minecraft:command_block", "minecraft:copper_ore",
		"minecraft:crafting_table", "minecraft:deepslate_coal_ore",
		"minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore",
		"minecraft:deepslate_emerald_ore", "minecraft:deepslate_gold_ore",
		"minecraft:deepslate_iron_ore", "minecraft:deepslate_lapis_ore",
		"minecraft:deepslate_redstone_ore", "minecraft:diamond_block",
		"minecraft:diamond_ore", "minecraft:dispenser", "minecraft:dropper",
		"minecraft:emerald_block", "minecraft:emerald_ore",
		"minecraft:enchanting_table", "minecraft:end_portal",
		"minecraft:end_portal_frame", "minecraft:ender_chest",
		"minecraft:furnace", "minecraft:glowstone", "minecraft:gold_block",
		"minecraft:gold_ore", "minecraft:hopper", "minecraft:iron_block",
		"minecraft:iron_ore", "minecraft:ladder", "minecraft:lapis_block",
		"minecraft:lapis_ore", "minecraft:lava", "minecraft:lodestone",
		"minecraft:mossy_cobblestone", "minecraft:nether_gold_ore",
		"minecraft:nether_portal", "minecraft:nether_quartz_ore",
		"minecraft:raw_copper_block", "minecraft:raw_gold_block",
		"minecraft:raw_iron_block", "minecraft:redstone_block",
		"minecraft:redstone_ore", "minecraft:repeating_command_block",
		"minecraft:spawner", "minecraft:suspicious_gravel",
		"minecraft:suspicious_sand", "minecraft:tnt", "minecraft:torch",
		"minecraft:trapped_chest", "minecraft:water");
	
	private ArrayList<String> oreNames;
	private final String warning;
	
	private final String renderName =
		Math.random() < 0.01 ? "X-Potato" : getName();
	
	public XRayHack()
	{
		super("X-Ray");
		setCategory(Category.RENDER);
		addSetting(ores);
		
		List<String> mods = FabricLoader.getInstance().getAllMods().stream()
			.map(ModContainer::getMetadata).map(ModMetadata::getId)
			.collect(Collectors.toList());
		
		Pattern optifine = Pattern.compile("opti(?:fine|fabric).*");
		
		if(mods.stream().anyMatch(optifine.asPredicate()))
			warning = "OptiFine is installed. X-Ray will not work properly!";
		else
			warning = null;
	}
	
	@Override
	public String getRenderName()
	{
		return renderName;
	}
	
	@Override
	public void onEnable()
	{
		oreNames = new ArrayList<>(ores.getBlockNames());
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(SetOpaqueCubeListener.class, this);
		EVENTS.add(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.add(ShouldDrawSideListener.class, this);
		EVENTS.add(TesselateBlockListener.class, this);
		EVENTS.add(RenderBlockEntityListener.class, this);
		MC.worldRenderer.reload();
		
		if(warning != null)
			ChatUtils.warning(warning);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(SetOpaqueCubeListener.class, this);
		EVENTS.remove(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.remove(ShouldDrawSideListener.class, this);
		EVENTS.remove(TesselateBlockListener.class, this);
		EVENTS.remove(RenderBlockEntityListener.class, this);
		MC.worldRenderer.reload();
		
		@SuppressWarnings("unchecked")
		ISimpleOption<Double> gammaOption =
			(ISimpleOption<Double>)(Object)MC.options.getGamma();
		
		if(!WURST.getHax().fullbrightHack.isEnabled())
			gammaOption.forceSetValue(WURST.getHax().fullbrightHack.defaultGamma.getValue());
	}
	
	@Override
	public void onUpdate()
	{
		@SuppressWarnings("unchecked")
		ISimpleOption<Double> gammaOption =
			(ISimpleOption<Double>)(Object)MC.options.getGamma();
		
		gammaOption.forceSetValue(16.0);
	}
	
	@Override
	public void onSetOpaqueCube(SetOpaqueCubeEvent event)
	{
		event.cancel();
	}
	
	@Override
	public void onGetAmbientOcclusionLightLevel(
		GetAmbientOcclusionLightLevelEvent event)
	{
		event.setLightLevel(1);
	}
	
	@Override
	public void onShouldDrawSide(ShouldDrawSideEvent event)
	{
		event.setRendered(isVisible(event.getState().getBlock()));
	}
	
	@Override
	public void onTesselateBlock(TesselateBlockEvent event)
	{
		if(!isVisible(event.getState().getBlock()))
			event.cancel();
	}
	
	@Override
	public void onRenderBlockEntity(RenderBlockEntityEvent event)
	{
		if(!isVisible(BlockUtils.getBlock(event.getBlockEntity().getPos())))
			event.cancel();
	}
	
	public void openBlockListEditor(Screen prevScreen)
	{
		MC.setScreen(new EditBlockListScreen(prevScreen, ores));
	}
	
	private boolean isVisible(Block block)
	{
		String name = BlockUtils.getName(block);
		int index = Collections.binarySearch(oreNames, name);
		return index >= 0;
	}
}
