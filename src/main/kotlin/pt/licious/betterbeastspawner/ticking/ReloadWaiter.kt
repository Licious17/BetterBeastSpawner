package pt.licious.betterbeastspawner.ticking

import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import pt.licious.betterbeastspawner.spawning.BeastController

class ReloadWaiter {

    private var ticks = 0

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ServerTickEvent) {
        if (e.phase == TickEvent.Phase.START && ticks++ % 20 == 0 && PixelmonSpawning.coordinator.active) {
            MinecraftForge.EVENT_BUS.unregister(this)
            BeastController.loadSpawner()
        }
    }

}