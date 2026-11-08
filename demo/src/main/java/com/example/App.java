package com.example;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFW;

public class App 
{
    public static void draw_quad(float x0, float y0, float x1, float y1, float r, float g, float b, float a) {
        glColor4f(r,g,b,a);
        glBegin(GL_TRIANGLES);
            glVertex3f(x0, y0, 0);
            glVertex3f(x1, y0, 0);
            glVertex3f(x1, y1, 0);

            glVertex3f(x0, y0, 0);
            glVertex3f(x1, y1, 0);
            glVertex3f(x0, y1, 0);
        glEnd();
    }

    public static void draw_circle(float x, float y, float radius, int segments, float r, float g, float b, float a) {
        int i;
        float t0, t1, ct0, st0, ct1, st1;
        glColor4f(r,g,b,a);
        glBegin(GL_TRIANGLES);
        for (i = 0; i < segments; i++) {
            t0 = (float)i / segments * (float)Math.TAU;
            t1 = (float)(i+1) / segments * (float)Math.TAU;
            ct0 = (float)Math.cos(t0);
            st0 = (float)Math.sin(t0);
            ct1 = (float)Math.cos(t1);
            st1 = (float)Math.sin(t1);

            glVertex3f(x, y, 0);
            glVertex3f(x+ct0*radius, y+st0*radius, 0);
            glVertex3f(x+ct1*radius, y+st1*radius, 0);
        }
        glEnd();
    }

    public static void draw_cube(float x, float y, float z, float half_size, float r, float g, float b, float a) {
        glColor4f(r,g,b,a);
        glBegin(GL_TRIANGLES);
        glNormal3f(0, -1, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x+half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y-half_size, z-half_size);
        
        glNormal3f(0, -1, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y-half_size, z-half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x-half_size, y-half_size, z-half_size);
        
        glNormal3f(0, 0, -1);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z-half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x+half_size, y-half_size, z-half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        
        glNormal3f(0, 0, -1);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z-half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x-half_size, y+half_size, z-half_size);

        glNormal3f(-1, 0, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x-half_size, y-half_size, z-half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x-half_size, y+half_size, z-half_size);
        
        glNormal3f(-1, 0, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x-half_size, y+half_size, z-half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x-half_size, y+half_size, z+half_size);
        
        glNormal3f(+1, 0, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x+half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x+half_size, y-half_size, z-half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        
        glNormal3f(+1, 0, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x+half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x+half_size, y+half_size, z+half_size);
        
        glNormal3f(0, +1, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y+half_size, z+half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x+half_size, y+half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        
        glNormal3f(0, +1, 0);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y+half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z-half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x-half_size, y+half_size, z-half_size);
        
        glNormal3f(0, 0, +1);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 0);
        glVertex3f(x+half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z+half_size);
        
        glNormal3f(0, 0, +1);
        glTexCoord2f(0, 0);
        glVertex3f(x-half_size, y-half_size, z+half_size);
        glTexCoord2f(1, 1);
        glVertex3f(x+half_size, y+half_size, z+half_size);
        glTexCoord2f(0, 1);
        glVertex3f(x-half_size, y+half_size, z+half_size);
        glEnd();
    }

    public static void main( String[] args )
    {
        Window window = new Window(640, 480, "Balls");
        double last_frame_time = 0;
        float ry = 0;

        GL.createCapabilities();
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);

        // Lights config.
        glShadeModel(GL_SMOOTH);
        glEnable(GL_NORMALIZE);
        glEnable(GL_LIGHTING);

        // Ambient light.
        float ambient_color[] = {0.2f, 0.2f, 0.2f, 1.0f};
        glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambient_color);

        // Light source.
        float diffuse[]  = {1,1,1,1};
        float ambient[]  = {0.5f,0,0,1};
        float specular[] = {1,1,1,1};

        glLightfv(GL_LIGHT0, GL_DIFFUSE,  diffuse);
        glLightfv(GL_LIGHT0, GL_AMBIENT,  ambient);
        glLightfv(GL_LIGHT0, GL_SPECULAR, specular);
        glEnable(GL_LIGHT0);

        // Light position.
        float light_pos[] = {1,1,1,1};
        glLightfv(GL_LIGHT0, GL_POSITION, light_pos);

        int width[] = new int[1];
        int height[] = new int[1];
        int channels[] = new int[1];
        ByteBuffer data = stbi_load("assets/textures/container.jpg", width, height, channels, 4);

        int texture_id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture_id);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        // Set texture filters.
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
        // Set texture wrapping.
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

        // Loop.
        while (!window.ShouldClose()) {
            double current_time = GLFW.glfwGetTime();
            float dt = (float)(current_time - last_frame_time);
            last_frame_time = current_time;

            int render_target_width  = window.getWidth();
            int render_target_height = window.getHeight();
            float ratio = (float)render_target_width / (float)render_target_height;

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

            ry += 50.0f * dt;
            glTranslatef(0, 0, -2.5f);
            glRotatef(ry, 0, 1, 0);

            glEnable(GL_LIGHTING);

            glBindTexture(GL_TEXTURE_2D, texture_id);
            
            draw_cube(5, 0, 0, 0.5f, 1,1,1,1);
            draw_cube(0,0,0, 0.5f, 1,1,1,1);
            
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
