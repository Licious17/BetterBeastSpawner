package pt.licious.betterbeastspawner.spawning

import com.pixelmonmod.pixelmon.api.spawning.AbstractSpawner.SpawnerBuilder
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet
import com.pixelmonmod.pixelmon.api.spawning.archetypes.algorithms.selection.FlatDistinctAlgorithm
import com.pixelmonmod.pixelmon.api.spawning.util.SetLoader
import com.pixelmonmod.pixelmon.commands.Reload
import com.pixelmonmod.pixelmon.config.PixelmonConfig
import com.pixelmonmod.pixelmon.enums.EnumSpecies
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import pt.licious.betterbeastspawner.BetterBeastSpawner
import java.io.File
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import pt.licious.betterbeastspawner.spawning.BeastSpawner.BeastSpawnerBuilder
import pt.licious.betterbeastspawner.ticking.ReloadWaiter
import java.time.Duration

object BeastController {

    private val beastDir = File("pixelmon/spawning/ultrabeasts")
    private val ubs = setOf(EnumSpecies.Blacephalon, EnumSpecies.Buzzwole, EnumSpecies.Celesteela, EnumSpecies.Guzzlord, EnumSpecies.Poipole, EnumSpecies.Naganadel, EnumSpecies.Nihilego, EnumSpecies.Pheromosa, EnumSpecies.Kartana, EnumSpecies.Stakataka, EnumSpecies.Xurkitree)
    private val beastSets = arrayListOf<SpawnSet>()

    var ultraBeastSpawnerPreset: BeastSpawnerBuilder<BeastSpawner> = BeastSpawnerBuilder<BeastSpawner>()
        .setSelectionAlgorithm<SpawnerBuilder<BeastSpawner>>(FlatDistinctAlgorithm())
        .setCheckSpawns(BeastCheckSpawns())

    fun onServerStarting() {
        if (PixelmonConfig.useExternalJSONFilesSpawning) {
            MinecraftForge.EVENT_BUS.register(this)
            load()
        }
        else
            BetterBeastSpawner.LOG.error("External Spawn JSON's are not enabled ${BetterBeastSpawner.MOD_NAME} won't do anything...")
    }

    @SubscribeEvent
    fun onCommand(e: CommandEvent) {
        if (!e.isCanceled && e.command is Reload && PixelmonConfig.useExternalJSONFilesSpawning) {
            ReloadWaiter()
        }
    }

    fun load() {
        if (!beastDir.exists()) {
            beastDir.mkdirs()
            beastSets.addAll(standard.filter { set -> ubs.any { it.pokemonName.equals(set.id, true) } }.toSet())
            val standardDir =  File("pixelmon/spawning/default/standard")
            if (standardDir.exists()) {
                standardDir.listFiles { file -> file.name.toLowerCase().endsWith(".set.json") }?.forEach { file ->
                    if (beastSets.any { file.nameWithoutExtension == "${it.id}.set" })
                        file.delete()
                }
            }
        }
        else
            beastSets.addAll(SetLoader.importSetsFrom(beastDir.path))
        standard.removeAll(beastSets)
        beastSets.forEach { it.export(beastDir.path) }
        val beastSpawner = ultraBeastSpawnerPreset
            .setSpawnFrequency<BeastSpawnerBuilder<BeastSpawner>>(1200.0F / PixelmonConfig.legendarySpawnTicks)
            .setSpawnSets<BeastSpawnerBuilder<BeastSpawner>>(beastSets)
            .setupCache<BeastSpawnerBuilder<BeastSpawner>>()
            .apply(BeastSpawner())
        coordinator.spawners.add(beastSpawner)
        BetterBeastSpawner.LOG.info("Loaded the Ultra Beast spawn sets!")
    }

}