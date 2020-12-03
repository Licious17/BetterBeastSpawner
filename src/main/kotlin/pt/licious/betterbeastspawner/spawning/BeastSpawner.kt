package pt.licious.betterbeastspawner.spawning

import com.pixelmonmod.pixelmon.Pixelmon
import com.pixelmonmod.pixelmon.api.spawning.SpawnAction
import com.pixelmonmod.pixelmon.config.PixelmonConfig
import com.pixelmonmod.pixelmon.spawning.LegendarySpawner
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import pt.licious.betterbeastspawner.api.BeastSpawnerEvents


class BeastSpawner : LegendarySpawner("ultrabeast") {

    // ToDo if you ever feel like a split config would go here
    /*
    override fun getSpawns(pass: Int): MutableList<SpawnAction<out Entity>>? {
        if (pass == 0) {
            possibleSpawns = null
            val playerCount = FMLCommonHandler.instance().minecraftServerInstance.playerList.currentPlayerCount
            val baseTicks = PixelmonConfig.legendarySpawnTicks
            val spawnTicks = (baseTicks / (1 + (playerCount - 1)) * PixelmonConfig.spawnTicksPlayerMultiplier)
            spawnFrequency = RandomHelper.getRandomNumberBetween(.6F, 1.4F) * 1200.0F / spawnTicks
            if (RandomHelper.getRandomChance(PixelmonConfig.legendarySpawnChance) && playerCount > 0)
                forcefullySpawn(null)
            return null
        }
        if (possibleSpawns != null && possibleSpawns.isNotEmpty())
            return possibleSpawns
        return null
    }
     */

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
                clusters.forEach { cluster ->
                    val player = cluster.random()
                    cluster.remove(player)
                    if (cluster.isEmpty())
                        clusters.remove(cluster)
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

    class BeastSpawnerBuilder<T : BeastSpawner?> : TickingSpawnerBuilder<T>() {
        private var minDistFromCentre: Int? = null
        private var maxDistFromCentre: Int? = null
        private var horizontalSliceRadius: Int? = null
        private var verticalSliceRadius: Int? = null
        private var firesChooseEvent = true

        override fun apply(spawner: T): T {
            super.apply(spawner)
            spawner!!.minDistFromCentre =
                if (minDistFromCentre != null) minDistFromCentre!! else PixelmonConfig.minimumDistanceFromCentre
            spawner.maxDistFromCentre =
                if (maxDistFromCentre != null) maxDistFromCentre!! else PixelmonConfig.maximumDistanceFromCentre
            spawner.verticalSliceRadius =
                if (verticalSliceRadius != null) verticalSliceRadius!! else PixelmonConfig.verticalSliceRadius
            spawner.horizontalSliceRadius =
                if (horizontalSliceRadius != null) horizontalSliceRadius!! else PixelmonConfig.horizontalSliceRadius
            spawner.onSpawnEnded()
            return spawner
        }
    }

}