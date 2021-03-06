package me.uniodex.unioprotections.managers;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import lombok.Getter;
import me.uniodex.unioprotections.UnioProtections;
import me.uniodex.unioprotections.objects.EntityData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class LootProtectionManager {

    private UnioProtections plugin;

    @Getter
    private Long protectionTimeout;
    @Getter
    private Map<UUID, EntityData> entityDatas = new HashMap<>();

    public LootProtectionManager(UnioProtections plugin) {
        this.plugin = plugin;
        protectionTimeout = plugin.getConfig().getLong("settings.lootProtectionTimeout", 10) * 1000;

        // Cleanup Task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (EntityData entityData : this.entityDatas.values()) {
                if (entityData.isInactive()) {
                    this.entityDatas.remove(entityData);
                }
            }
        }, 20L, 20L);
    }

    public boolean isItemProtected(Item item) {
        return item.hasMetadata("LootProtection");
    }

    public String getItemOwner(Item item) {
        String metaData = item.getMetadata("LootProtection").get(0).asString();
        String owner = metaData.split(" ")[0];
        Long time = Long.valueOf(metaData.split(" ")[1]);

        if (System.currentTimeMillis() - time < protectionTimeout) {
            return owner;
        }

        return null;
    }

    public void protectLoot(List<ItemStack> drops, String player, Location location) {
        for (ItemStack item : drops) {
            Entity entity = location.getWorld().dropItemNaturally(location, item);
            entity.setMetadata("LootProtection", new FixedMetadataValue(this.plugin, player + " " + System.currentTimeMillis()));
        }
    }

    public boolean shouldSendMessage(Entity entity) {
        if (entity instanceof Player) {
            return true;
        }

        if (plugin.getMythicMobs() != null) {
            Optional<ActiveMob> activeMob = plugin.getMythicMobs().getMobManager().getActiveMob(entity.getUniqueId());
            if (activeMob.isPresent()) {
                if (activeMob.get().getType().getInternalName().contains("Minyon") || activeMob.get().getType().getInternalName().contains("MiniBoss")) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearData(Entity entity) {
        entityDatas.remove(entity.getUniqueId());
    }
}
