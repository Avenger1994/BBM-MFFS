package mffs

import com.builtbroken.mc.lib.transform.vector.Pos
import com.mojang.authlib.GameProfile
import cpw.mods.fml.client.FMLClientHandler
import mffs.field.TileElectromagneticProjector
import mffs.field.gui.{GuiElectromagneticProjector, GuiForceMobilizer}
import mffs.field.mobilize.TileForceMobilizer
import mffs.item.gui.GuiFrequency
import mffs.production._
import mffs.render.fx._
import mffs.security.card.RenderIDCard
import mffs.security.card.gui.GuiCardID
import mffs.security.{GuiBiometricIdentifier, TileBiometricIdentifier}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient

class ClientProxy extends CommonProxy
{

  override def init()
  {
    super.init()
    MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.cardID, new RenderIDCard())
  }

  override def getClientWorld(): World = FMLClientHandler.instance.getClient.theWorld

  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    id match
    {
      case 0 =>
      {
        val tileEntity = world.getTileEntity(x, y, z)

        tileEntity match
        {
          case tile: TileFortronCapacitor => return new GuiFortronCapacitor(player, tile)
          case tile: TileElectromagneticProjector => return new GuiElectromagneticProjector(player, tile)
          case tile: TileCoercionDeriver => return new GuiCoercionDeriver(player, tile)
          case tile: TileBiometricIdentifier => return new GuiBiometricIdentifier(player, tile)
          case tile: TileForceMobilizer => return new GuiForceMobilizer(player, tile)
        }
      }
      case 1 => return new GuiFrequency(player, player.getCurrentEquippedItem)
      case 2 => return new GuiCardID(player, player.getCurrentEquippedItem)
    }

    return null
  }

  override def isOp(profile: GameProfile) = false

  override def renderBeam(world: World, position: Pos, target: Pos, color: (Float, Float, Float), age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXFortronBeam(world, position, target, color._1, color._2, color._3, age))
  }

  override def renderHologram(world: World, position: Pos, color: (Float, Float, Float), age: Int, targetPosition: Pos)
  {
    if (targetPosition != null)
    {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologram(world, position, color._1, color._2, color._3, age).setTarget(targetPosition))
    }
    else
    {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologram(world, position, color._1, color._2, color._3, age))
    }
  }

  override def renderHologramOrbit(world: World, orbitCenter: Pos, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramOrbit(world, orbitCenter, orbitCenter, color._1, color._2, color._3, age, maxSpeed))
  }

  override def renderHologramOrbit(controller: IEffectController, world: World, orbitCenter: Pos, position: Pos, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
    val fx = new FXHologramOrbit(world, orbitCenter, position, color._1, color._2, color._3, age, maxSpeed)
    fx.setController(controller)
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(fx)
  }

  override def renderHologramMoving(world: World, position: Pos, color: (Float, Float, Float), age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramMoving(world, position, color._1, color._2, color._3, age))
  }
}