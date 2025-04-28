package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;

import java.util.List;
import java.util.Map;

public class HumanoidScavModel extends HumanoidModel<AbstractScavEntity> {
    public HumanoidScavModel(boolean pSlim) {
        super(PlayerModel.createMesh(CubeDeformation.NONE, pSlim).getRoot().bake(64, 64));
    }
}
