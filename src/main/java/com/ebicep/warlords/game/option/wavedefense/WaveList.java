package com.ebicep.warlords.game.option.wavedefense;

import com.ebicep.warlords.player.WarlordsEntity;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.bukkit.Location;

public interface WaveList {

    Wave getWave(int wave, Random random);

    default WaveList prependMapper(BiFunction<Wave, Integer, Wave> mapper) {
        return (wave, random) -> mapper.apply(this.getWave(wave, random), wave);
    }

    default WaveList prependEntityMapper(BiFunction<WarlordsEntity, Integer, WarlordsEntity> mapper) {
        return prependMapper((w, counter) -> new DelegatingWave(w) {
            @Override
            public PartialMonster spawnRandomMonster(Location loc, Random random) {
                return super.spawnRandomMonster(loc, random).prependOperation(e -> mapper.apply(e, counter));
            }
            
        });
    }

}