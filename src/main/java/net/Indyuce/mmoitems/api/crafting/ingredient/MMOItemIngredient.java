package net.Indyuce.mmoitems.api.crafting.ingredient;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.stat.DisplayName;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.jetbrains.annotations.NotNull;

public class MMOItemIngredient extends Ingredient {
	private final MMOItemTemplate template;

	// TODO check level code.
	private final int level;
	private final String display;

	public MMOItemIngredient(MMOLineConfig config) {
		super("mmoitem", config);

		config.validate("type", "id");
		Type type = MMOItems.plugin.getTypes().getOrThrow(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, config.getString("id"));

		level = config.getInt("level", 0);
		display = config.contains("display") ? config.getString("display") : findName();
	}

	public MMOItemIngredient(ConfigMMOItem mmoitem) {
		super("mmoitem", mmoitem.getAmount());

		template = mmoitem.getTemplate();
		level = 0;
		display = findName();
	}

	public MMOItemTemplate getTemplate() {
		return template;
	}

	@Override
	public String getKey() {
		return "mmoitem:" + template.getType().getId().toLowerCase() + (level != 0 ? "-" + level : "") + "_" + template.getId().toLowerCase();
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#item#", display).replace("#level#", level != 0 ? "lvl." + level + " " : "").replace("#amount#", "" + getAmount());
	}

	@NotNull
	@Override
	public ItemStack generateItemStack(@NotNull RPGPlayer player) {

		// For display, obviously
		ItemStack item = template.newBuilder(player).build().newBuilder().build(true);
		item.setAmount(getAmount());
		return item;
	}

	@Override
	public String toString() {
		return getKey();
	}

	private String findName() {
		String name = null;
		if (template.getBaseItemData().containsKey(ItemStats.NAME))
			 name = template.getBaseItemData().get(ItemStats.NAME).toString().replace("<tier-color>", "").replace("<tier-name>", "").replace("<tier-color-cleaned>", "");

		if (template.getBaseItemData().containsKey(ItemStats.MATERIAL) && name == null)
			name = MMOUtils.caseOnWords(((MaterialData) template.getBaseItemData().get(ItemStats.MATERIAL)).getMaterial().name().toLowerCase().replace("_", " "));

		if (name == null) { name = "Unrecognized Item"; }
		if (level != 0) { return DisplayName.appendUpgradeLevel(name, level); }
		return name;
	}
}
