package reborncore.client.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Deprecated //use the correct package one
public class SlotFilteredVoid extends reborncore.client.gui.slots.SlotFilteredVoid
{

	private List<ItemStack> filter = new ArrayList<ItemStack>();

	public SlotFilteredVoid(IInventory par1iInventory, int id, int x, int y)
	{
		super(par1iInventory, id, x, y);
	}

	public SlotFilteredVoid(IInventory par1iInventory, int id, int x, int y, ItemStack[] filterList)
	{
		super(par1iInventory, id, x, y);
		for (ItemStack itemStack : filterList)
			this.filter.add(itemStack);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		for (ItemStack itemStack : filter)
			if (itemStack.getItem().equals(stack.getItem()) && itemStack.getItemDamage() == stack.getItemDamage())
				return false;

		return super.isItemValid(stack);
	}

	@Override
	public void putStack(ItemStack arg0)
	{
	}
}