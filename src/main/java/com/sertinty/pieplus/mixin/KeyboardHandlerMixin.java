package com.sertinty.pieplus.mixin;

import com.sertinty.pieplus.PieNumberInput;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 借键盘处理器自己的 tick 来驱动「Shift 松开就提交」。
 *
 * <p>用 {@code KeyboardHandler.tick()} 而不是 Fabric API 的 {@code ClientTickEvents}，
 * 这个模组因此**不需要 Fabric API**，只依赖 Fabric Loader。
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void pieplus$tickPendingInput(CallbackInfo ci) {
        PieNumberInput.tick();
    }
}
