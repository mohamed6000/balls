package com.example;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFW;

public class App 
{
    public static void main( String[] args )
    {
        Window window = new Window(640, 480, "Balls");
        double last_frame_time = 0;

        Renderer r = new Renderer();
        float camera[] = {0, 3, 7};
        float player[] = {0, 0, 0};

        int tex_container  = r.CreateTexture("assets/textures/container.jpg", true, true);
        int tex_container2 = r.CreateTexture("assets/textures/container2.png", true, true);
        int tex_earth      = r.CreateTexture("assets/textures/earth.png", true, true);
        int tex_grass      = r.CreateTexture("assets/textures/texGrass.png", true, true);

        float rx = 0;

        // Level.
        int width[] = new int[1];
        int height[] = new int[1];
        int channels[] = new int[1];
        ByteBuffer data = stbi_load("assets/levels/test.bmp", width, height, channels, 4);
        int level[] = null;
        if (data != null) {
            level = new int[height[0]*width[0]];
            int total_bytes = width[0] * height[0] * 4;
            for (int i = 0; i < total_bytes; i += 4) {
                int red   = data.get(i)   & 0xFF;
                int green = data.get(i+1) & 0xFF;
                int blue  = data.get(i+2) & 0xFF;

                int index = i / 4;
                if (red == 0 && green == 0 && blue == 0) {
                    // Player ball position.
                    player[0] = (float)(index % width[0]);  // X
                    player[2] = (float)(index / width[0]);  // Z
                } else if (red == 128 && green == 64 && blue == 0) {
                    level[index] = tex_container;
                }else if (red == 213 && green == 106 && blue == 0) {
                    level[index] = tex_container2;
                } else {
                    level[index] = 0;
                }
            }
            stbi_image_free(data);
        }

        // Loop.
        while (!window.ShouldClose()) {
            double current_time = GLFW.glfwGetTime();
            float dt = (float)(current_time - last_frame_time);
            last_frame_time = current_time;

            int render_target_width  = window.getWidth();
            int render_target_height = window.getHeight();
            float ratio = (float)render_target_width / (float)render_target_height;

            // Input.
            if (window.IsButtonDown(Button.MOVE_UP)) {
                System.out.println("Forward");
            }
            if (window.IsButtonDown(Button.MOVE_DOWN)) {
                System.out.println("Backward");
            }

            if (window.IsButtonPressed(Button.ACTION)) {
                System.out.println("ACTION!");
            }

            // Physics.
            rx += 150 * dt;

            // Render.
            glViewport(0, 0, render_target_width, render_target_height);
            glClearColor(0.25f, 0.25f, 0.25f, 1);
            glClearDepth(1.0f);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

            // Perspective view.
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glFrustum(-0.1f*ratio, 0.1f*ratio, -0.1f, 0.1f, 0.1f, 100.0f);

            // Switch to modelview for rendering geometry.
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glTranslatef(-camera[0], -camera[1], -camera[2]);

            glEnable(GL_LIGHTING);

            // Draw stage tiles.
            for (int y = 0; y < height[0]; y++) {
                for (int x = 0; x < width[0]; x++) {
                    int tile = level[y*width[0]+x];
                    if (tile != 0) {
                        glBindTexture(GL_TEXTURE_2D, tile);
                        r.DrawCube(x, 0, y, 0.5f, 1,1,1,1);
                    }
                }
            }

            // Floor.
            {
                float x0 = -width[0]  * 0.5f;
                float z0 = -height[0] * 0.5f;
                float x1 = +width[0]  * 0.5f;
                float z1 = +height[0] * 0.5f;
                float y = -0.5f;
                glBindTexture(GL_TEXTURE_2D, tex_grass);
                glColor4f(1,1,1,1);
                glBegin(GL_TRIANGLES);
                glTexCoord2f(0,0);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(1,0);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z0);

                glTexCoord2f(1,1);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z1);

                glTexCoord2f(0,0);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(1,1);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z1);

                glTexCoord2f(0,1);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z1);
                glEnd();
            }

            // Draw player ball.
            {
                float ss = 1;
                glScalef(ss, ss, ss);
                glRotatef(rx, 0, 1, 0);
                glBindTexture(GL_TEXTURE_2D, tex_earth);
                r.DrawSphere(player[0], player[1], player[2], 1,1,1,1);
            }

            // Orthographic Projection.
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, render_target_width, 0, render_target_height, -2000, 2000);
            glDisable(GL_LIGHTING);
            glBindTexture(GL_TEXTURE_2D, 0);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // draw_quad(100, 100, 200, 200, 1,1,1,1);
            // draw_quad(350, 350, 360, 360, 0,0,1,1);

            // draw_circle(250, 250, 50, 32, 1,1,1,1);

            glFlush();

            window.SwapBuffers();
            window.UpdateEvents();
        }

        // Cleanup.
        window.Terminate();
    }
}
