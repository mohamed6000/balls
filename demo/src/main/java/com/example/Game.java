package com.example;

import static org.lwjgl.opengl.GL11.*;
import java.io.File;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import static com.example.MathHelper.*;

class Particle {
    public float start_position[];
    public float position[];
    public float velocity[];
    public float color[];
    public float angle;
    public float angular_velocity;
    public float scale;
    public float life;
    public float max_life;
    public boolean active;

    public Particle(float x, float y, float z, float mx, float my, float mz) {
        start_position = new float[]{x, y, z};
        position = new float[]{x, y, z};
        velocity = new float[]{mx, my, mz};
        color    = new float[]{1,1,1,1};
        scale    = 0.25f;
        life     = 0;
        max_life = 1.5f;
        active = false;
    }

    public Particle() {
        this(0,0,0, 0,0,0);
    }

    public void SetColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }
}

class ParticleEmitter {
    private int particle_count;
    private Particle particles[];
    private Random rand;
    private int next_particle_index;

    public ParticleEmitter(int count, Random rand) {
        particle_count = count;
        particles = new Particle[particle_count];
        for (int index = 0; index < particle_count; index++) {
            particles[index] = new Particle();
        }
        if (rand != null) {
            this.rand = rand;
        } else {
            this.rand = new Random();
        }
    }

    public ParticleEmitter(int count) {
        this(count, null);
    }

    public ParticleEmitter() {
        this(128, null);
    }

    public void SpawnBurst(float x, float y, float z, int spawn_count,
                           float speed, float min_velocity, float max_velocity, 
                           float scale, 
                           float r, float g, float b) {
        for (int index = 0; index < spawn_count; index++) {
            Particle p = particles[next_particle_index];
            next_particle_index = (next_particle_index + 1) % particle_count;

            p.start_position[0] = x;
            p.start_position[1] = y;
            p.start_position[2] = z;

            p.position[0] = x;
            p.position[1] = y;
            p.position[2] = z;

            p.velocity[0] = (rand.nextFloat() * 2 - 1) * speed;
            p.velocity[1] = min_velocity + rand.nextFloat() * (max_velocity - min_velocity);
            p.velocity[2] = (rand.nextFloat() * 2 - 1) * speed;

            p.angle = rand.nextFloat() * 360;
            p.angular_velocity = (rand.nextFloat() * 2 - 1) * 180;
            p.scale = scale;
            p.life  = 0;
            p.max_life = 0.5f + rand.nextFloat() * 0.75f;
            p.active = true;
            p.SetColor(r, g, b, 1);
        }
    }

    public void Update(float dt) {
        float GRAVITY = -9.8f;
        for (int index = 0; index < particle_count; index++) {
            Particle p = particles[index];
            // Skip inactive particles.
            if (p.active == false) continue;

            // Update position.
            p.velocity[1] += GRAVITY * dt;
            p.position[0] += p.velocity[0] * dt;
            p.position[1] += p.velocity[1] * dt;
            p.position[2] += p.velocity[2] * dt;

            p.angle += p.angular_velocity * dt;

            p.life += dt;
            if (p.life >= p.max_life) {
                p.active = false;
            }
        }
    }

    public void Draw(float camera_rotation[], int tex_particle) {
        // glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glBindTexture(GL_TEXTURE_2D, tex_particle);
        for (int index = 0; index < particle_count; index++) {
            Particle p = particles[index];
            if (!p.active) continue;

            float fade = 1 - Math.min(1, p.life / p.max_life);
            float half = p.scale * 0.5f;
            
            glPushMatrix();
            {
                glTranslatef(p.position[0], p.position[1], p.position[2]);
                glRotatef(camera_rotation[1], 0,1,0);
                glRotatef(camera_rotation[0], 1,0,0);
                glRotatef(p.angle, 0,0,1);

                glColor4f(p.color[0]*fade,p.color[1]*fade,p.color[2]*fade,p.color[3]*fade);
                glBegin(GL_TRIANGLES);
                glTexCoord2f(0,0);  glVertex3f(-half,-half,0);
                glTexCoord2f(1,0);  glVertex3f( half,-half,0);
                glTexCoord2f(1,1);  glVertex3f( half, half,0);
                
                glTexCoord2f(0,0);  glVertex3f(-half,-half,0);
                glTexCoord2f(1,1);  glVertex3f( half,half,0);
                glTexCoord2f(0,1);  glVertex3f(-half,half,0);
                glEnd();
            }
            glPopMatrix();
        }
        glDisable(GL_BLEND);
        glDepthMask(true);
    }
}

