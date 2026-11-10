package com.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.system.MemoryUtil;

public class Window {
    private long window_pointer;
    private int width, height;
    private boolean buttons[];

    public Window(int width, int height, String title) {
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to glfwInit");
        }

        this.width  = width;
        this.height = height;

        buttons = new boolean[256];

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE,   GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window_pointer = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window_pointer == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to glfwCreateWindow");
        }

        glfwSetKeyCallback(window_pointer, (window_ptr, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window_ptr, true);
            }

            if ((key >= 'A') && (key <= 'Z')) {
                if (action == GLFW_PRESS) {
                    buttons[key] = true;
                } else if (action == GLFW_RELEASE) {
                    buttons[key] = false;
                }
            }
        });

        glfwSetWindowSizeCallback(window_pointer, (window_ptr, x, y) -> {
            this.width  = x;
            this.height = y;
        });

        // Set GL context.
        glfwMakeContextCurrent(window_pointer);
        // VSync ON.
        glfwSwapInterval(1);
        glfwShowWindow(window_pointer);
    }

    public void Terminate() {
        glfwDestroyWindow(window_pointer);
        glfwTerminate();
    }

    public boolean ShouldClose() {
        return glfwWindowShouldClose(window_pointer);
    }

    public void UpdateEvents() {
        glfwPollEvents();
    }

    public void SwapBuffers() {
        glfwSwapBuffers(window_pointer);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean IsKeyDown(int keycode) {
        return buttons[keycode];
    }
}
