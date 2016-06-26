package org.inventivetalent.soundmuffler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.mcstats.MetricsLite;

import java.util.ArrayList;
import java.util.Collection;

public class SoundMuffler extends JavaPlugin implements Listener {

	double radius;
	float  amount;

	ItemStack soundMufflerItem;

	boolean is1_8 = Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1);

	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("PacketListenerApi")) {
			getLogger().severe("Please download & install PacketListenerApi: https://www.spigotmc.org/resources/2930");
			throw new RuntimeException("PacketListenerApi not installed");
		}
		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		radius = getConfig().getDouble("muffle.radius");
		amount = (float) getConfig().getDouble("muffle.amount");

		PluginAnnotations.loadAll(this, this);

		soundMufflerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) soundMufflerItem.getItemMeta();
		meta.setDisplayName("Sound Muffler");
		meta.setOwner("MHF_SoundMuffler");
		try {
			// https://skulls.inventivetalent.org/1507
			HeadTextureChanger.applyTextureToMeta(meta, HeadTextureChanger
					.createProfile("eyJ0aW1lc3RhbXAiOjE0NjY4NzUzNjk1OTIsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJJbnZlbnRpdmVHYW1lcyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE5ZWVkMTJmMzhmNDQxOWZjOGRmOWU3M2Q4YWZmNjlmMDhlOGJmMzhkZTA4N2I4YjliOTU3NWM4MmI5ZDI2OCJ9fX0=",
							"AC2g+9j+3kCkf15VbObKGR1jD2477HVnXf4JswFemxyD2RRvEAe7/3VbT2ByyGR/5rKCJnVmqa3EIm3yrhV856Q1ZtTJ3lDaFpxA+2b/bR6KrRxOlMSf2y6lc28Y1OVggjpVnIxQP+zrAHJbsDjocWaP5tN8+qzh4f+q3U3R8hgWHjjWIBeFKWAxUHgwVEEqMiAYGirZLTltUwV7gqXQDXQnyyb7+MmSjFgZ2pO5q9BmevLjL0FP6EVsJdsCqLZRMGB7jPCpXo9iLe1pxIkqrUddzz5e8nu+RYkdT9j/eyRGraJZnTAsHZfWbCEv5kJhqy7lVAQq2ldN09/98a1vlijaQG1Aoio7Fx5v4iUzw5VOg1CXHYVCanioK7kbQ6GfUja9cxfLjQlalNuipL4Og4EdDCR/iMhFMBKL+8/KXe3WjJoaHntQPHNNdcEiKq6ci8V8ShXPL7YLWWXIwi0ad/u5PTHkJe1kBmIQDAXW2Y+NfBWaUMY0tpE7Zmw7hPuWw/ZMjQ/Y7yXXf3YyEjyETkc0go7jG1bDwexLXhF3V2Rx+nFSl6GWdcpzcQpR+YH5fzTHFkHWOoTVcMC+RKeLUJW2sXyGR10+lqv9bOasU4T8snW0K2Q/os/dW4x77KR26q8iH/k2Ect6oILVr/IRs2fEkTRq4jJbOS73jStj8Qc="));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		soundMufflerItem.setItemMeta(meta);
		ShapedRecipe recipe = new ShapedRecipe(soundMufflerItem.clone());
		recipe.shape("www", "wnw", "www");
		recipe.setIngredient('w', Material.WOOL);
		recipe.setIngredient('n', Material.NOTE_BLOCK);
		Bukkit.addRecipe(recipe);

		PacketHandler.addHandler(new PacketHandler(this) {
			@PacketOptions(forcePlayer = true)
			@Override
			public void onSend(final SentPacket sentPacket) {
				if (sentPacket.hasPlayer()) {
					if ("PacketPlayOutCustomSoundEffect".equals(sentPacket.getPacketName()) || "PacketPlayOutNamedSoundEffect".equals(sentPacket.getPacketName())) {
						int c = (int) sentPacket.getPacketValue(is1_8 ? "b" : "c");
						int d = (int) sentPacket.getPacketValue(is1_8 ? "c" : "d");
						int e = (int) sentPacket.getPacketValue(is1_8 ? "d" : "e");
						final float f = (float) sentPacket.getPacketValue(is1_8 ? "e" : "f");

						final double x = c / 8.0D;
						final double y = d / 8.0D;
						final double z = e / 8.0D;

						RunnableFutureResult<Float> runnableFuture = new RunnableFutureResult<Float>() {
							@Override
							public Float evaluate() {
								Location location = new Location(sentPacket.getPlayer().getWorld(), x, y, z);
								Collection<Entity> entities;
								if (is1_8) {
									double squaredRadius = radius * radius;
									entities = new ArrayList<>();
									for (Entity entity : sentPacket.getPlayer().getWorld().getEntitiesByClass(ArmorStand.class)) {
										if (entity.getLocation().distanceSquared(location) < squaredRadius) {
											entities.add(entity);
										}
									}
								} else {
									entities = sentPacket.getPlayer().getWorld().getNearbyEntities(location, radius, radius, radius);
								}
								for (Entity entity : entities) {
									if (entity.getType() == EntityType.ARMOR_STAND) {
										if ("SoundMuffler".equals(((ArmorStand) entity).getCustomName())) {
											if (!is1_8) {
												entity.getLocation().getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, ((ArmorStand) entity).getEyeLocation(), 1, 0.75, 0.75, 0.75, 0);
											}
											return (f * amount);
										}
									}
								}
								return f;
							}
						};
						Bukkit.getScheduler().runTask(SoundMuffler.this, runnableFuture);
						float newVolume = f;
						try {
							newVolume = runnableFuture.get();
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
							return;
						}
						sentPacket.setPacketValue(is1_8 ? "e" : "f", newVolume);
					}
				}
			}

			@Override
			public void onReceive(ReceivedPacket receivedPacket) {
			}
		});

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockPlaceEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getItemInHand().getType() == Material.SKULL_ITEM) {
			if ("Sound Muffler".equals(event.getItemInHand().getItemMeta().getDisplayName())) {
				event.setCancelled(true);

				Vector direction = event.getPlayer().getLocation().getDirection();
				Vector targetVector = event.getPlayer().getEyeLocation().toVector();
				for (double d = 1; d < 8; d += 0.02) {
					targetVector = direction.clone().multiply(d).add(event.getPlayer().getEyeLocation().toVector());
					if (targetVector.toLocation(event.getPlayer().getWorld()).getBlock().getType().isSolid()) {
						break;
					}
				}

				Location location = targetVector.toLocation(event.getPlayer().getWorld()).subtract(0, 1.25, 0);
				ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
				armorStand.setVisible(false);
				armorStand.setGravity(false);
				//				armorStand.setMarker(true);
				armorStand.setCustomName("SoundMuffler");
				armorStand.setHelmet(soundMufflerItem.clone());
			}
		}
	}

	@EventHandler
	public void on(PlayerArmorStandManipulateEvent event) {
		if ("SoundMuffler".equals(event.getRightClicked().getCustomName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(EntityDamageEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getEntityType() == EntityType.ARMOR_STAND) {
			if ("SoundMuffler".equals(event.getEntity().getCustomName())) {
				event.getEntity().remove();
				event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 1.25, 0), soundMufflerItem.clone());
			}
		}
	}

}
