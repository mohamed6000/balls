package com.example;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import static com.example.MathHelper.*;

public class App 
{
    public static void main( String[] args )
    {
        try {
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
            float ball_radius = 0.28f;
            float rotation_axis[] = {0, 0, 0};

            float goal[] = {0, 0, 0};
            LightSource light0 = new LightSource(0, 2, 1);
            Material default_material = new Material();
            Material goal_material = new Material();
            {
                goal_material.SetDiffuse(0.7f,0,0,1);
                goal_material.SetEmissive(0.7f,0,0,1);
                goal_material.SetShininess(128);
            }

            int tex_container  = r.CreateTexture("assets/textures/container.jpg", true, true);
            int tex_container2 = r.CreateTexture("assets/textures/container2.png", true, true);
            int tex_earth      = r.CreateTexture("assets/textures/earth.png", true, true);
            int tex_grass      = r.CreateTexture("assets/textures/texGrass.png", true, true);

            int texture_ids[] = {
                tex_container,
                tex_container2,
                tex_earth,
                tex_grass,
            };

            String level_directory_name = "assets/levels";
            File levels_directory = new File(level_directory_name);
            String[] level_files = levels_directory.list();
            int level_index = 0;

            if (level_files.length == 0) {
                throw new Exception("No level was found");
            } 

            // Level.
            Level level = new Level();
            String level_path = level_directory_name + "/" + level_files[level_index];
            if (!level.LoadFromFile(level_path, texture_ids)) {
                throw new Exception("Failed to load level " + level_path);
            }

            int level_width  = level.getWidth();
            int level_height = level.getHeight();
            int level_data[] = level.getMap();

            player[0] = level.GetPlayerStartX();
            player[2] = level.GetPlayerStartZ();
            goal[0]   = level.GetGoalX();
            goal[2]   = level.GetGoalZ();

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
                camera_rotation[1] += -delta_x * pointer_sensitivity;
                camera_radius += -pointer_scroll;
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

                float delta_px = player_velocity[0] * dt;
                float delta_pz = player_velocity[2] * dt;

                player[0] += delta_px;
                if (CheckCollision(player[0], player[2], ball_radius, level)) {
                    player[0] -= delta_px;
                    player_velocity[0] = 0;
                }

                player[2] += delta_pz;
                if (CheckCollision(player[0], player[2], ball_radius, level)) {
                    player[2] -= delta_pz;
                    player_velocity[2] = 0;
                }

                float position_delta[] = {
                    player_velocity[0] * dt,
                    0,
                    player_velocity[2] * dt,
                };

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

                if (DistanceSquared(player, goal) < 0.5f) {
                    System.out.println("Goal reached!");
                } else {
                    System.out.println("Goal not reached");
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

                // Light.
                light0.Update();
                default_material.Update();

                // Draw stage tiles.
                float half_cube_size = 0.5f;
                for (int y = 0; y < level_height; y++) {
                    for (int x = 0; x < level_width; x++) {
                        int tile = level_data[y*level_width+x];
                        if (tile != 0) {
                            glBindTexture(GL_TEXTURE_2D, tile);
                            r.DrawCube(x, half_cube_size, y, half_cube_size, 1,1,1,1);
                        }
                    }
                }

                // Floor.
                {
                    float x0 = -level_width;
                    float z0 = -level_height;
                    float x1 = +level_width;
                    float z1 = +level_height;
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

                // Draw goal object.
                {
                    goal_material.Update();
                    glBindTexture(GL_TEXTURE_2D, 0);
                    r.DrawSphere(goal[0], goal[1] + 0.5f, goal[2], 0.8f, 0.2f, 0.2f, 1);
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

                    default_material.Update();
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
