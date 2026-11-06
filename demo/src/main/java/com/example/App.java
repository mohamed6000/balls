package com.example;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void draw_quad(float x0, float y0, float x1, float y1, float r, float g, float b, float a) {
        glColor4f(r,g,b,a);
        glBegin(GL_TRIANGLES);
            glVertex3f(x0, y0, 0);
            glVertex3f(x1, y0, 0);
            glVertex3f(x1, y1, 0);

            glVertex3f(x0, y0, 0);
            glVertex3f(x1, y1, 0);
            glVertex3f(x0, y1, 0);
        glEnd();
    }

    public static void main( String[] args )
    {
        Window window = new Window(640, 480, "Test new framework");

        GL.createCapabilities();

        // Loop.
        while (!window.ShouldClose()) {
            int render_target_width  = window.getWidth();
            int render_target_height = window.getHeight();
            glViewport(0, 0, render_target_width, render_target_height);
            glClearColor(0.25f, 0.25f, 0.25f, 1);
            glClear(GL_COLOR_BUFFER_BIT);

            // Switch to modelview for rendering geometry.
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // Orthographic Projection.
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, render_target_width, 0, render_target_height, -2000, 2000);

            draw_quad(100, 100, 200, 200, 1,1,1,1);
            draw_quad(350, 350, 360, 360, 0,0,1,1);

            glFlush();

            window.SwapBuffers();
            window.UpdateEvents();
        }

        // Cleanup.
        window.Terminate();
    }
}
