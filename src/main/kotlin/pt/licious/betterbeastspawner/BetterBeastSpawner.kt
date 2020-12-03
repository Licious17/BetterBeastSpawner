package pt.licious.betterbeastspawner

import com.pixelmonmod.pixelmon.Pixelmon
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import pt.licious.betterbeastspawner.spawning.BeastController

@Mod(
    modid = BetterBeastSpawner.MOD_ID,
    name = BetterBeastSpawner.MOD_NAME,
    version = BetterBeastSpawner.VERSION,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    acceptableRemoteVersions = "*",
    dependencies = "after:${Pixelmon.MODID}",
    acceptedMinecraftVersions = "[1.12.2]"
)
object BetterBeastSpawner {

    const val MOD_ID = "betterbeastspawner"
    const val MOD_NAME = "BetterBeastSpawner"
    const val VERSION = "1.0"
    val LOG: Logger = LogManager.getLogger(MOD_NAME)

    @Mod.EventHandler
    fun onServerStarting(e: FMLServerStartingEvent) {
        LOG.info("Booting $MOD_NAME by Licious @2020 $VERSION")
        BeastController.onServerStarting()
    }

}