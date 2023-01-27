package sudo.module.client;

import java.awt.Color;

import sudo.module.Mod;
import sudo.module.settings.BooleanSetting;
import sudo.module.settings.ColorSetting;
import sudo.module.settings.ModeSetting;

public class ArrylistModule extends Mod{
	
	public BooleanSetting show = new BooleanSetting("Show", true);
	public ModeSetting mode = new ModeSetting("Mode", "Original", "Original");
//	public ModeSetting mode = new ModeSetting("Mode", "Original", "Original", "Glow", "Classic");
    public BooleanSetting cute = new BooleanSetting("Cute Colors", false);
    public ColorSetting Arraycolor = new ColorSetting("Color", new Color(0xff13294B));
	public ModeSetting SortY = new ModeSetting("Sorting", "Normal", "Normal", "Reversed");
	public ModeSetting SortX = new ModeSetting("Alignment", "Left", "Left", "Right");

	public ArrylistModule() {
		super("Arraylist", "", Category.CLIENT, 0);
		addSettings(show, mode, SortX, SortY, cute, Arraycolor);
	}
}
