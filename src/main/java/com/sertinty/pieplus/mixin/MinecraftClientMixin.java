package com.sertinty.pieplus.mixin;

import com.sertinty.pieplus.PieNumberInput;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 借客户端的 tick 来驱动多位数输入的等待超时。
 *
 * <p>刻意不用 {@code ClientTickEvents}：这个模组因此**不需要 Fabric API**，
 * 只依赖 Fabric Loader，装起来少一个前置。
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void pieplus$tickPendingInput(CallbackInfo ci) {
        PieNumberInput.tick((MinecraftClient) (Object) this);
    }
}
