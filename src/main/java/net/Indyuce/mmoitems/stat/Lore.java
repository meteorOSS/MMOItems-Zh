package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Lore extends StringStat {
	public Lore() {
		super(new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial()), "Lore", new String[] { "The item lore." }, "lore", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.LORE).enable("Write in the chat the lore line you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("lore")) {
				List<String> lore = config.getConfig().getStringList(inv.getItemId() + ".lore");
				if (lore.size() < 1)
					return true;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				config.getConfig().set(inv.getItemId() + ".lore", lore);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + ChatColor.translateAlternateColorCodes('&', last) + ChatColor.GRAY + "'.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = config.getConfig().getConfigurationSection(inv.getItemId()).contains("lore") ? config.getConfig().getStringList(inv.getItemId() + ".lore") : new ArrayList<>();
		lore.add(message);
		config.getConfig().set(inv.getItemId() + ".lore", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("lore"))
			lore.add(ChatColor.RED + "No lore.");
		else if (config.getStringList(path + ".lore").isEmpty())
			lore.add(ChatColor.RED + "No lore.");
		else
			config.getStringList(path + ".lore").forEach(str -> lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', str)));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a line.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last line.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(ItemStat.LORE, new StringListData(config.getStringList("lore")));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		List<String> lore = new ArrayList<>();
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(line -> {
			line = ChatColor.translateAlternateColorCodes('&', line);
			array.add(line);
			lore.add(line);
		});
		item.getLore().insert("lore", lore);
		item.addItemTag(new ItemTag("MMOITEMS_LORE", array.toString()));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_LORE"))
			mmoitem.setData(ItemStat.LORE, new StringListData(new JsonParser().parse(item.getString("MMOITEMS_LORE")).getAsJsonArray()));
	}
}