package com.example.coen390_safehit.model;

public class Colors {
    public static final int[] colors = {
            0xFFB2DFDB, // Light Teal
            0xFFB3E5FC, // Light Blue
            0xFFC8E6C9, // Light Green
            0xFFDCEDC8, // Light Lime Green
            0xFFF0F4C3, // Light Lemon
            0xFFFFF9C4, // Light Yellow
            0xFFFFECB3, // Light Orange
            0xFFFFE0B2, // Light Peach
            0xFFFFCCBC, // Light Red Peach
            0xFFFFAB91, // Light Salmon
            0xFFFFCDD2, // Light Pink
            0xFFF8BBD0, // Lighter Pink
            0xFFE1BEE7, // Lavender
            0xFFD1C4E9, // Light Purple
            0xFFBBDEFB, // Light Sky Blue
            0xFFB39DDB, // Light Indigo
            0xFF90CAF9, // Soft Blue
            0xFF9FA8DA, // Light Blue Gray
            0xFFA5D6A7, // Light Moss Green
            0xFFFFF176  // Bright Light Yellow
    };


    //Randomly pick a color from the array
    public static int getRandomColor() {
        int randomIndex = (int) (Math.random() * colors.length);
        return colors[randomIndex];
    }
}
