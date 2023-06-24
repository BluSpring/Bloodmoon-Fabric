package lumien.bloodmoon.mixin;

import lumien.bloodmoon.client.ClientBloodmoonHandler;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @ModifyArgs(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    public void manipulateRgb(Args args) {
        int argb = args.get(2);

        int pos = args.get(1);
        var b = ClientBloodmoonHandler.INSTANCE.manipulateBlue(pos, argb & 255);
        var g = ClientBloodmoonHandler.INSTANCE.manipulateGreen(pos, (argb >> 8) & 255);
        var r = ClientBloodmoonHandler.INSTANCE.manipulateRed(pos, (argb >> 16) & 255);

        args.set(2, -16777216 | b << 16 | g << 8 | r);
    }
}
