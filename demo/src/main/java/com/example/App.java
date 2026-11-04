package com.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.system.MemoryUtil;
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
        long window;

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to glfwInit");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE,   GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(640, 480, "Balls", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to glfwCreateWindow");
        }

        glfwSetKeyCallback(window, (window_ptr, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window_ptr, true);
            }
        });

        // Set GL context.
        glfwMakeContextCurrent(window);
        // VSync ON.
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Loop.
        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.25f, 0.25f, 0.25f, 1);
            glClear(GL_COLOR_BUFFER_BIT);

            glColor4f(1,1,1,1);
            glBegin(GL_TRIANGLES);
            glVertex2f(-0.5f, -0.5f);
            glVertex2f(0.5f,  -0.5f);
            glVertex2f(0,      0.5f);
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // Cleanup.
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
