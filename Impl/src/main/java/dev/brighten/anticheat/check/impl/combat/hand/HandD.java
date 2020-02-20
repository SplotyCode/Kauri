package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CheckInfo(name = "Hand (D)", description = "Checks if a player places a block without looking.",
        checkType = CheckType.HAND, vlToFlag = 3, punishVL = 40, developer = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandD extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        val pos = packet.getPosition();
        if(pos != null && (pos.getX() != -1 || pos.getY() != -1 || pos.getZ() != -1)) {
            Location loc = new Location(packet.getPlayer().getWorld(), pos.getX(), pos.getY(), pos.getZ());

            Block block = BlockUtils.getBlock(loc);

            if(block == null) return;

            List<SimpleCollisionBox> boxes = new ArrayList<>();

            BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion())
                    .downCast(boxes);

            if(boxes.size() == 0) {
                debug("collided=false but will not flag since no collision boxes were found for block.");
                return;
            }

            val origin = data.playerInfo.to.clone();

            origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

            RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

            collision.draw(WrappedEnumParticle.CRIT, Collections.singletonList(packet.getPlayer()));
            boolean collided = false;

            for (SimpleCollisionBox box : boxes) {
                box.expand(0.1,0.1,0.1);

                box.draw(WrappedEnumParticle.FLAME, Collections.singletonList(packet.getPlayer()));
                if(collision.isCollided(box)) {
                    collided = true;
                    break;
                }
            }

            debug("collided=%1", collided);
        }
    }
}
