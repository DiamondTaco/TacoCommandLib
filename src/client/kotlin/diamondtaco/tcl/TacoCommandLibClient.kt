package diamondtaco.tcl

import diamondtaco.tcl.lib.MarshalSerializer
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.commands.*
import diamondtaco.tcl.defualt.BooleanParser
import diamondtaco.tcl.defualt.ItemParser
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

        ArgumentTypeRegistry.registerArgumentType(
            Identifier("tcl", "block_type"),
            ItemParser::class.java,
            MarshalSerializer()
        )



        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback
            { dispatcher, registryAccess, environment ->
                getCommandNode(dispatcher)
            })
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }

    fun getCommandNode(dispatcher: CommandDispatcher<ServerCommandSource>): CommandNode<ServerCommandSource> {
        val argumentSet = ArgumentSet(
            "abc".map { FlagName("long-$it", it) }.toSet() + setOf(FlagName("long-g")),
            "xyz".map { FlagName("long-$it", it) }.toSet() + setOf(FlagName("long-w")),
        )

        val root = literal("foo").build()

        val flagGenerator =
            generateSequence<Pair<CommandNode<ServerCommandSource>, Int>>(Pair(root, 0)) { (_, idx) ->
                val newNode = argument("flag$idx", FlagParser(argumentSet)).executes { context ->
                    val name = runCatching {
                        buildString {
                            for (i in 0..idx) {
                                append(context.getArgument("flag$i", ParsedFlag::class.java))
                                append(", ")
                            }
                        }
                    }.getOrElse { it.toString() }

                    context.source.sendMessage(Text.literal(name))
                    1
                }.build()

                newNode to idx + 1
            }.map { it.first }

        flagGenerator.take(6)
            .zipWithNext()
            .onEach(::println)
            .forEach { (a, b) -> a.addChild(b) }
//            .fold<_, CommandNode<ServerCommandSource>>(root) { a, b -> b.first.also { a.addChild(it) } }

//
//        val booltest = argument("booltest", BooleanParser())
//            .executes { context ->
//                val boolis = runCatching {
//                    context.getArgument("booltest", Boolean::class.java).toString()
//                }.getOrElse { it.toString() }
//
//                context.source.sendMessage(Text.literal(boolis))
//
//                1
//            }.build()

//        val blockTest = argument("blab", ItemParser())
//            .executes { context ->
//                val outBlock = runCatching {
//                    context.getArgument("blab", String::class.java)
//                }.getOrElse { it.toString() }
//
//                context.source.sendMessage(Text.literal("Item picked: $outBlock"))
//
//                1
//            }.build()
//
//        val blockTest2 = argument("blab2", ItemParser())
//            .executes { context ->
//                val outBlock = runCatching {
//                    context.getArgument("blab", String::class.java) + " + " + context.getArgument("blab2", String::class.java)
//                }.getOrElse { it.toString() }
//
//                context.source.sendMessage(Text.literal("Item picked: $outBlock"))
//
//                1
//            }.build()

//        blockTest.addChild(blockTest2)
//        root.addChild(blockTest)
//        flagsPartA.addChild(booltest)


        val other = literal("balls").redirect(root).build()

        dispatcher.root.addChild(root)
        dispatcher.root.addChild(other)

        return root
    }


}