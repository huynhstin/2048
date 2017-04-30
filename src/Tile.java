import java.awt.Color;

/**
 * Tile object
 * @author Justin Huynh
 */

class Tile {
    private int pow;

    Tile(boolean startsAsFour) {
        pow = startsAsFour ? 2 : 1;
    }

    void increasePow() {
        pow++;
    }

    int getPow() {
        return pow;
    }

    int value() {
        return (int) Math.pow(2, pow);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value());
    }

    Color getColor() {
        switch (pow) {
            case 1:  return new Color(238,228,218); // 2
            case 2:  return new Color(237,224,200); // 4
            case 3:  return new Color(242,177,121); // 8
            case 4:  return new Color(245,149,99);  // 16
            case 5:  return new Color(246,124,95);  // 32
            case 6:  return new Color(246,94,59);   // 64
            case 7:  return new Color(237,207,114); // 128
            case 8:  return new Color(237,204,97);  // 256
            case 9:  return new Color(237,200,80);  // 512
            case 10: return new Color(237,197,63);  // 1024
            case 11: return new Color(237,194,46);  // 2048
        }
        return new Color(32, 32, 32);               // 4096+
    }
}