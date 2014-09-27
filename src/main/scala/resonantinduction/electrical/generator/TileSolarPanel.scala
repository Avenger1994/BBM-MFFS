package resonantinduction.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.content.spatial.block.SpatialBlock
import resonantinduction.core.{Reference, Settings}
import resonantinduction.electrical.battery.TileBattery
import universalelectricity.core.transform.region.Cuboid

class TileSolarPanel extends TileBattery {

    energy.setCapacity(Settings.SOLAR_ENERGY * 20)
    ioMap_$eq(728.asInstanceOf[Short])
    setTextureName("solarPanel_top")
    bounds(new Cuboid(0, 0, 0, 1, 0.3f, 1))
    isOpaqueCube(false)

  @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister) {
    SpatialBlock.icon.put("solarPanel_side", iconReg.registerIcon(Reference.prefix + "solarPanel_side"))
    SpatialBlock.icon.put("solarPanel_bottom", iconReg.registerIcon(Reference.prefix + "solarPanel_bottom"))
    super.registerIcons(iconReg)
  }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon = {
    if (side == 0) {
      return SpatialBlock.icon.get("solarPanel_bottom")
    }
    else if (side == 1) {
      return getIcon
    }
    return SpatialBlock.icon.get("solarPanel_side")
  }

  override def update {
    if (!this.worldObj.isRemote) {
      if (this.worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky) {
        if (this.worldObj.isDaytime) {
          if (!(this.worldObj.isThundering || this.worldObj.isRaining)) {
            this.electricNode.addEnergy(ForgeDirection.UNKNOWN, Settings.SOLAR_ENERGY, true)
          }
        }
      }
    }
    super.update
  }
}