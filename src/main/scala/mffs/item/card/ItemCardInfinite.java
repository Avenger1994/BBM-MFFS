package mffs.item.card;

import calclavia.api.mffs.card.ICardInfinite;
import mffs.card.ItemCard;

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
public class ItemCardInfinite extends ItemCard implements ICardInfinite
{
	public ItemCardInfinite(int id)
	{
		super(id, "cardInfinite");
	}
}