package dark.core.prefab.tilenetwork.fluid;

import java.util.EnumSet;
import java.util.Map.Entry;

import com.dark.fluid.FluidHelper;
import com.dark.tilenetwork.INetworkPart;
import com.dark.tilenetwork.prefab.NetworkUpdateHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import dark.api.fluid.INetworkPipe;

/** Extension on the fluid container network to provide a more advanced reaction to fluid passing
 * threw each pipe. As well this doubled as a pressure network for those machines that support the
 * use of pressure.
 * 
 * @author Rseifert */
public class NetworkPipes extends NetworkFluidTiles
{
    private boolean processingRequest;
    public float pressureProduced;

    static
    {
        NetworkUpdateHandler.registerNetworkClass("FluidPipes", NetworkPipes.class);
    }

    public NetworkPipes()
    {
        super();
    }

    public NetworkPipes(INetworkPart... parts)
    {
        super(parts);
    }

    @Override
    public int fillNetworkTank(TileEntity source, FluidStack stack, boolean doFill)
    {
        int netFill = this.addFluidToNetwork(source, stack, doFill);
        if (netFill > 0)
        {
            return netFill;
        }
        return super.fillNetworkTank(source, stack, doFill);
    }

    /** Adds FLuid to this network from one of the connected Pipes
     * 
     * @param source - Were this liquid came from
     * @param stack - LiquidStack to be sent
     * @param doFill - actually fill the tank or just check numbers
     * @return the amount of liquid consumed from the init stack */
    public int addFluidToNetwork(TileEntity source, FluidStack stack, boolean doFill)
    {
        return this.addFluidToNetwork(source, stack, doFill, false);
    }

    /** Adds FLuid to this network from one of the connected Pipes
     * 
     * @param source - Were this liquid came from
     * @param stack - LiquidStack to be sent
     * @param doFill - actually fill the tank or just check numbers
     * @param allowStore - allows the network to store this liquid in the pipes
     * @return the amount of liquid consumed from the init stack */
    public int addFluidToNetwork(TileEntity source, FluidStack sta, boolean doFill, boolean allowStore)
    {
        int used = 0;
        FluidStack stack = sta.copy();

        if (!this.processingRequest && stack != null)
        {
            this.processingRequest = true;
            if (stack.amount > this.getMaxFlow(stack))
            {
                stack = FluidHelper.getStack(stack, this.getMaxFlow(stack));
            }

            /* Secondary fill target if the main target is not found */
            IFluidHandler tankToFill = null;
            int mostFill = 0;
            ForgeDirection fillDir = ForgeDirection.UNKNOWN;

            boolean found = false;

            /* FIND THE FILL TARGET FROM THE LIST OF FLUID RECIEVERS */
            for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : this.connctedFluidHandlers.entrySet())
            {
                IFluidHandler tankContainer = entry.getKey();
                if (tankContainer instanceof TileEntity && tankContainer != source && !(tankContainer instanceof INetworkPipe))
                {
                    for (ForgeDirection dir : entry.getValue())
                    {
                        if (tankContainer.canFill(dir, sta.getFluid()))
                        {
                            int fill = tankContainer.fill(dir, stack, false);

                            if (fill > mostFill)
                            {
                                tankToFill = tankContainer;
                                mostFill = fill;
                                fillDir = dir;
                            }
                        }
                    }
                }
                if (found)
                {
                    break;
                }
            }// End of tank finder
            if (tankToFill != null)
            {
                //TODO set up a list of tanks to actually fill rather than one at a time
                used = tankToFill.fill(fillDir, stack, doFill);
                // System.out.println("Seconday Target " + used + doFill);
            }
        }
        this.processingRequest = false;
        return used;
    }

    /** Gets the flow rate of the system using the lowest flow rate */
    public int getMaxFlow(FluidStack stack)
    {
        return 1000;
    }

    /** Updates after the pressure has changed a good bit */
    public void onPresureChange()
    {
        this.cleanUpMembers();
    }

}
