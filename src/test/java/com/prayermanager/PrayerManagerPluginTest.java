package com.prayermanager;

import com.prayermanager.PrayerManager;
import com.prayermanager.PrayerManager;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PrayerManagerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PrayerManager.class);
		RuneLite.main(args);
	}
}