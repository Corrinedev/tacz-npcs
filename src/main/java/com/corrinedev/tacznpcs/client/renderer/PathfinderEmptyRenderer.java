package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.ScavPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PathfinderEmptyRenderer extends EntityRenderer<ScavPlayer.InternalPathfinder> {
    public PathfinderEmptyRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(ScavPlayer.InternalPathfinder pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

    }

    @Override
    public ResourceLocation getTextureLocation(ScavPlayer.InternalPathfinder pEntity) {
        return null;
    }

    @Override
    public boolean shouldRender(ScavPlayer.InternalPathfinder pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return false;
    }
}
