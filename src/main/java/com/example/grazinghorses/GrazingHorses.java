package com.example.grazinghorses;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("grazinghorses")
@Mod.EventBusSubscriber(modid = "grazinghorses")
public class GrazingHorses {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof AbstractHorse horse) {
            horse.goalSelector.addGoal(6, new HorseEatGrassGoal(horse));
        }
    }
}
