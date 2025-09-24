package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public class HumanoidScavRenderer extends HumanoidMobRenderer<AbstractScavEntity, HumanoidModel<AbstractScavEntity>> {
    public static final ResourceLocation testTexture = new ResourceLocation("tacz_npc", "textures/entity/bandit.png");
    public final HumanoidArmorLayer<AbstractScavEntity, HumanoidModel<AbstractScavEntity>, HumanoidModel<AbstractScavEntity>> slimLayer;
    public final HumanoidArmorLayer<AbstractScavEntity, HumanoidModel<AbstractScavEntity>, HumanoidModel<AbstractScavEntity>> thickLayer;

    public HumanoidScavRenderer(EntityRendererProvider.Context pContext, HumanoidModel<AbstractScavEntity> pModel) {
        super(pContext, pModel, 0.5f);

        thickLayer = new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), pContext.getModelManager());
        slimLayer = new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), pContext.getModelManager());
    }

    @Override
    public void render(AbstractScavEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if(pEntity.isSlim() && !layers.contains(slimLayer) && !layers.contains(thickLayer)) {
            addLayer(slimLayer);
        } else if (!layers.contains(thickLayer) && !layers.contains(slimLayer)) {
            addLayer(thickLayer);
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractScavEntity abstractClientPlayer) {
        return testTexture;
    }
}
