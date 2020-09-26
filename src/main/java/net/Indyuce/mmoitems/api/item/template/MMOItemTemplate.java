package net.Indyuce.mmoitems.api.item.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.PostLoadObject;

public class MMOItemTemplate extends PostLoadObject implements ItemReference {
	private final Type type;
	private final String id;

	// base item data
	private final Map<ItemStat, RandomStatData> base = new HashMap<>();

	private final Map<String, TemplateModifier> modifiers = new LinkedHashMap<>();
	private final Set<TemplateOption> options = new HashSet<>();

	/**
	 * Public constructor which can be used to register extra item templates
	 * using other addons or plugins
	 *
	 * @param type
	 *            The item type of your template
	 * @param id
	 *            The template identifier, it's ok if two templates with
	 *            different item types share the same ID
	 */
	public MMOItemTemplate(Type type, String id) {
		super(null);

		this.type = type;
		this.id = id;
	}

	/**
	 * Used to load mmoitem templates from config files
	 *
	 * @param type
	 *            The item type of your template
	 * @param config
	 *            The config file read to load the template
	 */
	public MMOItemTemplate(Type type, ConfigurationSection config) {
		super(config);
		Validate.notNull(config, "Could not load template config");

		this.type = type;
		this.id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");
	}

	@Override
	protected void whenPostLoaded(ConfigurationSection config) {
		if (config.contains("option"))
			for (TemplateOption option : TemplateOption.values())
				if (config.getBoolean("option." + option.name().toLowerCase().replace("_", "-")))
					options.add(option);

		if (config.contains("modifiers"))
			for (String key : config.getConfigurationSection("modifiers").getKeys(false))
				try {
					TemplateModifier modifier = new TemplateModifier(MMOItems.plugin.getTemplates(),
							config.getConfigurationSection("modifiers." + key));
					modifiers.put(modifier.getId(), modifier);
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.INFO,
							"Could not load modifier '" + key + "' from item template '" + type.getId() + "." + id + "': " + exception.getMessage());
				}

		Validate.notNull(config.getConfigurationSection("base"), "Could not find base item data");
		for (String key : config.getConfigurationSection("base").getKeys(false))
			try {
				String id = key.toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getStats().has(id), "Could not find stat with ID '" + id + "'");

				ItemStat stat = MMOItems.plugin.getStats().get(id);
				RandomStatData data = stat.whenInitialized(config.get("base." + key));
				if (data != null)
					base.put(stat, data);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO, "Could not load base item data '" + key + "' from item template '" + type.getId() + "."
						+ id + "': " + exception.getMessage());
			}
	}

	public Map<ItemStat, RandomStatData> getBaseItemData() {
		return base;
	}

	public Map<String, TemplateModifier> getModifiers() {
		return modifiers;
	}

	public boolean hasModifier(String id) {
		return modifiers.containsKey(id);
	}

	public TemplateModifier getModifier(String id) {
		return modifiers.get(id);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean hasOption(TemplateOption option) {
		return options.contains(option);
	}

	/**
	 * By default, item templates have item level 0 and no random tier. If the
	 * template has the 'tiered' recipe option, a random tier will be picked. If
	 * the template has the 'level-item' option, a random level will be picked
	 *
	 * @param player
	 *            The player for whom you are generating the itme
	 * @return Item builder with random level and tier?
	 */
	public MMOItemBuilder newBuilder(RPGPlayer player) {
		int itemLevel = hasOption(TemplateOption.LEVEL_ITEM) ? MMOItems.plugin.getTemplates().rollLevel(player.getLevel()) : 0;
		ItemTier itemTier = hasOption(TemplateOption.TIERED) ? MMOItems.plugin.getTemplates().rollTier() : null;
		return new MMOItemBuilder(this, itemLevel, itemTier);
	}

	/**
	 * @param itemLevel
	 *            The desired item level
	 * @param itemTier
	 *            The desired item tier, can be null
	 * @return Item builder with specific item level and tier
	 */
	public MMOItemBuilder newBuilder(int itemLevel, @Nullable ItemTier itemTier) {
		return new MMOItemBuilder(this, itemLevel, itemTier);
	}

	public enum TemplateOption {

		/**
		 * When the item is being generated, modifiers are rolled in a random
		 * order so you never the same modifiers again and again.
		 */
		ROLL_MODIFIER_CHECK_ORDER,

		/**
		 * When building an item based on this template, if no tier is
		 * specified, a random tier will be chosen for the item. By default, the
		 * item has no tier OR has the tier given in the config file.
		 */
		TIERED,

		/**
		 * When building an item based on this template, if no level is
		 * specified, a random level will be rolled based on the player's level
		 * or 0 if no player is specified. By default, items are generated with
		 * level 0
		 */
		LEVEL_ITEM;
	}
}
