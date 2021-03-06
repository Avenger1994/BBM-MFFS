package mffs.item.fortron

import mffs.item.card.ItemCard
import mffs.util.FortronUtility
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard with TFluidContainerItem
{
  override def fill(container: ItemStack, resource: FluidStack, doFill: Boolean): Int = if (resource.getFluid == FortronUtility.fluidFortron) resource.amount else 0

  override def drain(container: ItemStack, maxDrain: Int, doDrain: Boolean): FluidStack = new FluidStack(FortronUtility.fluidFortron, maxDrain)
}