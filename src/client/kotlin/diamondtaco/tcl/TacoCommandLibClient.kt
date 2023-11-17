package diamondtaco.tcl

import com.mojang.brigadier.CommandDispatcher
import diamondtaco.tcl.commands.Command
import diamondtaco.tcl.commands.CommandBuilder.Companion.command
import diamondtaco.tcl.defualt.ItemParser
import diamondtaco.tcl.lib.toParser
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.command.ServerCommandSource


object TacoCommandLibClient : ClientModInitializer {

    override fun onInitializeClient() {
        Command


        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback
            { dispatcher, registryAccess, environment ->
                getCommandNode(dispatcher)
            })
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }

    private fun getCommandNode(dispatcher: CommandDispatcher<ServerCommandSource>) {

        val give2 = command("give2") {
            addToggle("give-stack", 's')
            addToggle("force-stack", 'f')
            addArgument("player", 'p', EntityArgumentType.players().toParser())
            addArgument("item", 'i', ItemParser())

            executes { source ->
                val item = getArgument("item").getOrElse { return@executes Result.failure(it) } as Item
                
                val itemAmount = when {
                    getToggle('f') -> 64
                    getToggle('s') -> item.maxCount
                    else -> 1
                }

                val itemStack = ItemStack(item, itemAmount)

                val targetPlayers = getArgument("player").getOrElse { return@executes Result.failure(it) } as EntitySelector

                targetPlayers.getPlayers(source).forEach { it.inventory.setStack(it.inventory.emptySlot, itemStack) }

                Result.success("Gave ${targetPlayers.getPlayers(source).map { it.name.string }} $itemStack")
            }
        }


        dispatcher.root.addChild(give2.toBrigadierNode())
    }


}