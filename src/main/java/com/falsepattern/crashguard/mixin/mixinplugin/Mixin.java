package com.falsepattern.crashguard.mixin.mixinplugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.*;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    //region Minecraft->client
        ClientMinecraftMixin(Side.CLIENT, always(), "MinecraftMixin"),
        TessellatorMixin(Side.CLIENT, always(), "TessellatorMixin"),
        TileEntityRendererDispatcherMixin(Side.CLIENT, always(), "TileEntityRendererDispatcherMixin"),
    //endregion Minecraft->client
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

