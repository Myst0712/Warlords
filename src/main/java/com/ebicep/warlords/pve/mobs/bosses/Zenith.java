package com.ebicep.warlords.pve.mobs.bosses;

import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.circle.AreaEffect;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.instances.InstanceFlags;
import com.ebicep.warlords.pve.DifficultyIndex;
import com.ebicep.warlords.pve.mobs.Mob;
import com.ebicep.warlords.pve.mobs.MobDrop;
import com.ebicep.warlords.pve.mobs.abilities.AbstractPveAbility;
import com.ebicep.warlords.pve.mobs.abilities.SpawnMobAbility;
import com.ebicep.warlords.pve.mobs.tiers.BossMob;
import com.ebicep.warlords.pve.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Zenith extends AbstractZombie implements BossMob {

    private List<ThunderCloud> thunderClouds = new ArrayList<>();

    public Zenith(Location spawnLocation) {
        super(spawnLocation,
                "Zenith",
                26000,
                0.36f,
                25,
                1800,
                2500,
                new Armageddon(),
                new Cleanse(),
                new SpawnMobAbility(30, Mob.ZENITH_LEGIONNAIRE) {
                    @Override
                    public int getSpawnAmount() {
                        return (int) pveOption.getGame().warlordsPlayers().count();
                    }
                }
        );
    }

    public Zenith(
            Location spawnLocation,
            String name,
            int maxHealth,
            float walkSpeed,
            int damageResistance,
            float minMeleeDamage,
            float maxMeleeDamage
    ) {
        super(spawnLocation,
                name,
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage,
                new Armageddon(),
                new Cleanse(),
                new SpawnMobAbility(30, Mob.ZENITH_LEGIONNAIRE) {
                    @Override
                    public int getSpawnAmount() {
                        return (int) pveOption.getGame().warlordsPlayers().count();
                    }
                }
        );
    }

    @Override
    public HashMap<MobDrop, HashMap<DifficultyIndex, Double>> mobDrops() {
        return new HashMap<>() {{
            put(MobDrop.ZENITH_STAR, new HashMap<>() {{
                put(DifficultyIndex.EASY, .015);
                put(DifficultyIndex.NORMAL, .025);
                put(DifficultyIndex.HARD, .05);
                put(DifficultyIndex.EXTREME, .10);
                put(DifficultyIndex.ENDLESS, .05);
            }});
        }};
    }

    @Override
    public Mob getMobRegistry() {
        return Mob.ZENITH;
    }

    @Override
    public Component getDescription() {
        return Component.text("Leader of the Illusion Vanguard", NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public NamedTextColor getColor() {
        return NamedTextColor.DARK_PURPLE;
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);
        EffectUtils.strikeLightning(warlordsNPC.getLocation(), false, 6);
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {
        if (ticksElapsed % 40 == 0) {
            Location randomSpawnLocation = option.getRandomSpawnLocation(null);
            if (randomSpawnLocation == null) {
                return;
            }
            ThreadLocalRandom random = ThreadLocalRandom.current();
            randomSpawnLocation.add(random.nextDouble(10) - 5, 0, random.nextDouble(10) - 5);
            thunderClouds.add(new ThunderCloud(option.getGame(), randomSpawnLocation));
        }
        thunderClouds.removeIf(thunderCloud -> thunderCloud.tick(warlordsNPC));
    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {
        EffectUtils.strikeLightning(warlordsNPC.getLocation(), true);

        if (event.getAbility().equals("Uppercut") ||
                event.getAbility().equals("Armageddon") ||
                event.getAbility().equals("Intervene") ||
                event.getAbility().equals("Thunder Strike")
        ) {
            return;
        }
        new GameRunnable(attacker.getGame()) {
            int counter = 0;

            @Override
            public void run() {
                if (warlordsNPC.isDead()) {
                    this.cancel();
                }

                counter++;
                EffectUtils.playFirework(
                        receiver.getLocation(),
                        FireworkEffect.builder()
                                      .withColor(Color.WHITE)
                                      .with(FireworkEffect.Type.BURST)
                                      .build()
                );
                Utils.addKnockback(name, attacker.getLocation(), receiver, -1, 0.3);
                receiver.addDamageInstance(attacker, "Uppercut", 250, 350, 0, 100);

                if (counter == 3 || receiver.isDead()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(8, 2);
    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {
        Utils.playGlobalSound(self.getLocation(), Sound.ENTITY_BLAZE_HURT, 2, 0.2f);
    }

    @Override
    public void onDeath(WarlordsEntity killer, Location deathLocation, PveOption option) {
        super.onDeath(killer, deathLocation, option);
        for (int i = 0; i < 3; i++) {
            EffectUtils.playFirework(deathLocation, FireworkEffect.builder()
                                                                  .withColor(Color.WHITE)
                                                                  .with(FireworkEffect.Type.BALL_LARGE)
                                                                  .build());
        }

        EffectUtils.strikeLightning(deathLocation, false, 5);
    }

    private static class Armageddon extends AbstractPveAbility {

        private final int stormRadius = 10;


        public Armageddon() {
            super(
                    "Armageddon",
                    550,
                    700,
                    12,
                    100
            );
        }

        @Override
        public boolean onPveActivate(@Nonnull WarlordsEntity wp, PveOption pveOption) {
            wp.subtractEnergy(energyCost, false);

            long playerCount = pveOption.getGame().warlordsPlayers().count();
            Location loc = wp.getLocation();
            DifficultyIndex difficulty = pveOption.getDifficulty();
            float multiplier = switch (difficulty) {
                case EASY -> 0.5f;
                case HARD -> 1;
                case EXTREME -> 1.25f;
                default -> 0.75f;
            };

            Utils.playGlobalSound(loc, "rogue.healingremedy.impact", 500, 0.85f);
            Utils.playGlobalSound(loc, "rogue.healingremedy.impact", 500, 0.85f);
            wp.addSpeedModifier(wp, "Armageddon Slowness", -99, 90);
            Game game = wp.getGame();
            new GameRunnable(game) {
                @Override
                public void run() {
                    if (wp.isDead()) {
                        this.cancel();
                        return;
                    }

                    EffectUtils.strikeLightningInCylinder(loc, stormRadius, false, 12, game);
                    shockwave(loc, stormRadius, 12, playerCount, multiplier, wp);
                    EffectUtils.strikeLightningInCylinder(loc, stormRadius + 5, false, 24, game);
                    shockwave(loc, stormRadius + 5, 24, playerCount, multiplier, wp);
                    EffectUtils.strikeLightningInCylinder(loc, stormRadius + 10, false, 36, game);
                    shockwave(loc, stormRadius + 10, 36, playerCount, multiplier, wp);
                    if (difficulty == DifficultyIndex.HARD || difficulty == DifficultyIndex.EXTREME || difficulty == DifficultyIndex.ENDLESS) {
                        EffectUtils.strikeLightningInCylinder(loc, stormRadius + 15, false, 48, game);
                        shockwave(loc, stormRadius + 15, 48, playerCount, multiplier, wp);
                        EffectUtils.strikeLightningInCylinder(loc, stormRadius + 15, false, 60, game);
                        shockwave(loc, stormRadius + 15, 60, playerCount, multiplier, wp);
                    }
                }
            }.runTaskLater(40);
            return true;
        }

        private void shockwave(Location loc, double radius, int tickDelay, long playerCount, float damageMultiplier, WarlordsEntity wp) {
            new GameRunnable(wp.getGame()) {
                @Override
                public void run() {
                    if (wp.isDead()) {
                        this.cancel();
                    }

                    Utils.playGlobalSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 10, 0.4f);
                    Utils.playGlobalSound(loc, "warrior.laststand.activation", 10, 0.4f);
                    for (WarlordsEntity we : PlayerFilter
                            .entitiesAround(loc, radius, radius, radius)
                            .aliveEnemiesOf(wp)
                    ) {
                        if (we.getCooldownManager().hasCooldownFromName("Cloaked")) {
                            continue;
                        }
                        we.addDamageInstance(wp,
                                "Armageddon",
                                (minDamageHeal * playerCount) * damageMultiplier,
                                (maxDamageHeal * playerCount) * damageMultiplier,
                                critChance,
                                critMultiplier
                        );
                        Utils.addKnockback(name, wp.getLocation(), we, -2, 0.2);
                    }
                }
            }.runTaskLater(tickDelay);
        }

    }

    private static class Cleanse extends AbstractPveAbility {

        public Cleanse() {
            super(
                    "Cleanse",
                    300,
                    400,
                    4,
                    100
            );
        }

        @Override
        public boolean onPveActivate(@Nonnull WarlordsEntity wp, PveOption pveOption) {
            wp.subtractEnergy(energyCost, false);

            long playerCount = pveOption.getGame().warlordsPlayers().count();
            Location loc = wp.getLocation();
            DifficultyIndex difficulty = pveOption.getDifficulty();
            float multiplier = switch (difficulty) {
                case EASY -> 0.5f;
                case HARD -> 1;
                case EXTREME -> 1.25f;
                default -> 0.75f;
            };

            EffectUtils.playSphereAnimation(loc, 4, Particle.SPELL_WITCH, 2);
            for (WarlordsEntity we : PlayerFilter
                    .entitiesAround(loc, 4, 4, 4)
                    .aliveEnemiesOf(wp)
            ) {
                Utils.addKnockback(name, wp.getLocation(), we, -1.5, 0.3);
                we.addDamageInstance(
                        wp,
                        name,
                        (minDamageHeal * playerCount) * multiplier,
                        (maxDamageHeal * playerCount) * multiplier,
                        critChance,
                        critMultiplier
                );
                EffectUtils.strikeLightning(we.getLocation(), false);
            }
            return true;
        }
    }

    private static class ThunderCloud {

        private static final int[] CLOUD_COLORS = {150, 200, 250};
        private final Location floorLocation;
        private final int ticksToLive = ThreadLocalRandom.current().nextInt(7, 12) * 20;
        private final int size = ThreadLocalRandom.current().nextInt(5, 10);
        private final List<CircleEffect> effects = new ArrayList<>();
        private int ticksElapsed = ThreadLocalRandom.current().nextInt(15);
        private int startDamageTick = 30;

        private ThunderCloud(Game game, Location floorLocation) {
            this.floorLocation = floorLocation.clone();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int startingYOffset = random.nextInt(5, 8);
            for (int i = 0; i < 3; i++) {
                double yOffset = startingYOffset + i * .35;
                int cloudColor = CLOUD_COLORS[i];
                AreaEffect areaEffect = new AreaEffect(
                        yOffset,
                        Particle.REDSTONE,
                        new Particle.DustOptions(Color.fromRGB(cloudColor, cloudColor, cloudColor), 1)
                ).particlesPerSurface(1.15 - (i * 0.05));
                effects.add(new CircleEffect(
                        game,
                        null,
                        floorLocation,
                        size - i,
                        areaEffect
                ));
                if (i == 0) {
                    floorLocation.add(random.nextDouble(1) - .5, 0, random.nextDouble(1) - .5);
                }
            }
        }

        public boolean tick(WarlordsEntity warlordsEntity) {
            ticksElapsed++;
            startDamageTick--;
            if (ticksElapsed % 5 == 0) {
                effects.forEach(CircleEffect::playEffects);
            }
            if (ticksElapsed % 20 == 0 && startDamageTick <= 0) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                EffectUtils.strikeLightning(
                        floorLocation.clone().add(random.nextDouble(4) - 2, -1, random.nextDouble(4) - 2),
                        true
                );
                PlayerFilter.entitiesAround(floorLocation, size, size, size)
                            .excluding(warlordsEntity)
                            .forEach(entity -> {
                                float minDamage;
                                float maxDamage;
                                if (entity.isTeammate(warlordsEntity)) {
                                    minDamage = 150;
                                    maxDamage = 300;
                                } else {
                                    minDamage = 800;
                                    maxDamage = 1000;
                                }
                                entity.addDamageInstance(
                                        warlordsEntity,
                                        "Thunder Strike",
                                        minDamage,
                                        maxDamage,
                                        0,
                                        100,
                                        size > 8 ? EnumSet.of(InstanceFlags.PIERCE_DAMAGE) : EnumSet.noneOf(InstanceFlags.class)
                                );
                            });
            }
            return ticksElapsed >= ticksToLive;
        }
    }
}
