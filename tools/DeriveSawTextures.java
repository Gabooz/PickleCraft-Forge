import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Copies the finished saw's visible wood and steel onto separate item sprites. */
class DeriveSawTextures {
    private static final int SIZE = 16;
    private static final Path TEXTURES =
        Path.of("src/main/resources/assets/picklecraft/textures/item");

    public static void main(String[] args) throws IOException {
        boolean check = args.length == 1 && args[0].equals("--check");
        if (args.length > 0 && !check) {
            throw new IllegalArgumentException("Usage: java tools/DeriveSawTextures.java [--check]");
        }
        BufferedImage saw = read(TEXTURES.resolve("iron_saw.png"));
        export(saw, "saw_handle.png", true, check);
        export(saw, "saw_blade.png", false, check);
    }

    private static void export(BufferedImage saw, String name, boolean wood, boolean check)
        throws IOException {
        Rectangle bounds = new Rectangle(0, 0, -1, -1);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pixel = saw.getRGB(x, y);
                if ((pixel >>> 24) != 0 && isWood(pixel, x, y) == wood) {
                    // A Rectangle grown by points spans their coordinate differences.
                    bounds.add(x, y);
                }
            }
        }
        if (bounds.width < 0) {
            throw new IOException("No visible parent pixels for " + name);
        }
        int left = (SIZE - (bounds.width + 1)) / 2;
        int top = (SIZE - (bounds.height + 1)) / 2;
        BufferedImage result = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y <= bounds.height; y++) {
            for (int x = 0; x <= bounds.width; x++) {
                int pixel = saw.getRGB(bounds.x + x, bounds.y + y);
                if ((pixel >>> 24) != 0 && isWood(pixel, bounds.x + x, bounds.y + y) == wood) {
                    result.setRGB(left + x, top + y, pixel);
                }
            }
        }
        Path path = TEXTURES.resolve(name);
        if (check) {
            int[] savedPixels = read(path).getRGB(0, 0, SIZE, SIZE, null, 0, SIZE);
            int[] derivedPixels = result.getRGB(0, 0, SIZE, SIZE, null, 0, SIZE);
            if (!Arrays.equals(savedPixels, derivedPixels)) {
                throw new IOException(name + " differs from the assembled saw. Rerun the extractor.");
            }
        } else {
            ImageIO.write(result, "png", path.toFile());
        }
        System.out.println((check ? "Verified " : "Derived ") + name + " from iron_saw.png");
    }

    private static BufferedImage read(Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() != SIZE || image.getHeight() != SIZE) {
            throw new IOException("Expected a 16 × 16 PNG at " + path);
        }
        return image;
    }

    private static boolean isWood(int pixel, int x, int y) throws IOException {
        // The assembled saw uses warm wood and neutral-to-cool steel, including their outlines.
        int warmth = ((pixel >>> 16) & 0xff) - (pixel & 0xff);
        if (warmth >= 20) {
            return true;
        }
        if (warmth <= 8) {
            return false;
        }
        throw new IOException("Ambiguous saw material at (" + x + ", " + y + ")");
    }
}
