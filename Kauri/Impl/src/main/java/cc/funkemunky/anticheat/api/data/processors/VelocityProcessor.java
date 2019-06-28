package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import cc.funkemunky.api.utils.TickTimer;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

@Getter
@Setter
public class VelocityProcessor {
    private float maxVertical, maxHorizontal, motionX, motionY, motionZ, lastMotionX, lastMotionY, lastMotionZ;
    public double velocityX, velocityY, velocityZ;
    private PlayerData data;
    private long lastVelocityTimestamp, velocityTicks;
    private boolean attackedSinceVelocity;
    private TickTimer lastVelocity = new TickTimer(40);

    public VelocityProcessor(PlayerData data) {
        this.data = data;
    }

    public void update(WrappedOutVelocityPacket packet) {
        maxVertical = motionY = (float) packet.getY();
        maxHorizontal = (float) MiscUtils.hypot(packet.getX(), packet.getZ());

        if (packet.getId() == packet.getPlayer().getEntityId() && (MathUtils.hypot(packet.getX(), packet.getZ()) > 1E-4 || Math.abs(packet.getY()) > 1E-5)) {
            lastVelocity.reset();
            lastVelocityTimestamp = System.currentTimeMillis();
        }

        if (data.getMovementProcessor().isClientOnGround() && data.getMovementProcessor().getFrom().getY() % 1 == 0) {
            velocityX = packet.getX();
            velocityY = packet.getY();

            data.getMovementProcessor().setServerYVelocity(data.getMovementProcessor().getServerYVelocity() + (float) velocityY);
            velocityZ = packet.getZ();
        }

        motionX = (float) packet.getX();
        motionZ = (float) packet.getZ();
        velocityTicks = 0;
    }

    public void update(WrappedInFlyingPacket packet) {
        var motionX = this.motionX;
        var motionZ = this.motionZ;
        var motionY = this.motionY;

        if(velocityTicks > 1) {
            var multiplier = 0.91f;

            if (packet.isGround()) multiplier*= getData().getBlockBelow() != null ? ReflectionsUtil.getFriction(getData().getBlockBelow()) : 0.6;

            motionX *= multiplier;
            motionZ *= multiplier;

            if (packet.isGround()) {
                motionY = 0;
            } else if (motionY > 0) {
                motionY -= 0.08f;
                motionY *= 0.98f;
            }

            if (motionY < 0.0005) {
                motionY = 0;
            }
            if (motionX < 0.0005) {
                motionX = 0;
            }

            if (motionZ < 0.0005) {
                motionZ = 0;
            }
        }

        lastMotionX = this.motionX;
        lastMotionY = this.motionY;
        lastMotionZ = this.motionZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        velocityTicks++;
    }

    public void update(WrappedInUseEntityPacket packet) {
        if (!attackedSinceVelocity) {
            velocityX *= 0.6;
            velocityY *= 0.6;
            velocityZ *= 0.6;
            attackedSinceVelocity = true;
        }
    }

    public boolean isTakingVelocity() {
        return motionY > 0 || MiscUtils.hypot(motionX, motionZ) > 1E-4;
    }
}
