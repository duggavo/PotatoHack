/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.potatohack.altmanager.AltManager;
import net.potatohack.clickgui.ClickGui;
import net.potatohack.command.CmdList;
import net.potatohack.command.CmdProcessor;
import net.potatohack.command.Command;
import net.potatohack.event.EventManager;
import net.potatohack.events.ChatOutputListener;
import net.potatohack.events.GUIRenderListener;
import net.potatohack.events.KeyPressListener;
import net.potatohack.events.PostMotionListener;
import net.potatohack.events.PreMotionListener;
import net.potatohack.events.UpdateListener;
import net.potatohack.hack.Hack;
import net.potatohack.hack.HackList;
import net.potatohack.hud.IngameHUD;
import net.potatohack.keybinds.KeybindList;
import net.potatohack.keybinds.KeybindProcessor;
import net.potatohack.mixinterface.IMinecraftClient;
import net.potatohack.navigator.Navigator;
import net.potatohack.other_feature.OtfList;
import net.potatohack.other_feature.OtherFeature;
import net.potatohack.settings.SettingsFile;
import net.potatohack.update.ProblematicResourcePackDetector;
import net.potatohack.update.PotatoUpdater;
import net.potatohack.util.json.JsonException;

public enum PotatoHack
{
	INSTANCE;
	
	public static MinecraftClient MC;
	public static IMinecraftClient IMC;
	
	public static final String VERSION = "1.0.0";
	public static final String MC_VERSION = "1.20.1";
	
	private EventManager eventManager;
	private AltManager altManager;
	private HackList hax;
	private CmdList cmds;
	private OtfList otfs;
	private SettingsFile settingsFile;
	private Path settingsProfileFolder;
	private KeybindList keybinds;
	private ClickGui gui;
	private Navigator navigator;
	private CmdProcessor cmdProcessor;
	private IngameHUD hud;
	private RotationFaker rotationFaker;
	private FriendsList friends;
	
	private boolean enabled = true;
	private static boolean guiInitialized;
	private PotatoUpdater updater;
	private ProblematicResourcePackDetector problematicPackDetector;
	private Path wurstFolder;
	
	private KeyBinding zoomKey;
	
	public void initialize()
	{
		System.out.println("Starting PotatoHack...");
		
		MC = MinecraftClient.getInstance();
		IMC = (IMinecraftClient)MC;
		wurstFolder = createWurstFolder();
				
		eventManager = new EventManager(this);
		
		Path enabledHacksFile = wurstFolder.resolve("enabled-hacks.json");
		hax = new HackList(enabledHacksFile);
		
		cmds = new CmdList();
		
		otfs = new OtfList();
		
		Path settingsFile = wurstFolder.resolve("settings.json");
		settingsProfileFolder = wurstFolder.resolve("settings");
		this.settingsFile = new SettingsFile(settingsFile, hax, cmds, otfs);
		this.settingsFile.load();
		hax.tooManyHaxHack.loadBlockedHacksFile();
		
		Path keybindsFile = wurstFolder.resolve("keybinds.json");
		keybinds = new KeybindList(keybindsFile);
		
		Path guiFile = wurstFolder.resolve("windows.json");
		gui = new ClickGui(guiFile);
		
		Path preferencesFile = wurstFolder.resolve("preferences.json");
		navigator = new Navigator(preferencesFile, hax, cmds, otfs);
		
		Path friendsFile = wurstFolder.resolve("friends.json");
		friends = new FriendsList(friendsFile);
		friends.load();
		
		cmdProcessor = new CmdProcessor(cmds);
		eventManager.add(ChatOutputListener.class, cmdProcessor);
		
		KeybindProcessor keybindProcessor =
			new KeybindProcessor(hax, keybinds, cmdProcessor);
		eventManager.add(KeyPressListener.class, keybindProcessor);
		
		hud = new IngameHUD();
		eventManager.add(GUIRenderListener.class, hud);
		
		rotationFaker = new RotationFaker();
		eventManager.add(PreMotionListener.class, rotationFaker);
		eventManager.add(PostMotionListener.class, rotationFaker);
		
		updater = new PotatoUpdater();
		eventManager.add(UpdateListener.class, updater);
		
		problematicPackDetector = new ProblematicResourcePackDetector();
		problematicPackDetector.start();
		
		Path altsFile = wurstFolder.resolve("alts.encrypted_json");
		Path encFolder =
			Paths.get(System.getProperty("user.home"), ".Wurst encryption")
				.normalize();
		altManager = new AltManager(altsFile, encFolder);
		
		zoomKey = new KeyBinding("key.wurst.zoom", InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "Zoom");
		KeyBindingHelper.registerKeyBinding(zoomKey);
	}
	
