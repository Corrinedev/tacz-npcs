package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;

public class HumanoidScavModel extends HumanoidModel<AbstractScavEntity> {
    public HumanoidScavModel(boolean pSlim) {
        super(PlayerModel.createMesh(CubeDeformation.NONE, pSlim).getRoot().bake(64, 64));
    }

    @Override
    public void setupAnim(AbstractScavEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
    }
}
