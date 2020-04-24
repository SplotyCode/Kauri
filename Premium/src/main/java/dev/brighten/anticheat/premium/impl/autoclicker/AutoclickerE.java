package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.*;

@CheckInfo(name = "Autoclicker (E)", description = "Checks for hard to get deviations (Elevated/funkemunky).",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerE extends Check {

    private final List<Long> clickSamples = Collections.synchronizedList(new ArrayList<>());

    private long lastSwing;
    private double stdDelta;
    private MaxDouble verbose = new MaxDouble(10);

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delay = timeStamp - this.lastSwing;

        if (data.playerInfo.lookingAtBlock
                || data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(1))
            return;

        synchronized (clickSamples) {
            if (delay > 1L && delay < 300L && this.clickSamples.add(delay) && this.clickSamples.size() == 30) {
                double average = this.clickSamples.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

                double stdDeviation = 0.0;

                for (Long click : this.clickSamples) {
                    stdDeviation += Math.pow(click.doubleValue() - average, 2);
                }

                stdDeviation /= this.clickSamples.size();

                val std = Math.sqrt(stdDeviation);
                if (std < 15.d) {
                    if(verbose.add(std < 9. ? 2 : 1) > 4) {
                        vl++;
                        this.flag("STD: " + std);
                    }
                } else verbose.subtract(1.5);

                debug("std=%v verbose=%v", std, verbose.value());

                this.clickSamples.clear();
            }

            this.lastSwing = timeStamp;
        }
    }
}
