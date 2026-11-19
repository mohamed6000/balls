package com.example;

import static org.lwjgl.opengl.GL11.*;
import java.io.File;
import org.lwjgl.glfw.GLFW;
import static com.example.MathHelper.*;

public class Game {
    private Window window;
    private Renderer r;

    private int level_width;
    private int level_height;
    private int[] level_data;

    private int tex_container;
    private int tex_container2;
    private int tex_earth;
    private int tex_grass;
    private int texture_ids[];

    float goal[];
    LightSource light0;
    Material default_material;
    Material goal_material;

    float player[];
    float player_orientation[];
    float ball_radius;

    public Game() {
        window = new Window(640, 480, "Balls");
        r = new Renderer();

        tex_container  = r.CreateTexture("assets/textures/container.jpg", true, true);
        tex_container2 = r.CreateTexture("assets/textures/container2.png", true, true);
        tex_earth      = r.CreateTexture("assets/textures/earth.png", true, true);
        tex_grass      = r.CreateTexture("assets/textures/texGrass.png", true, true);

        texture_ids = new int[]{
            tex_container,
            tex_container2,
            tex_earth,
            tex_grass,
        };

        goal = new float[]{0, 0, 0};
        light0 = new LightSource(0, 2, 1);
        default_material = new Material();
        default_material.SetShininess(0);
        goal_material = new Material();
        {
            goal_material.SetDiffuse(0.7f,0,0,1);
            goal_material.SetEmissive(0.7f,0,0,1);
            goal_material.SetShininess(128);
        }

        player = new float[]{0, 0, 0};
        player_orientation = new float[]{0, 0, 0, 1};
        ball_radius = 0.28f;
    }

    private void DrawScene(float red, float g, float b, float a) {
        // Draw stage tiles.
        float half_cube_size = 0.5f;
        for (int y = 0; y < level_height; y++) {
            for (int x = 0; x < level_width; x++) {
                int tile = level_data[y*level_width+x];
                if (tile != 0) {
                    glBindTexture(GL_TEXTURE_2D, tile);
                    r.DrawCube(x, half_cube_size, y, half_cube_size, red,g,b,a);
                }
            }
        }

        // Draw goal object.
        {
            goal_material.Update();
            glBindTexture(GL_TEXTURE_2D, 0);
            r.DrawSphere(goal[0], goal[1] + 0.5f, goal[2], red*0.8f, g*0.2f, b*0.2f, a*1);
        }

        // Draw player ball.
        {
            float scale = ball_radius * 2.0f;
            float angle;
            float axis[] = {1, 0, 0};

            angle = QuaternionToAxisAngle(player_orientation, axis);

            glPushMatrix();
            glTranslatef(player[0], player[1]+ball_radius, player[2]);
            glRotatef((float)Math.toDegrees(angle), axis[0], axis[1], axis[2]);
            glScalef(scale, scale, scale);

            default_material.Update();
            glBindTexture(GL_TEXTURE_2D, tex_earth);
            r.DrawSphere(0, 0, 0, red,g,b,a);
            glPopMatrix();
        }
    }

