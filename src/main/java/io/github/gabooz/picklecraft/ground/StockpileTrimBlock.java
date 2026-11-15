package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class StockpileTrimBlock extends Block implements EntityBlock {
    enum Part implements StringRepresentable {
        RIM("rim"), POST("post"), FLOOR("floor");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final VoxelShape FLOOR = box(0, 0, 0, 16, 15, 16);

    public StockpileTrimBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.RIM)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == Part.FLOOR ? FLOOR : net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.POST ? new StockpilePost(pos, state) : null;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(switch (state.getValue(PART)) {
            case RIM -> Items.STONE_BRICKS.getDefaultInstance();
            case POST -> params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof StockpilePost post
                ? post.timber() : Items.OAK_LOG.getDefaultInstance();
            case FLOOR -> Items.DIRT.getDefaultInstance();
        });
    }
}
