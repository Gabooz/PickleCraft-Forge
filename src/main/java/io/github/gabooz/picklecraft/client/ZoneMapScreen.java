package io.github.gabooz.picklecraft.client;

import io.github.gabooz.picklecraft.world.HexRegions;
import io.github.gabooz.picklecraft.world.ZoneMines;
import io.github.gabooz.picklecraft.zombies.ZombieZones;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

/** Creative planning map. Sampling the layout never requests or generates world chunks. */
public final class ZoneMapScreen extends Screen {
    private static final int GRASS = 0xff8caa67;
    private static final int FOREST = 0xff456f4d;
    private static final int MOUNTAIN = 0xffa8a394;
    private static final int RASTER = 3;
    private final long seed;
    private double centerX;
    private double centerZ;
    private double pixelsPerBlock = 35.0 / HexRegions.SIDE;
    private boolean follow = true;
    private boolean dragging;
    private int[] colors = new int[0];
    private int rasterWidth;
    private int rasterHeight;
    private double cachedX = Double.NaN;
    private double cachedZ;
    private double cachedZoom;

    public ZoneMapScreen(long seed) {
        super(Component.translatable("item.picklecraft.zone_map"));
        this.seed = seed;
    }

    private int left() { return 12; }
    private int top() { return 34; }
    private int right() { return width - 12; }
    private int bottom() { return height - 45; }
    private double originX() { return (left() + right()) / 2.0; }
    private double originY() { return (top() + bottom()) / 2.0; }
    private double screenX(double x) { return originX() + (x - centerX) * pixelsPerBlock; }
    private double screenY(double z) { return originY() + (z - centerZ) * pixelsPerBlock; }
    private double worldX(double x) { return centerX + (x - originX()) / pixelsPerBlock; }
    private double worldZ(double y) { return centerZ + (y - originY()) / pixelsPerBlock; }
    private boolean inside(double x, double y) { return x >= left() && x < right() && y >= top() && y < bottom(); }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        if (minecraft.player == null || !minecraft.player.isCreative()) onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (minecraft.player == null) return;
        if (follow) {
            centerX = minecraft.player.getX();
            centerZ = minecraft.player.getZ();
        }
        graphics.fill(0, 0, width, height, 0xff171d1b);
        graphics.text(font, title, 12, 12, 0xffe9e4cf);
        graphics.text(font, "N", width - 22, 12, 0xffe9e4cf);
        raster();
        graphics.enableScissor(left(), top(), right(), bottom());
        for (int row = 0; row < rasterHeight; row++) {
            int start = 0;
            while (start < rasterWidth) {
                int color = colors[row * rasterWidth + start];
                int end = start + 1;
                while (end < rasterWidth && colors[row * rasterWidth + end] == color) end++;
                graphics.fill(left() + start * RASTER, top() + row * RASTER,
                    Math.min(right(), left() + end * RASTER), Math.min(bottom(), top() + (row + 1) * RASTER), color);
                start = end;
            }
        }
        grid(graphics);
        player(graphics);
        graphics.disableScissor();
        graphics.outline(left() - 1, top() - 1, right() - left() + 2, bottom() - top() + 2, 0xff697467);
        int footer = height - 34;
        graphics.text(font, "Grasslands", 12, footer, GRASS);
        graphics.text(font, "Oak forest", 81, footer, 0xff88ba88);
        graphics.text(font, "Mountains", 151, footer, MOUNTAIN);
        graphics.text(font, "Fe iron   Au gold   Z zombies   C cows", 224, footer, 0xffe4d7ae);
        graphics.text(font, "Drag pan   Wheel zoom   Right-click teleport   R center   F follow", 12, height - 19, 0xffb7c0b3);
        graphics.text(font, String.format(java.util.Locale.ROOT, "X %.0f  Z %.0f", centerX, centerZ), 180, 12, 0xffb7c0b3);
        if (inside(mouseX, mouseY)) {
            var region = HexRegions.at(worldX(mouseX), worldZ(mouseY), seed);
            String label = name(region.kind()) + "  |  " + mine(region) + "  |  "
                + (ZombieZones.assigned(region) ? "Zombie challenge" : "No challenge")
                + (io.github.gabooz.picklecraft.cattle.CattleHerds.assigned(region) ? "  |  Cow herd" : "");
            graphics.text(font, label, 12, top() - 12, 0xffe9e4cf);
        }
    }

    private void raster() {
        int cols = Math.max(1, (right() - left() + RASTER - 1) / RASTER);
        int rows = Math.max(1, (bottom() - top() + RASTER - 1) / RASTER);
        if (cols == rasterWidth && rows == rasterHeight && cachedX == centerX && cachedZ == centerZ
                && cachedZoom == pixelsPerBlock) return;
        rasterWidth = cols;
        rasterHeight = rows;
        cachedX = centerX;
        cachedZ = centerZ;
        cachedZoom = pixelsPerBlock;
        colors = new int[cols * rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                var kind = HexRegions.at(worldX(left() + x * RASTER + 1.5), worldZ(top() + y * RASTER + 1.5), seed).kind();
                colors[y * cols + x] = switch (kind) {
                    case GRASSLANDS -> GRASS;
                    case OAK_FOREST -> FOREST;
                    case MOUNTAINS -> MOUNTAIN;
                };
            }
        }
    }

    private void grid(GuiGraphicsExtractor graphics) {
        double side = HexRegions.SIDE;
        int minR = (int) Math.floor(worldZ(top()) / (side * 1.5)) - 2;
        int maxR = (int) Math.ceil(worldZ(bottom()) / (side * 1.5)) + 2;
        for (int r = minR; r <= maxR; r++) {
            int minQ = (int) Math.floor(worldX(left()) / (side * Math.sqrt(3)) - r / 2.0) - 2;
            int maxQ = (int) Math.ceil(worldX(right()) / (side * Math.sqrt(3)) - r / 2.0) + 2;
            for (int q = minQ; q <= maxQ; q++) {
                double cx = screenX(side * Math.sqrt(3) * (q + r / 2.0));
                double cy = screenY(side * 1.5 * r);
                double radius = side * pixelsPerBlock;
                for (int edge = 0; edge < 3; edge++) {
                    double a = Math.toRadians(edge * 60 - 30);
                    double b = Math.toRadians((edge + 1) * 60 - 30);
                    line(graphics, cx + Math.cos(a) * radius, cy + Math.sin(a) * radius,
                        cx + Math.cos(b) * radius, cy + Math.sin(b) * radius, 0xff384b3d);
                }
                if (!inside(cx, cy)) continue;
                var region = HexRegions.cell(q, r, seed);
                String marker = switch (ZoneMines.ore(region)) {
                    case NONE -> "";
                    case IRON -> "Fe";
                    case GOLD -> "Au";
                };
                if (ZombieZones.assigned(region)) marker += marker.isEmpty() ? "Z" : "  Z";
                if (io.github.gabooz.picklecraft.cattle.CattleHerds.assigned(region)) marker += marker.isEmpty() ? "C" : "  C";
                if (!marker.isEmpty()) {
                    int textWidth = font.width(marker);
                    graphics.fill((int) cx - textWidth / 2 - 3, (int) cy - 6,
                        (int) cx + textWidth / 2 + 3, (int) cy + 6, 0xc019241e);
                    graphics.centeredText(font, marker, (int) cx, (int) cy - 4, 0xffffdc86);
                }
            }
        }
    }

    private void player(GuiGraphicsExtractor graphics) {
        var player = minecraft.player;
        double x = screenX(player.getX());
        double y = screenY(player.getZ());
        double angle = Math.toRadians(player.getYRot());
        double dx = -Math.sin(angle);
        double dz = Math.cos(angle);
        double tipX = x + dx * 10;
        double tipY = y + dz * 10;
        double backX = x - dx * 4;
        double backY = y - dz * 4;
        line(graphics, tipX, tipY, backX + dz * 4, backY - dx * 4, 0xffffffff);
        line(graphics, tipX, tipY, backX - dz * 4, backY + dx * 4, 0xffffffff);
        line(graphics, backX + dz * 4, backY - dx * 4, backX - dz * 4, backY + dx * 4, 0xffffffff);
        graphics.fill((int) x - 1, (int) y - 1, (int) x + 2, (int) y + 2, 0xffffffff);
    }

    private void line(GuiGraphicsExtractor graphics, double x1, double y1, double x2, double y2, int color) {
        if (Math.max(x1, x2) < left() || Math.min(x1, x2) >= right()
                || Math.max(y1, y2) < top() || Math.min(y1, y2) >= bottom()) return;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));
        for (int i = 0; i <= steps; i++) {
            int x = (int) Math.round(x1 + (x2 - x1) * i / steps);
            int y = (int) Math.round(y1 + (y2 - y1) * i / steps);
            if (inside(x, y)) graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static String name(HexRegions.Kind kind) {
        return switch (kind) {
            case GRASSLANDS -> "Grasslands";
            case MOUNTAINS -> "Mountains";
            case OAK_FOREST -> "Oak forest";
        };
    }

    private static String mine(HexRegions.Region region) {
        return switch (ZoneMines.ore(region)) {
            case NONE -> "No mine";
            case IRON -> "Iron mine";
            case GOLD -> "Gold mine";
        };
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && inside(event.x(), event.y())) {
            io.github.gabooz.picklecraft.map.ZoneMapNetwork.requestTeleport(
                (int) Math.floor(worldX(event.x())), (int) Math.floor(worldZ(event.y())));
            onClose();
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && inside(event.x(), event.y())) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!dragging || event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        follow = false;
        centerX -= dx / pixelsPerBlock;
        centerZ -= dy / pixelsPerBlock;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (!inside(mouseX, mouseY)) return false;
        double x = worldX(mouseX);
        double z = worldZ(mouseY);
        pixelsPerBlock = Math.clamp(pixelsPerBlock * Math.pow(1.25, vertical), 20.0 / HexRegions.SIDE, 400.0 / HexRegions.SIDE);
        follow = false;
        centerX = x - (mouseX - originX()) / pixelsPerBlock;
        centerZ = z - (mouseY - originY()) / pixelsPerBlock;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.input() == InputConstants.KEY_R || event.input() == InputConstants.KEY_F) {
            follow = event.input() == InputConstants.KEY_R || !follow;
            if (minecraft.player != null) {
                centerX = minecraft.player.getX();
                centerZ = minecraft.player.getZ();
            }
            return true;
        }
        return super.keyPressed(event);
    }
}
