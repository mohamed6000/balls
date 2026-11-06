package com.example;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

/**
 * Hello world!
 *
 */
public class App 
{
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

            glColor4f(1,1,1,1);
            glBegin(GL_TRIANGLES);
            glVertex3f(100, 100, 0);
            glVertex3f(200, 100, 0);
            glVertex3f(200, 200, 0);
            
            glVertex3f(100, 100, 0);
            glVertex3f(200, 200, 0);
            glVertex3f(100, 200, 0);
            glEnd();

            window.SwapBuffers();
            window.UpdateEvents();
        }

        // Cleanup.
        window.Terminate();
    }
}
