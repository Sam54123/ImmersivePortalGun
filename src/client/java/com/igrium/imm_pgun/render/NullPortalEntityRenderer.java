package com.igrium.imm_pgun.render;

import org.joml.Math;
import org.joml.Quaternionf;

import com.igrium.imm_pgun.entity.NullPortalEntity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NullPortalEntityRenderer extends EntityRenderer<NullPortalEntity> {

    public NullPortalEntityRenderer(Context ctx) {
        super(ctx);
    }

    private static final Identifier TEXTURE = new Identifier("immersive_portalgun", "textures/entity/portal/portalframe.png");

    @Override
    public void render(NullPortalEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentCull(TEXTURE));
        
        matrices.push();
        matrices.multiply(new Quaternionf().rotateY(Math.toRadians(-yaw)));

        RenderUtils.drawQuad(matrices.peek(), consumer, -.5f, -1f, .5f, 1f, entity.getPortalColor().getColor());

        matrices.pop();
        
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(NullPortalEntity entity) {
        return TEXTURE;
    }
    
}
