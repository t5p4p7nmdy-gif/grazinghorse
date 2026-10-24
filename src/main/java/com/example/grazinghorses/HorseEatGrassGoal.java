package com.example.grazinghorses;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class HorseEatGrassGoal extends EatBlockGoal {

    private final AbstractHorse horse;

    public HorseEatGrassGoal(AbstractHorse horse) {
        super(horse);
        this.horse = horse;
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public void stop() {
        super.stop();
        if (horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(2.0F);
        }
    }
}
