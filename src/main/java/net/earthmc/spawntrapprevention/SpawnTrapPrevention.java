package net.earthmc.spawntrapprevention;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SpawnTrapPrevention extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDamagePlayer(TownyPlayerDamagePlayerEvent event) {
        TownBlock townBlock = event.getTownBlock();

        if (townBlock != null && isCloseToTownySpawn(event.getVictimResident(), townBlock.getWorldCoord()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (TownyAPI.getInstance().isWilderness(event.getEntity().getLocation()))
            return;

        if (isCloseToTownySpawn(TownyAPI.getInstance().getResident(event.getEntity()), WorldCoord.parseWorldCoord(event.getEntity().getLocation()))) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    private boolean isCloseToTownySpawn(@Nullable Resident resident, @NotNull WorldCoord worldCoord) {
        final @Nullable Town residentTown = resident != null ? resident.getTownOrNull() : null;

        for (WorldCoord coord : selectArea(worldCoord)) {
            final Town town = coord.getTownOrNull();
            if (town == null)
                continue;

            if (town.equals(residentTown)) {
                Location spawnLocation = town.getSpawnOrNull();
                if (spawnLocation != null && WorldCoord.parseWorldCoord(spawnLocation).equals(coord))
                    return true;
            }

            final Nation nation = town.getNationOrNull();
            if (nation != null && nation.isCapital(town)) {
                Location spawnLocation = nation.getSpawnOrNull();
                if (spawnLocation != null && WorldCoord.parseWorldCoord(spawnLocation).equals(coord))
                    return true;
            }
        }

        return false;
    }

    private Set<WorldCoord> selectArea(WorldCoord centre) {
        Set<WorldCoord> coords = new HashSet<>();

        coords.add(centre.add(-1, -1));
        coords.add(centre.add(-1, 0));
        coords.add(centre.add(-1, 1));
        coords.add(centre.add(0, -1));
        coords.add(centre);
        coords.add(centre.add(0, 1));
        coords.add(centre.add(1, -1));
        coords.add(centre.add(1, 0));
        coords.add(centre.add(1, 1));

        return coords;
    }
}
