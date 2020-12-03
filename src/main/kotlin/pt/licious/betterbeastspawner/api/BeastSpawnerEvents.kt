package pt.licious.betterbeastspawner.api

import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import pt.licious.betterbeastspawner.spawning.BeastSpawner


open class BeastSpawnerEvents : Event() {

    @Cancelable
    class ChoosePlayer(val spawner: BeastSpawner, val target: EntityPlayerMP, val clusters: ArrayList<ArrayList<EntityPlayerMP>>) : BeastSpawnerEvents()

    @Cancelable
    class CheckSpawns(val requesting: ICommandSender, var showTime: Boolean = true, var showChance: Boolean = true) : BeastSpawnerEvents()

}