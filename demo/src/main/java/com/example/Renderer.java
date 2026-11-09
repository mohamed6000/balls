package com.example;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;

class Vertex {
    public float x, y, z;
    public float u, v;
    public float nx, ny, nz;

    public Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public Vertex(float x, float y, float z, float u, float v) {
        this(x, y, z, u, v, 0.0f, 0.0f, 0.0f);
    }

    public void SetNormal(float nx, float ny, float nz) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public void ComputeVertexNormalFromItsPosition() {
        float length = (float)Math.sqrt(x*x + y*y + z*z);
        this.nx = this.x / length;
        this.ny = this.y / length;
        this.nz = this.z / length;
    }
}

public class Renderer {
    private Vertex sphere_buffer[];

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

        int n = 0, segments_x = 32, segments_y = 32;
        int vertex_count = segments_x * segments_y * 4;
        sphere_buffer = new Vertex[vertex_count];
        float radius = 0.5f;
        for (int y = 0; y < segments_y; y++) {
            float y0 = (float)y / segments_y;
            float y1 = (float)(y+1) / segments_y;
            float stack_x0 = (float)Math.cos(y0 * Math.PI);
            float stack_y0 = (float)Math.sin(y0 * Math.PI);
            float stack_x1 = (float)Math.cos(y1 * Math.PI);
            float stack_y1 = (float)Math.sin(y1 * Math.PI);

            for (int x = 0; x < segments_x; x++) {
                float x0 = (float)x / segments_x;
                float x1 = (float)(x+1) / segments_x;
                float seg_x0 = (float)Math.cos(x0 * Math.TAU);
                float seg_y0 = (float)Math.sin(x0 * Math.TAU);
                float seg_x1 = (float)Math.cos(x1 * Math.TAU);
                float seg_y1 = (float)Math.sin(x1 * Math.TAU);

                // Vertex 0.
                sphere_buffer[n] = new Vertex(
                    radius * stack_y0 * seg_x0,
                    radius * stack_x0,
                    radius * stack_y0 * seg_y0,
                    x0, y0
                );
                sphere_buffer[n].ComputeVertexNormalFromItsPosition();
                n++;

                // Vertex 1.
                sphere_buffer[n] = new Vertex(
                    radius * stack_y0 * seg_x1,
                    radius * stack_x0,
                    radius * stack_y0 * seg_y1,
                    x1, y0
                );
                sphere_buffer[n].ComputeVertexNormalFromItsPosition();
                n++;

                // Vertex 2.
                sphere_buffer[n] = new Vertex(
                    radius * stack_y1 * seg_x1,
                    radius * stack_x1,
                    radius * stack_y1 * seg_y1,
                    x1, y1
                );
                sphere_buffer[n].ComputeVertexNormalFromItsPosition();
                n++;

                // Vertex 3.
                sphere_buffer[n] = new Vertex(
                    radius * stack_y1 * seg_x0,
                    radius * stack_x1,
                    radius * stack_y1 * seg_y0,
                    x0, y1
                );
                sphere_buffer[n].ComputeVertexNormalFromItsPosition();
                n++;
            }
        }
    }

    public int CreateTexture(String filename, boolean filter, boolean wrap) {
        int width[], height[], channels[], gl_filter, gl_wrap, texture_id;
        ByteBuffer data;

        width    = new int[1];
        height   = new int[1];
        channels = new int[1];
        data = stbi_load(filename, width, height, channels, 4);
        if (data != null) {
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

                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
                stbi_image_free(data);
        } else {
            texture_id = 0;
            System.out.println("Failed to load bitmap: " + filename);
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

    public void DrawSphere(float px, float py, float pz, float r, float g, float b, float a) {
        int segments_x = 32, segments_y = 32;
        glColor4f(r, g, b, a);
        glBegin(GL_TRIANGLES);
        for (int y = 0; y < segments_y; y++) {
            for (int x = 0; x < segments_x; x++) {
                int n = (x * segments_x + y) * 4;
                Vertex v0 = sphere_buffer[n];
                Vertex v1 = sphere_buffer[n+1];
                Vertex v2 = sphere_buffer[n+2];
                Vertex v3 = sphere_buffer[n+3];

                glTexCoord2f(v0.u, v0.v);
                glNormal3f(v0.nx, v0.ny, v0.nz);
                glVertex3f(px + v0.x, py + v0.y, pz + v0.z);

                glTexCoord2f(v1.u, v1.v);
                glNormal3f(v1.nx, v1.ny, v1.nz);
                glVertex3f(px + v1.x, py + v1.y, pz + v1.z);

                glTexCoord2f(v2.u, v2.v);
                glNormal3f(v2.nx, v2.ny, v2.nz);
                glVertex3f(px + v2.x, py + v2.y, pz + v2.z);

                glTexCoord2f(v0.u, v0.v);
                glNormal3f(v0.nx, v0.ny, v0.nz);
                glVertex3f(px + v0.x, py + v0.y, pz + v0.z);

                glTexCoord2f(v2.u, v2.v);
                glNormal3f(v2.nx, v2.ny, v2.nz);
                glVertex3f(px + v2.x, py + v2.y, pz + v2.z);

                glTexCoord2f(v3.u, v3.v);
                glNormal3f(v3.nx, v3.ny, v3.nz);
                glVertex3f(px + v3.x, py + v3.y, pz + v3.z);
            }
        }
        glEnd();
    }
}
