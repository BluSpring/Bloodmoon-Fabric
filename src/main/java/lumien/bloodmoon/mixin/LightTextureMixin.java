package lumien.bloodmoon.mixin;

import lumien.bloodmoon.client.ClientBloodmoonHandler;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @ModifyArgs(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    public void manipulateRgb(Args args) {
        int pos = args.get(1);
        int original = args.get(2);
        var r = ClientBloodmoonHandler.INSTANCE.manipulateRed(pos, FastColor.ARGB32.blue(original));
        var g = ClientBloodmoonHandler.INSTANCE.manipulateGreen(pos, FastColor.ARGB32.green(original));
        var b = ClientBloodmoonHandler.INSTANCE.manipulateBlue(pos, FastColor.ARGB32.red(original));

        args.set(2, FastColor.ARGB32.color(0xFF, b, g, r));
    }
}
