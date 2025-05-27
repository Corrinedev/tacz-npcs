package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanoidScavRenderer extends HumanoidMobRenderer<AbstractScavEntity, HumanoidModel<AbstractScavEntity>> {
    public static final ResourceLocation testTexture = new ResourceLocation("tacz_npc", "textures/entity/bandit.png");
    public HumanoidScavRenderer(EntityRendererProvider.Context pContext, HumanoidModel<AbstractScavEntity> pModel) {
        super(pContext, pModel, 1f);
        addLayer(new HumanoidArmorLayer<>(this, pModel, pModel, pContext.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractScavEntity abstractClientPlayer) {
        return testTexture;
    }
}