	private Path createWurstFolder()
	{
		Path dotMinecraftFolder = MC.runDirectory.toPath().normalize();
		Path wurstFolder = dotMinecraftFolder.resolve("potato");
		
		try
		{
			Files.createDirectories(wurstFolder);
			
		}catch(IOException e)
		{
			throw new RuntimeException(
				"Couldn't create .minecraft/potato folder.", e);
		}
		
		return wurstFolder;
	}
	
	public String translate(String key)
	{
		if(otfs.translationsOtf.getForceEnglish().isChecked())
			return IMC.getLanguageManager().getEnglish().get(key);
			
		// This extra check is necessary because I18n.translate() doesn't
		// always return the key when the translation is missing. If the key
		// contains a '%', it will return "Format Error: key" instead.
		if(!I18n.hasTranslation(key))
			return key;
		
		return I18n.translate(key);
	}
	
	public EventManager getEventManager()
	{
		return eventManager;
	}
	
	public void saveSettings()
	{
		settingsFile.save();
	}
	
	public ArrayList<Path> listSettingsProfiles()
	{
		if(!Files.isDirectory(settingsProfileFolder))
			return new ArrayList<>();
		
		try(Stream<Path> files = Files.list(settingsProfileFolder))
		{
			return files.filter(Files::isRegularFile)
				.collect(Collectors.toCollection(ArrayList::new));
			
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void loadSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.loadProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public void saveSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.saveProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public HackList getHax()
	{
		return hax;
	}
	
	public CmdList getCmds()
	{
		return cmds;
	}
	
	public OtfList getOtfs()
	{
		return otfs;
	}
	
	public Feature getFeatureByName(String name)
	{
		Hack hack = getHax().getHackByName(name);
		if(hack != null)
			return hack;
		
		Command cmd = getCmds().getCmdByName(name.substring(1));
		if(cmd != null)
			return cmd;
		
		OtherFeature otf = getOtfs().getOtfByName(name);
		return otf;
	}
	
	public KeybindList getKeybinds()
	{
		return keybinds;
	}
	
	public ClickGui getGui()
	{
		if(!guiInitialized)
		{
			guiInitialized = true;
			gui.init();
		}
		
		return gui;
	}
	
	public Navigator getNavigator()
	{
		return navigator;
	}
	
	public CmdProcessor getCmdProcessor()
	{
		return cmdProcessor;
	}
	
	public IngameHUD getHud()
	{
		return hud;
	}
	
	public RotationFaker getRotationFaker()
	{
		return rotationFaker;
	}
	
	public FriendsList getFriends()
	{
		return friends;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if(!enabled)
		{
			hax.panicHack.setEnabled(true);
			hax.panicHack.onUpdate();
		}
	}
	
	public PotatoUpdater getUpdater()
	{
		return updater;
	}
	
	public ProblematicResourcePackDetector getProblematicPackDetector()
	{
		return problematicPackDetector;
	}
	
	public Path getWurstFolder()
	{
		return wurstFolder;
	}
	
	public KeyBinding getZoomKey()
	{
		return zoomKey;
	}
	
	public AltManager getAltManager()
	{
		return altManager;
	}
}
