package sudo.mixins;

import com.google.common.base.MoreObjects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import sudo.module.ModuleManager;
import sudo.module.render.ItemViewModel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @SuppressWarnings("static-access")
	@ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "STORE", ordinal = 0), index = 6)
    private float modifySwing(float swingProgress) {
        @SuppressWarnings("resource")
        Hand hand = MoreObjects.firstNonNull(MinecraftClient.getInstance().player.preferredHand, Hand.MAIN_HAND);

        if (ModuleManager.INSTANCE.getModule(ItemViewModel.class).isEnabled()) {
            if (hand == Hand.OFF_HAND) {
                return (float) (swingProgress + ModuleManager.INSTANCE.getModule(ItemViewModel.class).swingLeft.getValue());
            }
            if (hand == Hand.MAIN_HAND) {
                return (float) (swingProgress + ModuleManager.INSTANCE.getModule(ItemViewModel.class).swingRight.getValue());
            }
        }
        return swingProgress;
    }

    @SuppressWarnings("static-access")
	@Inject(at = @At("INVOKE"), method = "renderFirstPersonItem")
    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if (ModuleManager.INSTANCE.getModule(ItemViewModel.class).isEnabled()) matrices.translate(ModuleManager.INSTANCE.getModule(ItemViewModel.class).x.getValue()/2, ModuleManager.INSTANCE.getModule(ItemViewModel.class).y.getValue()/2, ModuleManager.INSTANCE.getModule(ItemViewModel.class).z.getValue()/2);

    }

}