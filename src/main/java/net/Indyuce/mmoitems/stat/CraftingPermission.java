package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CraftingPermission extends StringStat {
	public CraftingPermission() {
		super("CRAFT_PERMISSION", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Crafting Recipe Permission", new String[] { "The permission needed to craft this item.", "Changing this value requires &o/mi reload recipes&7." }, new String[] { "all" });

		disable();
	}

	@Override
	public StatData whenInitialized(Object object) {
		return null;
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
	}
}