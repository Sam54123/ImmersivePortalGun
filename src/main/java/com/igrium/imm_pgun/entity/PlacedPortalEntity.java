package com.igrium.imm_pgun.entity;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.igrium.imm_pgun.ImmersivePortalGunMod.PortalColor;
import com.mojang.logging.LogUtils;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.portal.GeometryPortalShape;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalExtension;

/**
 * A portal which is active and connected to another portal.
 */
public class PlacedPortalEntity extends Portal implements IPortalEntity {

    public static final EntityType<PlacedPortalEntity> TYPE = FabricEntityTypeBuilder.create(SpawnGroup.MISC, PlacedPortalEntity::new).build();

    public PlacedPortalEntity(EntityType<?> entityType, World world) {
        super(entityType, world);

        int width = 1;
        int height = 2;

        GeometryPortalShape shape = new GeometryPortalShape();
        final int triangleNum = 30;
        double twoPi = Math.PI * 2;
        shape.triangles = IntStream.range(0, triangleNum)
            .mapToObj(i -> new GeometryPortalShape.TriangleInPlane(
                0, 0,
                width * 0.5 * Math.cos(twoPi * ((double) i) / triangleNum),
                height * 0.5 * Math.sin(twoPi * ((double) i) / triangleNum),
                width * 0.5 * Math.cos(twoPi * ((double) i + 1) / triangleNum),
                height * 0.5 * Math.sin(twoPi * ((double) i + 1) / triangleNum)
            )).collect(Collectors.toList());
            
        specialShape = shape;
    }
    
    private static final TrackedData<PortalColor> PORTAL_COLOR = DataTracker.registerData(
            PlacedPortalEntity.class, CustomTrackedDataHandlers.PORTAL_COLOR);


    private UUID linkID = UUID.randomUUID();
    private Direction direction = Direction.SOUTH;
    
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PORTAL_COLOR, PortalColor.BLUE);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("portalColor", (byte) getPortalColor().ordinal());
        nbt.putUuid("linkID", linkID);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("portalColor", NbtElement.BYTE_TYPE)) {
            int ordinal = Byte.toUnsignedInt(nbt.getByte("portalColor"));
            setPortalColor(PortalColor.values()[ordinal]);
        }
        if (nbt.contains("linkID", NbtElement.INT_ARRAY_TYPE)) {
            setLinkID(nbt.getUuid("linkID"));
        }
    }

    public PortalColor getPortalColor() {
        return dataTracker.get(PORTAL_COLOR);
    }

    protected void setPortalColor(PortalColor portalColor) {
        dataTracker.set(PORTAL_COLOR, portalColor);
    }

    @Override
    public UUID getLinkID() {
        return linkID;
    }

    public void setLinkID(UUID linkID) {
        Objects.requireNonNull(linkID);
        this.linkID = linkID;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public PlacedPortalEntity asEntity() {
        return this;
    }

    @Override
    public void tick() {
        if (world.isClient()) {
            super.tick();
            return;
        }

        PortalExtension extension = PortalExtension.get(this);
        if (extension.reversePortal != null) {
            if (!(extension.reversePortal instanceof PlacedPortalEntity)) {
                LogUtils.getLogger().error("Some dumbass managed to set the reverse portal to a standard Immersive Portals portal.");
                revertToNull();
                return;
            }
            if (extension.reversePortal.isRemoved()) {
                revertToNull();
                return;
            }
        }

        super.tick();
    }

    private void revertToNull() {
        this.remove(RemovalReason.KILLED);

        NullPortalEntity nullPortal = NullPortalEntity.TYPE.create(world);
        nullPortal.setPosition(getPos());
        nullPortal.setPitch(this.getPitch());
        nullPortal.setYaw(this.getYaw());
        nullPortal.setDirection(direction);

        nullPortal.setPortalColor(getPortalColor());
        nullPortal.setLinkID(linkID);

        world.spawnEntity(nullPortal);
    }
}
