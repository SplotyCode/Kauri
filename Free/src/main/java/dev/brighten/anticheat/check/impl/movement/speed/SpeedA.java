package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (A)", description = "Minecraft code speed acceleration check.",
        checkType = CheckType.SPEED, developer = true)
@Cancellable
public class SpeedA extends Check {

    private double ldxz = .12f;
    private float friction = 0.91f;
    private float buffer;
    private double vxz;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            data.runKeepaliveAction(ka -> {
                vxz = Math.hypot(packet.getX(), packet.getZ());
                debug("set velocity: %v.3", vxz);
            });
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        checkProccesing:
        {
            if (!packet.isPos()
                    || (data.playerInfo.deltaY == 0 && data.playerInfo.deltaXZ == 0)
                    || data.playerInfo.serverPos) {
                break checkProccesing;
            }
            float drag = friction;

            TagsBuilder tags = new TagsBuilder();
            double moveFactor = data.getPlayer().getWalkSpeed() / 2f;

            moveFactor+= moveFactor * 0.3f;

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED))
                moveFactor += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED)
                        * (0.20000000298023224D)) * moveFactor;

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW))
                moveFactor += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SLOW)
                        * (-0.15000000596046448D)) * moveFactor;

            if (data.playerInfo.lClientGround) {
                tags.addTag("ground");
                drag *= 0.91f;
                moveFactor *= 0.16277136 / (drag * drag * drag);

                if (data.playerInfo.jumped) {
                    tags.addTag("jumped");
                    moveFactor += 0.2;
                }
            } else {
                tags.addTag("air");
                drag = 0.91f;
                moveFactor = 0.026f;
            }

            double ratio = (data.playerInfo.deltaXZ - ldxz) / moveFactor * 100;

            if (ratio > 100.8 && data.playerInfo.lastBrokenBlock.hasPassed(data.lagInfo.transPing + 1)
                    && data.playerInfo.liquidTimer.hasPassed(2)
                    && !data.playerInfo.generalCancel && data.playerInfo.lastVelocity.hasPassed(2)) {
                buffer+= ratio > 1000 ? 3 : 1;

                if(buffer > 5) {
                    vl++;
                    flag("p=%v.1% dxz=%v.3 aimove=%v.3 tags=%v",
                            ratio, data.playerInfo.deltaXZ, data.predictionService.aiMoveSpeed, tags.build());
                }
            } else if(buffer > 0) buffer-= 0.2f;
            debug("ratio=%v.1 tags=%v", ratio, tags.build());

            if(vxz != 0) {
                ldxz = vxz;
                vxz = 0;
            } else ldxz = data.playerInfo.deltaXZ * drag;
        }
        friction = data.blockInfo.currentFriction;
    }
}