public class Game {
    private Window window;
    private Renderer r;
    Font font;

    private int level_width;
    private int level_height;
    private int[] level_data;

    private int tex_container;
    private int tex_container2;
    private int tex_earth;
    private int tex_grass;
    private int tex_particle;
    private int texture_ids[];

    float goal[];
    float shadow_matrix[];
    LightSource light0;
    Material default_material;
    Material goal_material;

    float camera_rotation[];
    float camera_forward[];
    float camera_right[];
    float camera_up[];

    float player[];
    float player_orientation[];
    float ball_radius;

    ParticleEmitter emitter;

    public Game() throws Exception {
        window = new Window(640, 480, "Balls");
        r = new Renderer();
        font = new Font("C:/windows/fonts/arial.ttf", 128);

        tex_container  = r.CreateTexture("assets/textures/container.jpg", true, true);
        tex_container2 = r.CreateTexture("assets/textures/container2.png", true, true);
        tex_earth      = r.CreateTexture("assets/textures/earth.png", true, true);
        tex_grass      = r.CreateTexture("assets/textures/texGrass.png", true, true);
        tex_particle   = r.CreateTexture("assets/textures/particle.png", true, true);

        texture_ids = new int[]{
            tex_container,
            tex_container2,
            tex_earth,
            tex_grass,
            tex_particle,
        };

        goal = new float[]{0, 0, 0};
        light0 = new LightSource(0, 20, 1);
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

        camera_rotation  = new float[]{0, 0};
        camera_forward   = new float[]{0, 0, 0};
        camera_right     = new float[]{0, 0, 0};
        camera_up        = new float[]{0, 0, 0};

        shadow_matrix = new float[16];

        emitter = new ParticleEmitter();
    }

