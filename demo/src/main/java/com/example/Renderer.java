package com.example;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;

public class Renderer {
    public Renderer() {
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
    }

    public int CreateTexture(String filename, boolean filter, boolean wrap) {
        int width[], height[], channels[], gl_filter, gl_wrap, texture_id;
        ByteBuffer data;

        width    = new int[1];
        height   = new int[1];
        channels = new int[1];
        data = stbi_load(filename, width, height, channels, 4);

        texture_id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture_id);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        // Set texture filters.
        gl_filter = filter ? GL_LINEAR : GL_NEAREST;
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, gl_filter);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, gl_filter);
    
        // Set texture wrapping.
        gl_wrap = wrap ? GL_REPEAT : GL_CLAMP;
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, gl_wrap);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, gl_wrap);

        if (data != null) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
            stbi_image_free(data);
        }
        return texture_id;
    }

    public int CreateTexture(String filename) {
        return CreateTexture(filename, false, false);
    }

    public void DrawQuad(float x0, float y0, float x1, float y1, float r, float g, float b, float a) {
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

    public void DrawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {
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

    public void DrawCube(float x, float y, float z, float half_size, float r, float g, float b, float a) {
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
}
