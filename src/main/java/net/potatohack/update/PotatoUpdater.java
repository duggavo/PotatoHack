/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.potatohack.update;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.potatohack.PotatoHack;
import net.potatohack.events.UpdateListener;
import net.potatohack.util.ChatUtils;
import net.potatohack.util.json.JsonException;
import net.potatohack.util.json.JsonUtils;
import net.potatohack.util.json.WsonArray;
import net.potatohack.util.json.WsonObject;

public final class PotatoUpdater implements UpdateListener
{
	private Thread thread;
	private boolean outdated;
	private Text component;
	
	@Override
	public void onUpdate()
	{
		if(thread == null)
		{
			thread = new Thread(this::checkForUpdates, "PotatoUpdater");
			thread.start();
			return;
		}
		
		if(thread.isAlive())
			return;
		
		if(component != null)
			ChatUtils.component(component);
		
		PotatoHack.INSTANCE.getEventManager().remove(UpdateListener.class,
			this);
	}
	
	public void checkForUpdates()
	{
		Version currentVersion = new Version(PotatoHack.VERSION);
		Version latestVersion = null;
		
		try
		{
			WsonArray wson = JsonUtils.parseURLToArray(
				"https://api.github.com/repos/duggavo/PotatoHack/releases");
			
			for(WsonObject release : wson.getAllObjects())
			{
				if(!currentVersion.isPreRelease()
					&& release.getBoolean("prerelease"))
					continue;
				
				if(!containsCompatibleAsset(release.getArray("assets")))
					continue;
				
				String tagName = release.getString("tag_name");
				latestVersion = new Version(tagName.substring(1));
				break;
			}
			
			if(latestVersion == null)
				throw new NullPointerException("Latest version is missing!");
			
			System.out.println("[Updater] Current version: " + currentVersion);
			System.out.println("[Updater] Latest version: " + latestVersion);
			outdated = currentVersion.shouldUpdateTo(latestVersion);
			
		}catch(Exception e)
		{
			System.err.println("[Updater] An error occurred!");
			e.printStackTrace();
		}
		
		if(latestVersion == null || latestVersion.isInvalid())
		{
			String text = "An error occurred while checking for updates."
				+ " Click \u00a7nhere\u00a7r to check manually.";
			String url =
				"https://potatohack.net/potato_chat/error_checking_updates";
			showLink(text, url);
			return;
		}
		
		if(!outdated)
			return;
		
		String textPart1 = "Potato " + latestVersion + " MC"
			+ PotatoHack.MC_VERSION + " is now available.";
		String text =
			textPart1 + " Click \u00a7nhere\u00a7r to download the update.";
		String url =
			"https://potatohack.net/potato_chat/download"
				+ URLEncoder.encode(textPart1, StandardCharsets.UTF_8);
		showLink(text, url);
	}
	
	private void showLink(String text, String url)
	{
		ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
		component = Text.literal(text).styled(s -> s.withClickEvent(event));
	}
	
	private boolean containsCompatibleAsset(WsonArray wsonArray)
		throws JsonException
	{
		String compatibleSuffix = "MC" + PotatoHack.MC_VERSION + ".jar";
		
		for(WsonObject asset : wsonArray.getAllObjects())
		{
			String assetName = asset.getString("name");
			if(!assetName.endsWith(compatibleSuffix))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	public boolean isOutdated()
	{
		return outdated;
	}
}
