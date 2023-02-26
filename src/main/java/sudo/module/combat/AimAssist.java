package sudo.module.combat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import sudo.module.Mod;
import sudo.utils.player.RotationUtils;

public class AimAssist extends Mod {

	static PlayerEntity target = null;
	
	public AimAssist() {
		super("AimAssist", "Automatically aims for you", Category.COMBAT, 0);
	}
	
	
	@Override
	public void onTick() {
		
		HitResult hit = mc.crosshairTarget;
		
		if (mc.player != null) {
			if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
			    if (((EntityHitResult) hit).getEntity() instanceof PlayerEntity player) {
			        target = player;
			    }
			} else if (target == null) return;
			
			int maxDistance = 8;
			
			if (!(target == null)) {
				if (target.isDead() || mc.player.squaredDistanceTo(target) > maxDistance) target = null;
			}
			
			if (target != null) {
				mc.player.setYaw(newYAW());
				mc.player.setPitch(newPITCH());
			}
		}
		
		super.onTick();
	}


	private float newYAW() {
		return RotationUtils.getRotationFromPosition((double) target.getX(), (double) target.getZ(), (double) target.getY()+1)[0];
	}
	
	private float newPITCH() {
		return RotationUtils.getRotationFromPosition((double) target.getX(), (double) target.getZ(), (double) target.getY()+1)[1];
	}
}