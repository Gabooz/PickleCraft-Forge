package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class GoldFurnaceShellBlock extends Block {
    static final IntegerProperty PART = IntegerProperty.create("part", 0, 26);

    public GoldFurnaceShellBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    // Number the shell cells left to right, front to back, and bottom to top.
    static int part(BlockPos pos, BlockPos center, Direction front) {
        var right = front.getClockWise();
        int dx = pos.getX() - center.getX();
        int dz = pos.getZ() - center.getZ();
        int x = dx * right.getStepX() + dz * right.getStepZ() + 1;
        int z = -dx * front.getStepX() - dz * front.getStepZ() + 1;
        return (pos.getY() - center.getY() + 1) * 9 + z * 3 + x;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHELL_SHAPES[state.getValue(PART)][state.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue()];
    }

    // The shell steps in towards the chimney on the top course.
    private static final VoxelShape[][] SHELL_SHAPES = buildShapes();

    private static VoxelShape[][] buildShapes() {
        var shapes = new VoxelShape[27][4];
        for (int part = 0; part < shapes.length; part++) {
            int x = part % 3;
            int z = part / 3 % 3;
            var shape = switch (part / 9) {
                case 0 -> Shapes.or(inset(x, z, 0, 3, 0), inset(x, z, 3, 5, 1),
                    inset(x, z, 5, 13, 2), inset(x, z, 13, 16, 0));
                case 1 -> Shapes.block();
                default -> Shapes.or(inset(x, z, 0, 4, 2), inset(x, z, 4, 9, 5),
                    inset(x, z, 9, 13, 8), inset(x, z, 13, 16, 11));
            };
            var rotated = Shapes.rotateHorizontal(shape);
            for (var front : Direction.Plane.HORIZONTAL) {
                shapes[part][front.get2DDataValue()] = rotated.get(front);
            }
        }
        return shapes;
    }

    private static VoxelShape inset(int x, int z, int low, int high, int border) {
        return box(x == 0 ? border : 0, low, z == 0 ? border : 0, x == 2 ? 16 - border : 16,
            high, z == 2 ? 16 - border : 16);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(Blocks.RED_SANDSTONE));
    }
}
