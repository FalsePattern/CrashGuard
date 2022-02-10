package com.falsepattern.crashguard.mixin.mixins.client;

import com.falsepattern.crashguard.CrashGuard;
import com.falsepattern.crashguard.CrashHandler;
import com.falsepattern.crashguard.ErrorTESR;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
@SideOnly(Side.CLIENT)
public abstract class TileEntityRendererDispatcherMixin {
    @Shadow public abstract TileEntitySpecialRenderer getSpecialRenderer(TileEntity p_147547_1_);

    @Redirect(method = "getSpecialRenderer",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getSpecialRendererByClass(Ljava/lang/Class;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"),
              require = 1)
    private TileEntitySpecialRenderer checkTESRBan(TileEntityRendererDispatcher instance, Class<?> clazz) {
        val tesr = instance.getSpecialRendererByClass(clazz);
        return CrashHandler.bannedTESRs.size() > 0 && CrashHandler.bannedTESRs.contains(tesr.getClass()) ? ErrorTESR.instance : tesr;
    }

    @Inject(method = "renderTileEntityAt",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"),
            require = 1)
    private void banCrashedTESR(TileEntity te, double x, double y, double z, float partialTickType, CallbackInfo ci) {
        val tesr = getSpecialRenderer(te);
        CrashHandler.bannedTESRs.add(tesr.getClass());
        CrashGuard.softCrash = true;
        CrashGuard.crashHint = new String[]{"Erroring TESR: ", tesr.getClass().getName(), te.hasWorldObj() ? ("X: " + te.xCoord + " Y: " + te.yCoord + " Z: " + te.zCoord) : "Position: maybe an item"};
    }
}
