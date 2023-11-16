package diamondtaco.tcl

import com.mojang.brigadier.CommandDispatcher
import diamondtaco.tcl.commands.Command
import diamondtaco.tcl.commands.CommandBuilder.Companion.command
import diamondtaco.tcl.commands.FlagParser
import diamondtaco.tcl.defualt.BooleanParser
import diamondtaco.tcl.defualt.ItemParser
import diamondtaco.tcl.lib.MarshalSerializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource
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

        ArgumentTypeRegistry.registerArgumentType(
            Identifier("tcl", "block_type"),
            ItemParser::class.java,
            MarshalSerializer()
        )

        Command


        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback
            { dispatcher, registryAccess, environment ->
                getCommandNode(dispatcher)
            })
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }

    fun getCommandNode(dispatcher: CommandDispatcher<ServerCommandSource>) {
//        val argumentSet = ArgumentSet(
//            "abc".map { FlagName("long-$it", it) }.toSet() + setOf(FlagName("long-g")),
//            "xyz".map { FlagName("long-$it", it) }.toSet() + setOf(FlagName("long-w")),
//        )
//
//        val root = literal("foo").build()
//
//        val flagGenerator =
//            generateSequence<Pair<CommandNode<ServerCommandSource>, Int>>(Pair(root, 0)) { (_, idx) ->
//                val newNode = argument("flag$idx", FlagParser(argumentSet)).executes { context ->
//                    val name = runCatching {
//                        buildString {
//                            for (i in 0..idx) {
//                                append(context.getArgument("flag$i", ParsedFlag::class.java))
//                                append(", ")
//                            }
//                        }
//                    }.getOrElse { it.toString() }
//
//                    context.source.sendMessage(Text.literal(name))
//                    1
//                }.build()
//
//                newNode to idx + 1
//            }.map { it.first }

        val command = command("foo") {
            for (c in "abc") addToggle("long-$c", c)
            addToggle("long-g")

            for (c in "xyz") addArgument("long-$c", c, BooleanParser())
            addArgument("long-w", BooleanParser())

            executes {
                Result.success(
                    buildString {
                        append("Toggles: ")
                        append(getToggle('a'))
                        append(',')
                        append(getToggle('b'))
                        append(',')
                        append(getToggle('c'))
                        append(',')
                        append(getToggle("long-g"))

                        append("; Args: ")
                        append(getArgument('x').getOrElse { return@executes Result.failure(it) })
                        append(',')
                        append(getArgument('y').getOrElse { "default y" })
                        append(',')
                        append(getArgument('z').getOrElse { "default z" })
                        append(',')
                        append(getArgument("long-w").getOrElse { return@executes Result.failure(it) })
                    }
                )
            }
        }.toBrigadierNode()

        dispatcher.root.addChild(command)
    }


}