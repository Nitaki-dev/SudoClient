package sudo.ui.screens.clickgui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import sudo.module.Mod.Category;

public class ClickGUI extends Screen {

	public static final ClickGUI INSTANCE = new ClickGUI();

	private List<Frame> frames;
	
	private ClickGUI() {
		super(Text.literal("Click GUI"));

		frames = new ArrayList<>();
		
		int offset = 10;
		for (Category category : Category.values()) {
			frames.add(new Frame(category, offset, 20, 100, 15));
			offset += 110;
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		for (Frame frame : frames) {
			frame.render(matrices, mouseX, mouseY, delta);
		}
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (Frame frame : frames) {
			frame.mouseClicked(mouseX, mouseY, button);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}