package com.example;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFW;

public class App 
{
    public static float Length3(float v[]) {
        return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }

    public static float LengthSquared3(float v[]) {
        return v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
    }

    public static void CrossProduct3(float a[], float b[], float out[]) {
        out[0] = a[1]*b[2] - a[2]*b[1];
        out[1] = a[2]*b[0] - a[0]*b[2];
        out[2] = a[0]*b[1] - a[1]*b[0];
    }

    public static void QuaternionFromAxisAngle(float axis[], float angle, float out[]) {
        float ca, sa;
        ca = (float)Math.cos(angle * 0.5f);
        sa = (float)Math.sin(angle * 0.5f);
        out[0] = axis[0] * sa;
        out[1] = axis[1] * sa;
        out[2] = axis[2] * sa;
        out[3] = ca;
    }

    public static void QuaternionMultiply(float q1[], float q2[], float out[]) {
        out[0] = q1[3] * q2[0] + q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1];
        out[1] = q1[3] * q2[1] - q1[0] * q2[2] + q1[1] * q2[3] + q1[2] * q2[0];
        out[2] = q1[3] * q2[2] + q1[0] * q2[1] - q1[1] * q2[0] + q1[2] * q2[3];
        out[3] = q1[3] * q2[3] - q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2];
    }

    public static float QuaternionNormalizeOrIdentity(float q[]) {
        float sq, factor;
        sq = (float)Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        if (sq == 0) {
            q[0] = 0;
            q[1] = 0;
            q[2] = 0;
            q[3] = 1;
            return sq;
        }
        factor = 1.0f / sq;
        q[0] *= factor;
        q[1] *= factor;
        q[2] *= factor;
        q[3] *= factor;
        return sq;
    }

    public static float QuaternionToAxisAngle(float q[], float axis[]) {
        float w, s;
        w = q[3];
        if (w > 1.0f)  w = 1.0f;
        if (w < -1.0f) w = -1.0f;

        s = (float)Math.sqrt(1.0f - w*w);
        if (s < 0.0001f) {
            axis[0] = 1; axis[1] = 0; axis[2] = 0;
        } else {
            axis[0] = q[0] / s; 
            axis[1] = q[1] / s; 
            axis[2] = q[2] / s;
        }

        return (float)Math.acos(w) * 2.0f;
    }

    public static void main( String[] args )
    {
        Window window = new Window(640, 480, "Balls");
        Renderer r = new Renderer();

        double last_pointer_x = 0, last_pointer_y = 0;
        float pointer_sensitivity = 0.1f;
        float camera_smoothness = 8.0f;

        float camera[] = {0, 0, 0};
        float camera_radius = 7.0f;
        float camera_rotation[] = {0, 0};
        float camera_forward[] = {0, 0, 0};
        float camera_right[] = {0, 0, 0};

        float player[] = {0, 0, 0};
        float player_orientation[] = {0, 0, 0, 1};
        float player_velocity[] = {0, 0, 0};
        float player_friction = 0.25f;
        float player_speed = 20.0f;
        float ball_radius = 0.5f;
        float rotation_axis[] = {0, 0, 0};

        int tex_container  = r.CreateTexture("assets/textures/container.jpg", true, true);
        int tex_container2 = r.CreateTexture("assets/textures/container2.png", true, true);
        int tex_earth      = r.CreateTexture("assets/textures/earth.png", true, true);
        int tex_grass      = r.CreateTexture("assets/textures/texGrass.png", true, true);

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

        window.PointerLockAndHide(true);
        double last_frame_time = GLFW.glfwGetTime();
        // Loop.
        while (!window.ShouldClose()) {
            double current_time = GLFW.glfwGetTime();
            float dt = (float)(current_time - last_frame_time);
            last_frame_time = current_time;

            int render_target_width  = window.getWidth();
            int render_target_height = window.getHeight();
            float ratio = (float)render_target_width / (float)render_target_height;

            // Input.
            double pointer_x = window.GetPointerX();
            double pointer_y = window.GetPointerY();
            float pointer_scroll = (float)window.GetPointerVerticalScroll();
            double delta_x = pointer_x - last_pointer_x;
            double delta_y = pointer_y - last_pointer_y;
            last_pointer_x = pointer_x;
            last_pointer_y = pointer_y;

            camera_rotation[0] += -delta_y * pointer_sensitivity;
            camera_rotation[1] +=  delta_x * pointer_sensitivity;
            camera_radius += pointer_scroll;
            if (camera_radius > 18.0f) camera_radius = 18.0f;
            if (camera_radius <  3.0f) camera_radius = 3.0f;

            if (camera_rotation[0] < -89.0f) camera_rotation[0] = -89.0f;
            if (camera_rotation[0] >  89.0f) camera_rotation[0] =  89.0f;

            float input_vector[] = {0, 0, 0};
            // Determine the movement vector.
            if (window.IsButtonDown(Button.MOVE_UP)) {
                input_vector[2] = -1;
            }
            if (window.IsButtonDown(Button.MOVE_DOWN)) {
                input_vector[2] = 1;
            }
            if (window.IsButtonDown(Button.MOVE_LEFT)) {
                input_vector[0] = -1;
            }
            if (window.IsButtonDown(Button.MOVE_RIGHT)) {
                input_vector[0] = 1;
            }
            
            float input_length = Length3(input_vector);
            if (input_length != 0) {
                input_vector[0] /= input_length;
                input_vector[2] /= input_length;
            }

            // Get camera forward and right vectors.
            float yaw = (float)Math.toRadians(camera_rotation[1]);
            float cyaw = (float)Math.cos(yaw);
            float syaw = (float)Math.sin(yaw);

            camera_forward[0] = syaw;
            camera_forward[2] = cyaw;
            camera_right[0]   = cyaw;
            camera_right[2]   = -syaw;

            // Local input to world input transform.
            float world_input_x = input_vector[0] * camera_right[0] + input_vector[2] * camera_forward[0];
            float world_input_z = input_vector[0] * camera_right[2] + input_vector[2] * camera_forward[2];

            // Apply acceleration to velocity.
            player_velocity[0] += world_input_x * player_speed * dt;
            player_velocity[2] += world_input_z * player_speed * dt;

            // Apply friction to velocity.
            player_velocity[0] *= (float)Math.pow(player_friction, dt);
            player_velocity[2] *= (float)Math.pow(player_friction, dt);

            float position_delta[] = {
                player_velocity[0] * dt,
                0,
                player_velocity[2] * dt,
            };
            
            player[0] += position_delta[0];
            player[2] += position_delta[2];

            // Update camera.
            {
                float rotation_y = (float)Math.toRadians(camera_rotation[0]);
                float cy = (float)Math.cos(rotation_y);
                float target_camera_x = player[0] + camera_radius * (float)(syaw * cy);
                float target_camera_y = player[1] + camera_radius * (float)Math.sin(-rotation_y);
                float target_camera_z = player[2] + camera_radius * (float)(cyaw * cy);

                camera[0] += (target_camera_x - camera[0]) * Math.min(1, camera_smoothness * dt);
                camera[1] += (target_camera_y - camera[1]) * Math.min(1, camera_smoothness * dt);
                camera[2] += (target_camera_z - camera[2]) * Math.min(1, camera_smoothness * dt);
            }

            float distance = Length3(position_delta);
            if (distance > 0.0001f) {
                rotation_axis[0] = position_delta[2] / distance;
                rotation_axis[1] = 0;
                rotation_axis[2] = -position_delta[0] / distance;

                float angle = distance / ball_radius;
                float delta_rotation[] = {0, 0, 0, 1};
                QuaternionFromAxisAngle(rotation_axis, angle, delta_rotation);

                float q[] = player_orientation;
                QuaternionMultiply(delta_rotation, q, player_orientation);
                QuaternionNormalizeOrIdentity(player_orientation);
            }

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

            glRotatef(-camera_rotation[0], 1, 0, 0);
            glRotatef(-camera_rotation[1], 0, 1, 0);
            glTranslatef(-camera[0], -camera[1], -camera[2]);

            glEnable(GL_LIGHTING);

            // Draw stage tiles.
            float half_cube_size = 0.5f;
            for (int y = 0; y < height[0]; y++) {
                for (int x = 0; x < width[0]; x++) {
                    int tile = level[y*width[0]+x];
                    if (tile != 0) {
                        glBindTexture(GL_TEXTURE_2D, tile);
                        r.DrawCube(x, half_cube_size, y, half_cube_size, 1,1,1,1);
                    }
                }
            }

            // Floor.
            {
                float x0 = -width[0];
                float z0 = -height[0];
                float x1 = +width[0];
                float z1 = +height[0];
                float y = 0;
                float u0 = 0;
                float v0 = 0;
                float u1 = 2;
                float v1 = 2;
                glBindTexture(GL_TEXTURE_2D, tex_grass);
                glColor4f(1,1,1,1);
                glBegin(GL_TRIANGLES);
                glTexCoord2f(u0,v0);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(u1,v0);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z0);

                glTexCoord2f(u1,v1);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z1);

                glTexCoord2f(u0,v0);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(u1,v1);
                glNormal3f(0, 1, 0);
                glVertex3f(x1, y, z1);

                glTexCoord2f(u0,v1);
                glNormal3f(0, 1, 0);
                glVertex3f(x0, y, z1);
                glEnd();
            }

            // Draw player ball.
            {
                float scale = ball_radius * 2.0f;
                float angle;
                float axis[] = {1, 0, 0};

                angle = QuaternionToAxisAngle(player_orientation, axis);

                glTranslatef(player[0], player[1]+ball_radius, player[2]);
                glRotatef((float)Math.toDegrees(angle), axis[0], axis[1], axis[2]);
                glScalef(scale, scale, scale);
                glBindTexture(GL_TEXTURE_2D, tex_earth);
                r.DrawSphere(0, 0, 0, 1,1,1,1);
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
