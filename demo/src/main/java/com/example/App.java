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
            glClearColor(0.25f, 0.25f, 0.25f, 1);
            glClear(GL_COLOR_BUFFER_BIT);

            glBegin(GL_TRIANGLES);
            glColor4f(1,0,0,1);
            glVertex2f(-0.5f, -0.5f);
            glColor4f(0,1,0,1);
            glVertex2f(0.5f,-0.5f);
            glColor4f(0,0,1,1);
            glVertex2f(0.5f,0.5f);

            glColor4f(1,0,0,1);
            glVertex2f(-0.5f, -0.5f);
            glColor4f(0,0,1,1);
            glVertex2f(0.5f,0.5f);
            glColor4f(1,1,0,1);
            glVertex2f(-0.5f,0.5f);
            glEnd();

            window.SwapBuffers();
            window.UpdateEvents();
        }

        // Cleanup.
        window.Terminate();
    }
}
