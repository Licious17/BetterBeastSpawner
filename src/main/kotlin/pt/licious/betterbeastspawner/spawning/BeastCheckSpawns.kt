package pt.licious.betterbeastspawner.spawning

import com.pixelmonmod.pixelmon.Pixelmon
import com.pixelmonmod.pixelmon.api.spawning.AbstractSpawner
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo
import com.pixelmonmod.pixelmon.api.spawning.SpawnLocation
import com.pixelmonmod.pixelmon.api.spawning.calculators.ICheckSpawns
import com.pixelmonmod.pixelmon.config.PixelmonConfig
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.FMLCommonHandler
import pt.licious.betterbeastspawner.api.BeastSpawnerEvents
import kotlin.math.ceil

class BeastCheckSpawns : ICheckSpawns {

    override fun getPermissionNode() = "pixelmon.checkspawns.ultrabeast"

    override fun checkSpawns(spawner: AbstractSpawner, sender: ICommandSender, args: MutableList<String>) {
        if (spawner is BeastSpawner) {
            val event = BeastSpawnerEvents.CheckSpawns(sender)
            if (Pixelmon.EVENT_BUS.post(event))
                return
            val server = FMLCommonHandler.instance().minecraftServerInstance
            var target: EntityPlayerMP? = null
            val playerList = server.playerList
            val targets = mutableListOf<EntityPlayerMP>()
            if (sender !is EntityPlayerMP) {
                args.forEach { arg ->
                    if (spawner.name != arg) {
                        target = playerList.getPlayerByUsername(arg)
                        if (target != null)
                            return@forEach
                    }
                }
            }
            if (target == null)
                targets.addAll(playerList.players)
            else
                targets.add(target!!)
            val totalPercentages = hashMapOf<String, Double>()
            var percentageSum = 0.0
            targets.forEach { player ->
                val spawnLocations = spawner.spawnLocationCalculator.calculateSpawnableLocations((spawner.getTrackedBlockCollection(player, 0.0F, 0.0F, spawner.horizontalSliceRadius, spawner.verticalSliceRadius, 0, 0)))
                val possibleSpawns = hashMapOf<SpawnLocation, List<SpawnInfo>>()
                spawnLocations.forEach { spawnLocation ->
                    val spawns = spawner.getSuitableSpawns(spawnLocation)
                    if (spawns.isNotEmpty())
                        possibleSpawns[spawnLocation] = spawns
                }
                val percentages = spawner.selectionAlgorithm.getPercentages(spawner, possibleSpawns)
                percentages.forEach { (spawnLocation, percentage) ->
                    percentageSum += percentage
                    totalPercentages[spawnLocation] = (percentage.toDouble() + (totalPercentages[spawnLocation] ?: 0.0))
                }
            }
            if (percentageSum > 100) {
                val multiplier = 100.0 / percentageSum
                totalPercentages.forEach { (spawnLocation, percentage) ->
                    val newPercentage = percentage * multiplier
                    val format = if (newPercentage in .01..99.99) "%.2f" else "%.4f"
                    totalPercentages[spawnLocation] = String.format(format, newPercentage).toDouble()
                }
            }
            server.addScheduledTask {
                val messages = mutableListOf<ITextComponent>()
                if (event.showTime) {
                    val timeLeft = spawner.nextSpawnTime - System.currentTimeMillis()
                    val minutes = ceil(timeLeft / 1000F / 60F)
                    messages.add(translate(TextFormatting.GOLD, "spawning.checkspawns.timeuntilnextattempt1")
                        .appendSibling(TextComponentString(" "))
                        .appendSibling(translate(TextFormatting.DARK_AQUA, "spawning.checkspawns.timeuntilnextattempt2", minutes)))
                }
                if (event.showChance) {
                    messages.add(translate(TextFormatting.GOLD, "spawning.checkspawns.chanceofspawning")
                        .appendSibling(TextComponentString(" ${TextFormatting.DARK_AQUA}${String.format("%.2f", PixelmonConfig.legendarySpawnChance * 100)}%")));
                }
                messages.add(translate(TextFormatting.AQUA, "spawning.checkspawns.possiblespawns"))
                messages.addAll(generateMessages(totalPercentages, args))
                messages.forEach { sender.sendMessage(it) }
            }
        }
    }

}