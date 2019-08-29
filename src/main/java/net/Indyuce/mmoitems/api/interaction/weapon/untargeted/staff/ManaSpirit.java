package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.DamageInfo.DamageType;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class ManaSpirit implements StaffAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range, UntargetedWeapon untargeted) {
		new BukkitRunnable() {
			Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.4);
			Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;
			double r = .2;

			public void run() {
				ti++;
				if (ti > range)
					cancel();

				if (ti % 2 == 0)
					loc.getWorld().playSound(loc, Sound.BLOCK_SNOW_BREAK, 2, 2);
				List<Entity> targets = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					for (double item = 0; item < Math.PI * 2; item += Math.PI / 3.5) {
						Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(item), r * Math.sin(item), 0), loc);
						if (random.nextDouble() <= .6)
							MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc.clone().add(vec), Color.AQUA);
					}
					for (Entity target : targets)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							new AttackResult(untargeted, attackDamage).applyEffectsAndDamage(stats, nbt, (LivingEntity) target, DamageType.WEAPON, DamageType.PROJECTILE);
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}