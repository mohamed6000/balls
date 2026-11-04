package com.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.system.MemoryUtil;

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

        glfwShowWindow(window);

        // Loop.
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
        }

        // Cleanup.
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
