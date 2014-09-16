package resonantinduction.mechanical;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import resonant.content.loader.ModManager;
import resonant.engine.content.debug.TileCreativeBuilder;
import resonant.lib.network.discriminator.PacketAnnotationManager;
import resonantinduction.core.ResonantPartFactory$;
import resonantinduction.core.ResonantTab;
import resonantinduction.mechanical.mech.MechanicalNode;
import resonantinduction.mechanical.fluid.pipe.PartPipe;
import resonantinduction.mechanical.fluid.pipe.PipeMaterials;
import resonantinduction.mechanical.mech.gear.PartGear;
import resonantinduction.mechanical.mech.gearshaft.PartGearShaft;
import resonantinduction.mechanical.mech.turbine.*;
import resonantinduction.mechanical.fluid.pipe.ItemPipe;
import resonantinduction.mechanical.fluid.transport.TilePump;
import resonantinduction.mechanical.mech.gear.ItemGear;
import resonantinduction.mechanical.mech.gearshaft.ItemGearShaft;
import resonantinduction.mechanical.machine.TileDetector;
import resonantinduction.mechanical.mech.process.crusher.TileMechanicalPiston;
import resonantinduction.mechanical.machine.edit.TileBreaker;
import resonantinduction.mechanical.machine.edit.TilePlacer;
import resonantinduction.mechanical.mech.process.grinder.TileGrindingWheel;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.lib.recipe.UniversalRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.mech.process.mixer.TileMixer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import universalelectricity.api.core.grid.NodeRegistry;

/**
 * Resonant Induction Mechanical Module
 *
 * @author DarkCow, Calclavia
 */
@Mod(modid = Mechanical.ID, name = "Resonant Induction Mechanical", version = "", dependencies = "before:ThermalExpansion;required-after:ResonantInductionCore;after:ResonantInduction|Archaic")
public class Mechanical
{
	/** Mod Information */
	public static final String ID = "ResonantInduction|Mechanical";
	public static final String NAME = Reference.name() + " Mechanical";

	@Instance(ID)
	public static Mechanical INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.mechanical.ClientProxy", serverSide = "resonantinduction.mechanical.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final ModManager contentRegistry = new ModManager().setPrefix(Reference.prefix()).setTab(ResonantTab.tab());

	// Energy
	public static Item itemGear;
	public static Item itemGearShaft;
	public static Block blockWindTurbine;
	public static Block blockWaterTurbine;
    public static Block blockElectricTurbine;

	// Transport
	public static Block blockConveyorBelt;
	public static Block blockManipulator;
	public static Block blockDetector;
	// public static Block blockRejector;
	public static Block blockSorter;

	// Fluids
	public static Block blockReleaseValve;
	public static Block blockPump;
	public static Item itemPipe;

	// Machines/Processes
	public static Block blockGrinderWheel;
	public static Block blockMixer;
	public static Block blockMechanicalPiston;

    // Block Breaker and Placer
    public static Block blockTileBreaker;
    public static Block blockTilePlacer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		MinecraftForge.EVENT_BUS.register(new MicroblockHighlightHandler());
		TileCreativeBuilder.register(new SchematicWindTurbine());
		TileCreativeBuilder.register(new SchematicWaterTurbine());
		NodeRegistry.register(IMechanicalNode.class, MechanicalNode.class);

		itemGear = contentRegistry.newItem(ItemGear.class);
		itemGearShaft = contentRegistry.newItem(ItemGearShaft.class);
        itemPipe = contentRegistry.newItem(ItemPipe.class);

        blockWindTurbine = contentRegistry.newBlock(TileWindTurbine.class);
		blockWaterTurbine = contentRegistry.newBlock(TileWaterTurbine.class);
		blockDetector = contentRegistry.newBlock(TileDetector.class);
		blockPump = contentRegistry.newBlock(TilePump.class);

		// Machines
		blockGrinderWheel = contentRegistry.newBlock(TileGrindingWheel.class);
		blockMixer = contentRegistry.newBlock(TileMixer.class);
		blockMechanicalPiston = contentRegistry.newBlock(TileMechanicalPiston.class);
		OreDictionary.registerOre("gear", itemGear);

