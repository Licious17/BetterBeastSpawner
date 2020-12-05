package pt.licious.betterbeastspawner.spawning

import com.pixelmonmod.pixelmon.api.spawning.AbstractSpawner.SpawnerBuilder
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet
import com.pixelmonmod.pixelmon.api.spawning.archetypes.algorithms.selection.FlatDistinctAlgorithm
import com.pixelmonmod.pixelmon.commands.Reload
import com.pixelmonmod.pixelmon.config.PixelmonConfig
import com.pixelmonmod.pixelmon.enums.EnumSpecies
import com.pixelmonmod.pixelmon.spawning.LegendarySpawner
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import pt.licious.betterbeastspawner.BetterBeastSpawner
import java.io.File
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning.*
import com.pixelmonmod.pixelmon.spawning.PlayerTrackingSpawner
import pt.licious.betterbeastspawner.ticking.ReloadWaiter

object BeastController {

    private val beastDir = File("pixelmon/spawning/ultrabeasts")
    private val beastSets = arrayListOf<SpawnSet>()

    private val beastSpawnerPreset: LegendarySpawner.LegendarySpawnerBuilder<BeastSpawner> = LegendarySpawner.LegendarySpawnerBuilder<BeastSpawner>()
        .setSelectionAlgorithm<SpawnerBuilder<BeastSpawner>>(FlatDistinctAlgorithm())
        .setCheckSpawns(BeastCheckSpawns())

    fun onServerStarting() {
        if (PixelmonConfig.useExternalJSONFilesSpawning) {
            MinecraftForge.EVENT_BUS.register(this)
            loadSpawner()
        }
        else
            BetterBeastSpawner.LOG.info("External Spawn JSON's are not enabled ${BetterBeastSpawner.MOD_NAME} won't do anything...")
    }

    @SubscribeEvent
    fun onCommand(e: CommandEvent) {
        if (!e.isCanceled && e.command is Reload && PixelmonConfig.useExternalJSONFilesSpawning) {
            ReloadWaiter()
        }
    }

    fun loadSpawner() {
        beastSets.clear()
        beastDir.mkdirs()
        standard.filter { isBeastSet(it) }.forEach {
            beastSets.add(it)
            standard.remove(it)
        }
        trackingSpawnerPreset.setSpawnSets<SpawnerBuilder<PlayerTrackingSpawner>>(standard)
            .addSpawnSets<SpawnerBuilder<PlayerTrackingSpawner>>(npcs)
            .setupCache<SpawnerBuilder<PlayerTrackingSpawner>>()
        val beastSpawner = beastSpawnerPreset
            .setSpawnFrequency<LegendarySpawner.LegendarySpawnerBuilder<BeastSpawner>>(1200.0F / PixelmonConfig.legendarySpawnTicks)
            .setSpawnSets<LegendarySpawner.LegendarySpawnerBuilder<BeastSpawner>>(beastSets)
            .setupCache<LegendarySpawner.LegendarySpawnerBuilder<BeastSpawner>>()
            .apply(BeastSpawner())
        coordinator.spawners.add(beastSpawner)
        BetterBeastSpawner.LOG.info("Loaded the Ultra Beast spawn sets!")
    }

    private fun isBeastSet(spawnSet: SpawnSet) = EnumSpecies.ultrabeasts.any { it.equals(spawnSet.id, true) }

}