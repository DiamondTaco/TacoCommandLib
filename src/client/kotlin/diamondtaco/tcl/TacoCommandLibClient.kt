package diamondtaco.tcl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.commands.FlagArgumentType
import diamondtaco.tcl.commands.FlagSerializer
import diamondtaco.tcl.commands.StackFlags
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier


object TacoCommandLibClient : ClientModInitializer {

    override fun onInitializeClient() {
        ArgumentTypeRegistry.registerArgumentType(
            Identifier("tcl", "flag_type"),
            FlagArgumentType::class.java,
            FlagSerializer()
        )
        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback { dispatcher, registryAccess, environment ->
                getCommandNode(dispatcher)
            })
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }

    private fun getCommandNode(dispatcher: CommandDispatcher<ServerCommandSource>): CommandNode<ServerCommandSource> {
        val root = literal("foo").build()
        val flagsPartA = argument("asdf", FlagArgumentType("abcde".toSet()))
            .executes { context ->
                val name = runCatching {
                    context.getArgument("asdf", StackFlags::class.java).toString()
                }.getOrElse { it.toString() }

                context.source.sendMessage(Text.literal(name))
                1
            }.build()

        root.addChild(flagsPartA)



        val other = literal("balls").redirect(root).build()

        dispatcher.root.addChild(root)
        dispatcher.root.addChild(other)

        return root
    }


}