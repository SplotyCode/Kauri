package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@CheckInfo(name = "Motion (Type A)", description = "Looks for illegitimate omni-directional sprinting.", type = CheckType.MOTION, maxVL = 50, executable = true)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@Init
public class MotionA extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val action = getData().getActionProcessor();
        val move = getData().getMovementProcessor();
        val player = getData().getPlayer();

        if(action.isSprinting() && player.isSprinting() && !getData().isGeneralCancel()) {
            val direction = move.getTo().toLocation(player.getWorld()).getDirection().setY(0);
            val moveVector = new Vector(move.getTo().getX() - move.getFrom().getX(), 0, move.getTo().getZ() - move.getFrom().getZ());

            val diff = direction.distance(moveVector);

            if(diff > 1.05) {
                if(vl++ > 15) {
                    flag(diff + ">-1", true, true, AlertTier.LIKELY);
                }
            } else vl-= vl > 0 ? 3 : 0;

            debug("diff=" + diff + " vl=" + vl);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
