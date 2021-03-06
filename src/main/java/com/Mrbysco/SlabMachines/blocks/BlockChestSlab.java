package com.mrbysco.slabmachines.blocks;

import com.mrbysco.slabmachines.SlabMachines;
import com.mrbysco.slabmachines.SlabReference;
import com.mrbysco.slabmachines.gui.SlabGuiHandler;
import com.mrbysco.slabmachines.tileentity.TileChestSlab;
import com.mrbysco.slabmachines.utils.SlabUtil;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockChestSlab extends BlockSlab{
	public static final PropertyEnum<BlockChestSlab.Variant> VARIANT = PropertyEnum.<BlockChestSlab.Variant>create("variant", BlockChestSlab.Variant.class);
	private static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockChestSlab() {
		super(Material.WOOD);
		
		this.setUnlocalizedName(SlabReference.MOD_PREFIX + "chest_slab".replaceAll("_", ""));
		this.setRegistryName("chest_slab");
		this.setHardness(2.5F);
	    this.setSoundType(SoundType.WOOD);
		
		useNeighborBrightness = true;
		setCreativeTab(SlabMachines.slabTab);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockChestSlab.Variant.DEFAULT).withProperty(HALF, EnumBlockHalf.BOTTOM).withProperty(FACING, EnumFacing.NORTH));
	    this.setHarvestLevel("axe", 0);
	}
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
    		float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    
	@Override
	public String getUnlocalizedName(int meta) {
		return this.getUnlocalizedName();
	}

	@Override
	public boolean isDouble() {
		return false;
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
        return BlockChestSlab.Variant.DEFAULT;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).ordinal();
		if (state.getValue(HALF) == EnumBlockHalf.TOP) {
			meta |= 0b1000;
		}
		return meta;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.VALUES[meta & 0b0111];
		if (!EnumFacing.Plane.HORIZONTAL.apply(facing)) facing = EnumFacing.NORTH;
		boolean top = meta >> 3 == 1;
		return getDefaultState()
				.withProperty(FACING, facing)
				.withProperty(HALF, top ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
	}
	
	@Override
    protected BlockStateContainer createBlockState()
    {
		return new BlockStateContainer(this, VARIANT, HALF, FACING);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) 
		{
			return true;
		}
		else
		{
			int id = state.getValue(HALF) == EnumBlockHalf.TOP ? SlabGuiHandler.GUI_CHEST_SLAB_TOP : SlabGuiHandler.GUI_CHEST_SLAB_BOTTOM;
			
			playerIn.openGui(SlabMachines.instance, id, worldIn, pos.getX(), pos.getY(), pos.getZ());
            playerIn.addStat(StatList.CHEST_OPENED);

			return true;
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote) {
			TileChestSlab te = SlabUtil.getTileSlab(worldIn, pos, state.getValue(BlockSlab.HALF), TileChestSlab.class);
			if(te != null)
			{
				InventoryHelper.dropInventoryItems(worldIn, pos, te);
				worldIn.updateComparatorOutputLevel(pos, this);
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	public static enum Variant implements IStringSerializable
    {
        DEFAULT;

        public String getName()
        {
            return "default";
        }
    }
	
	@Override
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileChestSlab();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstone(SlabUtil.getTileSlab(worldIn, pos, blockState.getValue(BlockSlab.HALF), TileChestSlab.class));
	}
}
