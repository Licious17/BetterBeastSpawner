package pt.licious.betterbeastspawner.spawning

import com.pixelmonmod.pixelmon.Pixelmon
import com.pixelmonmod.pixelmon.api.spawning.SpawnAction
import com.pixelmonmod.pixelmon.spawning.LegendarySpawner
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import pt.licious.betterbeastspawner.api.BeastSpawnerEvents

class BeastSpawner : LegendarySpawner("ultrabeast") {

    override fun forcefullySpawn(onlyFocus: EntityPlayerMP?) {
        val clusters = arrayListOf<ArrayList<EntityPlayerMP>>()
        val players = ArrayList(FMLCommonHandler.instance().minecraftServerInstance.playerList.players)
        if (onlyFocus == null) {
            while (players.isNotEmpty()) {
                val cluster = arrayListOf<EntityPlayerMP>()
                val focus = players.removeAt(0)
                cluster.add(focus)
                fillNearby(players, cluster, focus)
                clusters.add(cluster)
            }
        }
        isBusy = true
        PixelmonSpawning.coordinator.processor.addProcess {
            if (onlyFocus != null)
                possibleSpawns = doBeastSpawn(onlyFocus)
            else {
                val clustersIterator = clusters.iterator()
                for (cluster in clustersIterator) {
                    val player = cluster.random()
                    cluster.remove(player)
                    if (cluster.isEmpty())
                        clustersIterator.remove()
                    val event = BeastSpawnerEvents.ChoosePlayer(this, player, clusters)
                    if (!Pixelmon.EVENT_BUS.post(event)) {
                        possibleSpawns = doBeastSpawn(player)
                        if (possibleSpawns != null) {
                            isBusy = false
                            return@addProcess
                        }
                    }
                }
            }
            isBusy = false
        }
    }

    private fun doBeastSpawn(target: EntityPlayerMP?): List<SpawnAction<*>>? {
        val collection = getTrackedBlockCollection(target, 0.0f, 0.0f, horizontalSliceRadius, verticalSliceRadius, minDistFromCentre, maxDistFromCentre)
        val spawnLocations = spawnLocationCalculator.calculateSpawnableLocations(collection)
        val possibleSpawns = selectionAlgorithm.calculateSpawnActions(this, spawnSets, spawnLocations)
        if (possibleSpawns == null || possibleSpawns.isEmpty())
            return null
        possibleSpawns.forEach { it.applyLocationMutations() }
        return possibleSpawns
    }

}