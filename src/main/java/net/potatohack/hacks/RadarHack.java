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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.potatohack.Category;
import net.potatohack.SearchTags;
import net.potatohack.clickgui.Window;
import net.potatohack.clickgui.components.RadarComponent;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.settings.CheckboxSetting;
import net.potatohack.settings.SliderSetting;
import net.potatohack.settings.SliderSetting.ValueDisplay;
import net.potatohack.settings.filterlists.EntityFilterList;
import net.potatohack.settings.filters.FilterAnimalsSetting;
import net.potatohack.settings.filters.FilterInvisibleSetting;
import net.potatohack.settings.filters.FilterMonstersSetting;
import net.potatohack.settings.filters.FilterPlayersSetting;
import net.potatohack.settings.filters.FilterSleepingSetting;
import net.potatohack.util.FakePlayerEntity;

@SearchTags({"MiniMap", "mini map"})
public final class RadarHack extends Hack implements UpdateListener
{
	private final Window window;
	private final ArrayList<Entity> entities = new ArrayList<>();
	
	private final SliderSetting radius = new SliderSetting("Radius",
		"Radius in blocks.", 100, 1, 100, 1, ValueDisplay.INTEGER);
	private final CheckboxSetting rotate =
		new CheckboxSetting("Rotate with player", true);
	
	private final EntityFilterList entityFilters = new EntityFilterList(
		new FilterPlayersSetting("Won't show other players.", false),
		new FilterSleepingSetting("Won't show sleeping players.", false),
		new FilterMonstersSetting("Won't show zombies, creepers, etc.", false),
		new FilterAnimalsSetting("Won't show pigs, cows, etc.", false),
		new FilterInvisibleSetting("Won't show invisible entities.", false));
	
	public RadarHack()
	{
		super("Radar");
		
		setCategory(Category.RENDER);
		addSetting(radius);
		addSetting(rotate);
		entityFilters.forEach(this::addSetting);
		
		window = new Window("Radar");
		window.setPinned(true);
		window.setInvisible(true);
		window.add(new RadarComponent(this));
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		window.setInvisible(false);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		window.setInvisible(true);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		ClientWorld world = MC.world;
		
		entities.clear();
		Stream<Entity> stream =
			StreamSupport.stream(world.getEntities().spliterator(), true)
				.filter(e -> !e.isRemoved() && e != player)
				.filter(e -> !(e instanceof FakePlayerEntity))
				.filter(e -> e instanceof LivingEntity)
				.filter(e -> ((LivingEntity)e).getHealth() > 0);
		
		stream = entityFilters.applyTo(stream);
		
		entities.addAll(stream.collect(Collectors.toList()));
	}
	
	public Window getWindow()
	{
		return window;
	}
	
	public Iterable<Entity> getEntities()
	{
		return Collections.unmodifiableList(entities);
	}
	
	public double getRadius()
	{
		return radius.getValue();
	}
	
	public boolean isRotateEnabled()
	{
		return rotate.isChecked();
	}
}