    private void DrawFloor(float r, float g, float b, float a) {
        float step = 1.0f;
        glBindTexture(GL_TEXTURE_2D, tex_grass);
        glColor4f(r,g,b,a);
        glBegin(GL_TRIANGLES);
        for (float x = -level_width; x < level_width; x += step) {
            for (float z = -level_height; z < level_height; z += step) {
                float x0 = x;
                float x1 = x + step;
                float z0 = z;
                float z1 = z + step;
                float y = 0;

                glTexCoord2f(0,0);
                glNormal3f(0,1,0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(1,0);
                glNormal3f(0,1,0);
                glVertex3f(x1, y, z0);

                glTexCoord2f(1,1);
                glNormal3f(0,1,0);
                glVertex3f(x1, y, z1);


                glTexCoord2f(0,0);
                glNormal3f(0,1,0);
                glVertex3f(x0, y, z0);

                glTexCoord2f(1,1);
                glNormal3f(0,1,0);
                glVertex3f(x1, y, z1);

                glTexCoord2f(0,1);
                glNormal3f(0,1,0);
                glVertex3f(x0, y, z1);
            }
        }
        glEnd();
    }

    public void Run() throws Exception {
        float[] background_color = {0.35f, 0.42f, 0.48f, 1};

        double last_pointer_x = 0, last_pointer_y = 0;
        float pointer_sensitivity = 0.1f;
        float camera_smoothness = 8.0f;

        float camera[] = {0, 0, 0};
        float camera_radius = 7.0f;
        float camera_rotation[] = {0, 0};
        float camera_forward[] = {0, 0, 0};
        float camera_right[] = {0, 0, 0};

        float player_velocity[] = {0, 0, 0};
        float player_friction = 0.25f;
        float player_speed = 20.0f;
        float rotation_axis[] = {0, 0, 0};

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

        level_width  = level.getWidth();
        level_height = level.getHeight();
        level_data   = level.getMap();

        player[0] = level.GetPlayerStartX();
        player[2] = level.GetPlayerStartZ();
        goal[0]   = level.GetGoalX();
        goal[2]   = level.GetGoalZ();

        // Fog params.
        float fog_density = 0.085f;
        float[] fog_color = background_color;
        boolean fog_linear = false;

        glEnable(GL_FOG);
        if (fog_linear) {
            glFogi(GL_FOG_MODE, GL_LINEAR);
            glFogf(GL_FOG_START, 1);
            glFogf(GL_FOG_END, 10);
        } else {
            glFogi(GL_FOG_MODE, GL_EXP2);
        }
        glFogfv(GL_FOG_COLOR, fog_color);
        glFogf(GL_FOG_DENSITY, fog_density);
        glHint(GL_FOG_HINT, GL_NICEST);

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
                // Go to next level or game over.
                if (level_index < (level_files.length-1)) {
                    level_index++;
                    level_path = level_directory_name + "/" + level_files[level_index];
                    if (!level.LoadFromFile(level_path, texture_ids)) {
                        throw new Exception("Failed to load level " + level_path);
                    }

                    level_width  = level.getWidth();
                    level_height = level.getHeight();
                    level_data   = level.getMap();

                    player[0] = level.GetPlayerStartX();
                    player[2] = level.GetPlayerStartZ();
                    player_velocity[0] = 0;
                    player_velocity[1] = 0;
                    player_velocity[2] = 0;
                    player_orientation[0] = 0;
                    player_orientation[1] = 0;
                    player_orientation[2] = 0;
                    player_orientation[3] = 1;
                    goal[0]  = level.GetGoalX();
                    goal[2]  = level.GetGoalZ();
                } else {
                    System.out.println("GAME OVER!!!!!!!!");
                }
            }

            // Render.
            glViewport(0, 0, render_target_width, render_target_height);
            glClearColor(background_color[0], background_color[1], background_color[2], background_color[3]);
            glClearDepth(1.0f);
            glClearStencil(0);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
            
            // Perspective view.
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glFrustum(-0.1f*ratio, 0.1f*ratio, -0.1f, 0.1f, 0.1f, 100.0f);

            glRotatef(-camera_rotation[0], 1, 0, 0);
            glRotatef(-camera_rotation[1], 0, 1, 0);
            glTranslatef(-camera[0], -camera[1], -camera[2]);

            // Switch to modelview for rendering geometry.
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glDepthMask(false);
            glEnable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 1, 0xFFFFFFFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
            // glStencilMask(0xFF);

            light0.Update();
            default_material.Update();
            DrawFloor(1, 1, 1, 1);

            glStencilFunc(GL_EQUAL, 1, 0xFFFFFFFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            // glStencilMask(0);

            glDepthMask(false);
            glDisable(GL_LIGHTING);
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glPushMatrix();
            glTranslatef(0, 0.002f, 0);
            glScalef(1, 0.001f, 1);
            // glRotatef(1, 0, 1, 0);
            DrawScene(0, 0, 0, 0.4f);
            glPopMatrix();

            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_LIGHTING);
            glDepthMask(true);
            glDisable(GL_STENCIL_TEST);

            light0.Update();
            default_material.Update();
            DrawScene(1, 1, 1, 1);

            // Draw debug light source.
            {
                glDisable(GL_LIGHTING);
                glBindTexture(GL_TEXTURE_2D, 0);
                r.DrawSphere(light0.GetPositionX(), light0.GetPositionY(), light0.GetPositionZ(), 1, 1, 1, 1);
            }


            // Orthographic Projection.
            // glMatrixMode(GL_PROJECTION);
            // glLoadIdentity();
            // glOrtho(0, render_target_width, 0, render_target_height, -2000, 2000);
            // glDisable(GL_LIGHTING);
            // glBindTexture(GL_TEXTURE_2D, 0);

            // glMatrixMode(GL_MODELVIEW);
            // glLoadIdentity();

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
