package diamondtaco.tcl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.commands.BooleanParser
import diamondtaco.tcl.commands.FlagParser
import diamondtaco.tcl.commands.MarshalSerializer
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
            FlagParser::class.java,
            MarshalSerializer()
        )

        ArgumentTypeRegistry.registerArgumentType(
            Identifier("tcl", "bool_type"),
            BooleanParser::class.java,
            MarshalSerializer()
        )



        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback
            { dispatcher, registryAccess, environment ->
                getCommandNode(dispatcher)
            })
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }

    private fun getCommandNode(dispatcher: CommandDispatcher<ServerCommandSource>): CommandNode<ServerCommandSource> {
        val root = literal("foo").build()
        val flagsPartA = argument("asdf", FlagParser("abcde".toSet()))
            .executes { context ->
                val name = runCatching {
                    context.getArgument("asdf", StackFlags::class.java).toString()
                }.getOrElse { it.toString() }

                context.source.sendMessage(Text.literal(name))
                1
            }.build()

        val booltest = argument("booltest", BooleanParser())
            .executes { context ->
                val boolis = runCatching {
                    context.getArgument("booltest", Boolean::class.java).toString()
                }.getOrElse { it.toString() }

                context.source.sendMessage(Text.literal(boolis))

                1
            }.build()

        root.addChild(flagsPartA)
        flagsPartA.addChild(booltest)


        val other = literal("balls").redirect(root).build()

        dispatcher.root.addChild(root)
        dispatcher.root.addChild(other)

        return root
    }


}