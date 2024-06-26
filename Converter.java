import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Converter {
    private static final String LETTER = "  .-=+*#%@";
            //" .,:ilwW";
            //" `.-':_,^=;><+!rc*/z?sLTv)J7(|Fi{C}fI31tlu[neoZ5Yxjya]2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@";
    private static final int MAX_WIDTH = 100;
    private static final double RED = 0.299;
    private static final double GREEN = 0.587;
    private static final double BLUE = 0.114;
    private static final double LETTER_RATIO = 2;
    private static final double IGNORE = 0.04;

    public static void main(String[] args) {
        if (args != null) {
            if (args.length == 1 && args[0] != null) {
                convert(args[0], true, MAX_WIDTH, LETTER_RATIO);
                return;
            }
            boolean validArgs = true;
            boolean dark = true;
            int maxWidth = MAX_WIDTH;
            double letterRatio = LETTER_RATIO;
            if (args.length == 4) {
                if (args[0] == null) {
                    validArgs = false;
                }
                if (args[1] == null) {
                    validArgs = false;
                } else {
                    if (args[1].equals("light")) {
                        dark = false;
                    } else if (!args[1].equals("dark")) {
                        validArgs = false;
                    }
                }
                if (args[2] == null) {
                    validArgs = false;
                } else {
                    try {
                        maxWidth = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.err.println("Error max width parsing: " + e.getMessage());
                        return;
                    }
                    if (maxWidth < 1) {
                        validArgs = false;
                    }
                }
                if (args[3] == null) {
                    validArgs = false;
                } else {
                    try {
                        letterRatio = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        System.err.println("Error letter ratio parsing: " + e.getMessage());
                        return;
                    }
                    if (letterRatio <= 0.0) {
                        validArgs = false;
                    }
                }
            } else {
                validArgs = false;
            }
            if (validArgs) {
                convert(args[0], dark, maxWidth, letterRatio);
            } else {
                System.out.println("Usage:");
                System.out.println("\tutility-name <inputImage>");
                System.out.println("or\tutility-name <inputImage> <light|dark> <maxWidth> <letterRatio>");
                System.out.println("Example:");
                System.out.println("\tjava Converter image.png");
                System.out.println("or\tjava Converter image.png light 100 1.3");
            }
        }
    }

    private static void convert(String inputFileName, boolean dark, int maxWidth, double letterRatio) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(inputFileName));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Image opening error: " + e.getMessage());
            return;
        }
        if (image == null) {
            System.err.println("Image opening error: can't read this image.");
            return;
        }
        int scaleWidth = image.getWidth() / maxWidth;
        if (scaleWidth == 0) {
            System.err.println("Error: too large maxWidth");
            return;
        }
        int scaleHeight = (int) (scaleWidth * letterRatio);
        int width = image.getWidth() / scaleWidth;
        int height = image.getHeight() / scaleHeight;
        double[][] brightnesses = new double[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                brightnesses[x][y] = getBrightness(image, scaleHeight, scaleWidth, x * scaleWidth, y * scaleHeight);
            }
        }
        increaseContrast(brightnesses, width, height);
        for (int y = 0; y < image.getHeight() / scaleHeight; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < image.getWidth() / scaleWidth; x++) {
                double brightness = brightnesses[x][y];
                int index = (int) ((LETTER.length() - 1) * brightness);
                if (!dark) {
                    index = LETTER.length() - 1 - index;
                }
                index = Math.min(Math.max(index, 0), LETTER.length() - 1);
                sb.append(LETTER.charAt(index));
            }
            System.out.println(sb);
        }
    }

    private static void increaseContrast(double[][] data, int n, int m) {
        double[] sorted = new double[n * m];
        for (int i = 0; i < n; i++) {
            System.arraycopy(data[i], 0, sorted, i * m, m);
        }
        Arrays.sort(sorted);
        double minValue = sorted[(int) ((sorted.length - 1) * IGNORE)];
        double maxValue = sorted[sorted.length - 1 - (int) ((sorted.length - 1) * IGNORE)];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                data[i][j] = (data[i][j] - minValue) / (maxValue - minValue);
                data[i][j] = Math.min(Math.max(data[i][j], 0), 1);
            }
        }
    }

    private static double getBrightness(BufferedImage image, int scaleHeight, int scaleWidth, int x, int y) {
        double sumOfBrightness = 0;
        for (int i = 0; i < scaleWidth; i++) {
            for (int j = 0; j < scaleHeight; j++) {
                Color color = new Color(image.getRGB(x + i, y + j));
                sumOfBrightness += RED * color.getRed() + GREEN * color.getGreen() + BLUE * color.getBlue();
            }
        }
        double result = sumOfBrightness / 256 / scaleHeight / scaleWidth;
        return Math.min(Math.max(result, 0), 1);
    }
}
