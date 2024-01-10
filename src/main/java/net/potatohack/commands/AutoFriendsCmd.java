package net.potatohack.commands;

import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.potatohack.util.EntityUtils;
import net.potatohack.command.Command;

public class AutoFriendsCmd extends Command
{
	public AutoFriendsCmd()
	{
		super("AutoFriends", "Removes all the friends, then marks nearby players as friends.\n"+
			"Very useful for team mini-games.");
	}
	
	@Override
	public void call(String[] args)
	{
		WURST.getFriends().removeAll();
		
		Stream<Entity> stream = EntityUtils.getAttackableEntities();

		stream = stream.filter(entity->{
			return entity.isPlayer() && entity.distanceTo(MC.player) < 25;
		});

		stream.forEach(e->{
			WURST.getFriends().add(e.getName().getString());
		});

		WURST.getFriends().save();
	}
}