        blockTileBreaker = contentRegistry.newBlock(TileBreaker.class);
        blockTilePlacer = contentRegistry.newBlock(TilePlacer.class);

        //TODO disable if electrical module is not enabled
        blockElectricTurbine = contentRegistry.newBlock(TileElectricTurbine.class);

		proxy.preInit();

		ResonantTab.itemStack(new ItemStack(blockGrinderWheel));

		PacketAnnotationManager.INSTANCE.register(TileWindTurbine.class);
        PacketAnnotationManager.INSTANCE.register(TileWaterTurbine.class);

		ResonantPartFactory$.MODULE$.register(PartGear.class);
		ResonantPartFactory$.MODULE$.register(PartGearShaft.class);
		ResonantPartFactory$.MODULE$.register(PartPipe.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Add recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 0), "SWS", "W W", "SWS", 'W', "plankWood", 'S', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 1), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 0), 'W', Blocks.cobblestone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 2), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 1), 'W', Items.iron_ingot));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 0), "S", "S", "S", 'S', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 1), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 0), 'S', Blocks.cobblestone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 2), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 1), 'S', Items.iron_ingot));
		//GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConveyorBelt, 4), "III", "GGG", 'I', Items.iron_ingot, 'G', itemGear));
		//GameRegistry.addRecipe(new ShapedOreRecipe(blockManipulator, "SSS", "SRS", "SCS", 'S', Items.iron_ingot, 'C', blockConveyorBelt, 'R', Blocks.redstone_block));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockDetector, "SWS", "SRS", "SWS", 'S', Items.iron_ingot, 'W', UniversalRecipe.WIRE.get()));
		//GameRegistry.addRecipe(new ShapedOreRecipe(blockSorter, "SSS", "SPS", "SRS", 'P', Blocks.piston, 'S', Items.iron_ingot, 'R', Blocks.redstone_block));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 0), "CWC", "WGW", "CWC", 'G', itemGear, 'C', Blocks.wool, 'W', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 1), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 0), 'C', Blocks.stone, 'W', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 2), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 1), 'C', Items.iron_ingot, 'W', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 0), "SWS", "WGW", "SWS", 'G', itemGear, 'W', "plankWood", 'S', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 1), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 0), 'W', Blocks.stone, 'S', Items.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 2), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get(), 'S', Items.stick));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockElectricTurbine, " B ", "BMB", " B ", 'B', UniversalRecipe.SECONDARY_PLATE.get(), 'M', UniversalRecipe.MOTOR.get()));

        GameRegistry.addRecipe(new ShapedOreRecipe(blockPump, "PPP", "GGG", "PPP", 'P', itemPipe, 'G', new ItemStack(itemGear, 1, 2)));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.ceramic().id()), "BBB", "   ", "BBB", 'B', Items.brick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.bronze().id()), "BBB", "   ", "BBB", 'B', "ingotBronze"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.plastic().id()), "BBB", "   ", "BBB", 'B', UniversalRecipe.RUBBER.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.iron().id()), "BBB", "   ", "BBB", 'B', Items.iron_ingot));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.steel().id()), "BBB", "   ", "BBB", 'B', "ingotSteel"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, PipeMaterials.fiberglass().id()), "BBB", "   ", "BBB", 'B', Items.diamond));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockMechanicalPiston, "SGS", "SPS", "SRS", 'P', Blocks.piston, 'S', Items.iron_ingot, 'R', Items.redstone, 'G', new ItemStack(itemGear, 1, 2)));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrinderWheel, "III", "LGL", "III", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'L', "logWood", 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockMixer, "IGI", "IGI", "IGI", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'G', itemGear));

        // block break and placer recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTileBreaker, "CGC", "CPC", "CDC", 'C', Blocks.cobblestone, 'G', itemGear, 'P', Blocks.piston, 'D', Items.diamond_pickaxe));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTilePlacer, "CGC", "CSC", "CRC", 'C', Blocks.cobblestone, 'G', itemGear, 'S', Items.iron_ingot, 'R', Blocks.redstone_block));
	}
}
