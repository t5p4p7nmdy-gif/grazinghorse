package com.example.grazinghorses;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HorseEatGrassGoal extends Goal {

    private static final int SEARCH_RADIUS = 10;
    private static final int SEARCH_HEIGHT = 3;
    private static final int SEARCH_ATTEMPTS = 40;
    private static final int START_CHANCE = 1;
    private static final int EAT_TIME = 1;

    private final AbstractHorse horse;
    private final Level level;
    private BlockPos target;
    private int eatTick;
    private int giveUpTick;

    public HorseEatGrassGoal(AbstractHorse horse) {
        this.horse = horse;
        this.level = horse.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    private boolean isEdible(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)) {
            return true;
        }
        return level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && state.isAir();
    }

    private BlockPos findGrass() {
        BlockPos origin = horse.blockPosition();
        for (int i = 0; i < SEARCH_ATTEMPTS; i++) {
            int dx = horse.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int dz = horse.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            for (int dy = SEARCH_HEIGHT; dy >= -SEARCH_HEIGHT; dy--) {
                BlockPos pos = origin.offset(dx, dy, dz);
                if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)
                        && level.getBlockState(pos.above()).isAir()) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle() || horse.isInWater()) {
            return false;
        }
        if (horse.getRandom().nextInt(horse.isBaby() ? 10 : START_CHANCE) != 0) {
            return false;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        target = findGrass();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && giveUpTick < 200
                && level.getBlockState(target).is(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        eatTick = 0;
        giveUpTick = 0;
        horse.getNavigation().moveTo(
                target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, 1.0D);
    }

    @Override
    public void stop() {
        target = null;
        eatTick = 0;
        giveUpTick = 0;
        horse.getNavigation().stop();
        horse.setEating(false);
    }

    @Override
    public void tick() {
        giveUpTick++;
        if (target == null) {
            return;
        }
        double distSq = horse.distanceToSqr(
                target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);

        if (distSq > 4.0D) {
            if (horse.getNavigation().isDone()) {
                horse.getNavigation().moveTo(
                        target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, 1.0D);
            }
            return;
        }

        horse.getNavigation().stop();
        horse.getLookControl().setLookAt(
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        horse.setEating(true);
        eatTick++;

        if (eatTick >= EAT_TIME) {
            level.levelEvent(2001, target, net.minecraft.world.level.block.Block.getId(
                    Blocks.GRASS_BLOCK.defaultBlockState()));
            level.setBlock(target, Blocks.DIRT.defaultBlockState(), 2);
            if (horse.getHealth() < horse.getMaxHealth()) {
                horse.heal(2.0F);
            }
            if (horse.isBaby()) {
                horse.ageUp(60);
            }
            horse.setEating(false);
            target = null;
        }
    }
}
