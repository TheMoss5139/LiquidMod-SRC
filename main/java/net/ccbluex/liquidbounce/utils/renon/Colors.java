package net.ccbluex.liquidbounce.utils.renon;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Colors {
    public static int getColor(Color color) {
        return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getColor(int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }

    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }
    
    public static int getColor(Color Color, int alpha) {
    	int red = Color.getRed();
    	int green = Color.getGreen();
    	int blue = Color.getBlue();
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }
    
    public static int getColor2(int Color, int alpha) {
        Color color2 = new Color(Color);
        int red = color2.getRed();
        int green = color2.getGreen();
        int blue = color2.getBlue();
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }

    public static Color getColor3(int Color, int alpha) {
        Color color2 = new Color(Color);
        int red = color2.getRed();
        int green = color2.getGreen();
        int blue = color2.getBlue();
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return new Color(color);
    }
    
    public static List<Color> getColorsList() {
    	List<Color> list = new ArrayList<>();
    	list.add(Color.BLACK);
    	list.add(Color.BLUE);
    	list.add(Color.CYAN);
    	list.add(Color.DARK_GRAY);
    	list.add(Color.GRAY);
    	list.add(Color.GREEN);
    	list.add(Color.LIGHT_GRAY);
    	list.add(Color.MAGENTA);
    	list.add(Color.ORANGE);
    	list.add(Color.PINK);
    	list.add(Color.RED);
    	list.add(Color.WHITE);
    	list.add(Color.YELLOW);
    	return list;
    }
    
    public static Color getRandomColor() {
    	Color color = null;
    	int random = Utils.random(0, getColorsList().size());
    	color = getColorsList().get(random);
    	return color;
    }
    
    public static Color getRandomColor2() {
    	Color color = new Color(Utils.random(0, 255), Utils.random(0, 255), Utils.random(0, 255), Utils.random(0, 255));
    	return color;
    }
    
    public static Color getColor(int red, int green, int blue, int alpha, boolean needalpha) {
    	return new Color(red, green, blue, needalpha ? alpha : 255);
    }
}
