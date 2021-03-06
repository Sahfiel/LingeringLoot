package lingerloot

import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.config.Configuration
import java.io.File

class LingeringLootConfig(file: File) {
    val despawns: DespawnTimes
    val shitTier: Set<Item>
    val shitTierMods: Set<String>

    init {
        val config = Configuration(file)

        val timeCategory = "despawn times"
        val shitTierCategory = "shit tier"
        config.setCategoryComment(timeCategory,
                "Despawn times are in seconds.  Minecraft's default is 300.  Use -1 to defer to less granular settings\n" +
                        "eg: player drops and player-killed mob drops are both types of mob drops, and player-caused drops.\n" +
                        "The order of precedence is: player drops, player-killed mob drops or player-mined items or player-thrown\n" +
                        "items, player-caused drops, mob drops, and finally other.")

        fun configOptionSecs(category: String, name: String, default: Int): Int {
            val r = (20 * config.get(category, name, default.toDouble()).getDouble(default.toDouble())).toInt()
            return if (r == MINECRAFT_LIFESPAN) FAKE_DEFAULT_LIFESPAN else r  // important to differentiate 6000 from -1
        }

        despawns = DespawnTimes(
                configOptionSecs(timeCategory, "player drops", 3600),
                configOptionSecs(timeCategory, "player-killed mob drops", -1),
                configOptionSecs(timeCategory, "player-mined items", -1),
                configOptionSecs(timeCategory, "mob drops", -1),
                configOptionSecs(timeCategory, "player-thrown items", -1),
                configOptionSecs(timeCategory, "player-caused drops", 1800),
                configOptionSecs(timeCategory, "other", 900),
                configOptionSecs(shitTierCategory, "shit despawn time", 300)
        )

        config.setCategoryComment(shitTierCategory, "The despawn time for shit-tier items, if set, overrides all other settings.")
        shitTier = config.get(shitTierCategory, "shit tier items", "cobblestone,snowball").string.split(",").
                map{b -> Item.REGISTRY.getObject(ResourceLocation(b))}.filterNotNull().
                toSet()
        shitTierMods = config.get(shitTierCategory, "shit tier mods", "").string.split(",").
                toSet()

        if (config.hasChanged()) config.save()
    }
}
