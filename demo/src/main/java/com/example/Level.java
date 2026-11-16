package com.example;

import static org.lwjgl.stb.STBImage.*;
import java.nio.ByteBuffer;

public class Level {
    private int width, height;
    private int map[];

    public Level() {
        width = 0;
        height = 0;
        map = null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getMap() {
        return map;
    }

    public boolean LoadFromFile(String filename, float player_position[], int texture_ids[]) {
        int channels[]   = {0};
        int out_width[]  = {0};
        int out_height[] = {0};
        ByteBuffer data = stbi_load(filename, out_width, out_height, channels, 4);
        if (data != null) {
            width  = out_width[0];
            height = out_height[0];
            map = new int[height*width];
            int total_bytes = width * height * 4;
            for (int i = 0; i < total_bytes; i += 4) {
                int red   = data.get(i)   & 0xFF;
                int green = data.get(i+1) & 0xFF;
                int blue  = data.get(i+2) & 0xFF;

                int index = i / 4;
                if (red == 0 && green == 0 && blue == 0) {
                    // Player ball position.
                    player_position[0] = (float)(index % width);  // X
                    player_position[2] = (float)(index / width);  // Z
                } else if (red == 128 && green == 64 && blue == 0) {
                    map[index] = texture_ids[0];
                }else if (red == 213 && green == 106 && blue == 0) {
                    map[index] = texture_ids[1];
                } else {
                    map[index] = 0;
                }
            }
            stbi_image_free(data);
            return true;
        }
        return false;
    }
}
