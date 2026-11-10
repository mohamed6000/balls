package com.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.system.MemoryUtil;

enum Button {
    UNKNOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    ACTION,
    COUNT,
}

public class Window {
    private long window_pointer;
    private int width, height;
    private boolean buttons[];
    private boolean previous_buttons[];

    public Window(int width, int height, String title) {
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to glfwInit");
        }

        this.width  = width;
        this.height = height;

        buttons = new boolean[Button.COUNT.ordinal()];
        previous_buttons = new boolean[Button.COUNT.ordinal()];

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

            Button button_code = Button.UNKNOWN;
            if (key == GLFW_KEY_A || key == GLFW_KEY_LEFT) {
                button_code = Button.MOVE_LEFT;
            } else if (key == GLFW_KEY_D || key == GLFW_KEY_RIGHT) {
                button_code = Button.MOVE_RIGHT;
            } else if (key == GLFW_KEY_W || key == GLFW_KEY_UP) {
                button_code = Button.MOVE_UP;
            } else if (key == GLFW_KEY_S || key == GLFW_KEY_DOWN) {
                button_code = Button.MOVE_DOWN;
            } else if (key == GLFW_KEY_ENTER) {
                button_code = Button.ACTION;
            }

            int button_index = button_code.ordinal();

            if (action == GLFW_PRESS) {
                buttons[button_index] = true;
            } else if (action == GLFW_RELEASE) {
                buttons[button_index] = false;
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
        System.arraycopy(buttons, 0, previous_buttons, 0, buttons.length);
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

    public boolean IsButtonDown(Button button) {
        int keycode = button.ordinal();
        assert(keycode < buttons.length);
        return buttons[keycode];
    }

    public boolean IsButtonPressed(Button button) {
        int keycode = button.ordinal();
        assert(keycode < buttons.length);
        boolean was_down = previous_buttons[keycode];
        boolean is_down = buttons[keycode];
        return is_down && !was_down;
    }
}