    private void DrawScene(float red, float g, float b, float a, boolean shadow_pass) {
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

            glPushMatrix();
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            if (shadow_pass) {
                glEnable(GL_TEXTURE_2D);
                glTranslatef(player[0], player[1]+1, player[2]);
                font.DrawText3D("Player", 0.52f, red,g,b,a);
                glDisable(GL_TEXTURE_2D);
            } else {
                glDisable(GL_LIGHTING);
                glTranslatef(player[0], player[1]+1, player[2]);
                font.DrawText3D("Player", 0.52f, red,g,b,a);
                glEnable(GL_LIGHTING);
            }
            glDisable(GL_BLEND);
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

    private void CreatePlanarShadowMatrix(float light_x, float light_y, float light_z, float plane_y, 
                                          float[] out_matrix) {
        float effective_light_y = light_y;
        if (Math.abs(light_y - plane_y) < 0.01f) {
            effective_light_y = plane_y + 0.01f;
        }
        float d = effective_light_y - plane_y;

        // Column major matrix.
        out_matrix[0] = d;
        out_matrix[1] = 0;
        out_matrix[2] = 0;
        out_matrix[3] = 0;

        out_matrix[4] = -light_x;
        out_matrix[5] = -plane_y;
        out_matrix[6] = -light_z;
        out_matrix[7] = -1;

        out_matrix[8]  = 0;
        out_matrix[9]  = 0;
        out_matrix[10] = d;
        out_matrix[11] = 0;

        out_matrix[12] = light_x * plane_y;
        out_matrix[13] = plane_y * effective_light_y;
        out_matrix[14] = light_z * plane_y;
        out_matrix[15] = effective_light_y;
    }

    public void Run() throws Exception {
        float[] background_color = {0.35f, 0.42f, 0.48f, 1};

        double last_pointer_x = 0, last_pointer_y = 0;
        float pointer_sensitivity = 0.1f;
        float camera_smoothness = 8.0f;

        float camera[] = {0, 0, 0};
        float camera_radius = 7.0f;

        float player_velocity[] = {0, 0, 0};
        float player_friction = 0.25f;
        float player_speed = 20.0f;
        float rotation_axis[] = {0, 0, 0};

        String level_directory_name = "assets/levels";
        File levels_directory = new File(level_directory_name);
        String[] level_files = levels_directory.list();
        int level_index = 0;

        float goal_time_wait = 0;
        boolean current_goal_reached = false;

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

        // glEnable(GL_FOG);
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
            float yaw  = (float)Math.toRadians(camera_rotation[1]);
            float cyaw = (float)Math.cos(yaw);
            float syaw = (float)Math.sin(yaw);

            camera_forward[0] = syaw;
            camera_forward[2] = cyaw;
            camera_right[0]   = cyaw;
            camera_right[2]   = -syaw;

            CrossProduct3(camera_forward, camera_right, camera_up);

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
                if (Math.abs(player_velocity[0]) > 3.0f) {
                    emitter.SpawnBurst(player[0], player[1]+ball_radius, player[2], 12, 2, 1, 3, 0.18f, 1,0.8f,0.2f);
                }
                player[0] -= delta_px;
                player_velocity[0] = 0;
            }

            player[2] += delta_pz;
            if (CheckCollision(player[0], player[2], ball_radius, level)) {
                if (Math.abs(player_velocity[2]) > 3.0f) {
                    emitter.SpawnBurst(player[0], player[1]+ball_radius, player[2], 12, 2, 1, 3, 0.18f, 1,0.8f,0.2f);
                }
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

            if ((DistanceSquared(player, goal) < 0.5f) && !current_goal_reached) {
                current_goal_reached = true;
                goal_time_wait = 1.0f;
                emitter.SpawnBurst(goal[0], goal[1]+0.5f, goal[2], 48, 3.5f, 2.5f, 6.5f, 0.3f, 1.0f,0.35f,0.2f);
            }

            if (current_goal_reached) {
                // Go to next level or game over.
                if (goal_time_wait <= 0) {
                    current_goal_reached = false;
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
                } else {
                    goal_time_wait -= dt;
                    if (goal_time_wait <= 0) goal_time_wait = 0;
                }
            }

            // Update particles.
            emitter.Update(dt);

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

            glEnable(GL_STENCIL_TEST);
            glStencilFunc(GL_ALWAYS, 1, 0xFFFFFFFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
            glStencilMask(0xFF);

            light0.Update();
            default_material.Update();
            DrawFloor(1, 1, 1, 1);

            glStencilFunc(GL_EQUAL, 1, 0xFFFFFFFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_INCR);
            glStencilMask(0xFF);

            glDepthMask(false);
            glDisable(GL_LIGHTING);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            CreatePlanarShadowMatrix(light0.GetPositionX(),
                                     light0.GetPositionY(),
                                     light0.GetPositionZ(),
                                     0.002f,
                                     shadow_matrix);

            glPushMatrix();
            glMultMatrixf(shadow_matrix);
            DrawScene(0, 0, 0, 0.45f, true);
            glPopMatrix();

            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_LIGHTING);
            glDepthMask(true);
            glDisable(GL_STENCIL_TEST);

            light0.Update();
            default_material.Update();
            DrawScene(1, 1, 1, 1, false);

            // Draw debug light source.
            {
                glDisable(GL_LIGHTING);
                glBindTexture(GL_TEXTURE_2D, 0);
                r.DrawSphere(light0.GetPositionX(), light0.GetPositionY(), light0.GetPositionZ(), 1, 1, 1, 1);

                glPushMatrix();
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glTranslatef(light0.GetPositionX(), light0.GetPositionY(), light0.GetPositionZ());
                font.DrawText3D("Light0", 2.15f, 1,1,1,1);
                glPopMatrix();
            }

            // Draw Particles.
            emitter.Draw(camera_rotation, tex_particle);


            // Orthographic Projection.
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, render_target_width, 0, render_target_height, -2000, 2000);
            glDisable(GL_LIGHTING);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            font.DrawTextScaled2D("Frame Time:"+(int)(dt*1000)+"ms", 0, 0, 32, 1,1,0,1);

            glEnable(GL_DEPTH_TEST);

            glFlush();

            window.SwapBuffers();
            window.UpdateEvents();
        }

        // Cleanup.
        window.Terminate();
    }
}
