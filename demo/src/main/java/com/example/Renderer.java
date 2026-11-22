package com.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

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

class LightSource {
    private float position[];
    private float diffuse[];
    private float ambient[];
    private float specular[];

    public LightSource(float x, float y, float z) {
        position = new float[4];
        position[0] = x;
        position[1] = y;
        position[2] = z;
        position[3] = 1;

        diffuse = new float[4];
        diffuse[0] = 1;
        diffuse[1] = 1;
        diffuse[2] = 1;
        diffuse[3] = 1;

        ambient = new float[4];
        ambient[0] = 0;
        ambient[1] = 0;
        ambient[2] = 0;
        ambient[3] = 1;

        specular = new float[4];
        specular[0] = 1;
        specular[1] = 1;
        specular[2] = 1;
        specular[3] = 1;
    }

    public float GetPositionX() { return position[0]; }
    public float GetPositionY() { return position[1]; }
    public float GetPositionZ() { return position[2]; }

    public void SetPosition(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void SetDiffuse(float r, float g, float b) {
        diffuse[0] = r;
        diffuse[1] = g;
        diffuse[2] = b;
    }

    public void SetAmbient(float r, float g, float b) {
        ambient[0] = r;
        ambient[1] = g;
        ambient[2] = b;
    }

    public void SetSpecular(float r, float g, float b) {
        specular[0] = r;
        specular[1] = g;
        specular[2] = b;
    }

    public void Update() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glEnable(GL_NORMALIZE);
        
        // Ambient light.
        // float ambient_color[] = {0.2f, 0.2f, 0.2f, 1.0f};
        // glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambient_color);
        
        // Light position.
        glLightfv(GL_LIGHT0, GL_POSITION, position);
        
        // Light color.
        glLightfv(GL_LIGHT0, GL_DIFFUSE,  diffuse);
        glLightfv(GL_LIGHT0, GL_SPECULAR, specular);
        glLightfv(GL_LIGHT0, GL_AMBIENT,  ambient);
    }
}

class Material {
    private float diffuse[];
    private float ambient[];
    private float specular[];
    private float emissive[];
    private float shininess;

    public Material() {
        diffuse = new float[4];
        diffuse[0] = 1;
        diffuse[1] = 1;
        diffuse[2] = 1;
        diffuse[3] = 1;

        ambient = new float[4];
        ambient[0] = 0;
        ambient[1] = 0;
        ambient[2] = 0;
        ambient[3] = 1;

        specular = new float[4];
        specular[0] = 1;
        specular[1] = 1;
        specular[2] = 1;
        specular[3] = 1;

        emissive = new float[4];
        emissive[0] = 0;
        emissive[1] = 0;
        emissive[2] = 0;
        emissive[3] = 1;

        shininess = 0.2f * 128;
    }

    public void SetDiffuse(float r, float g, float b, float a) {
        diffuse[0] = r;
        diffuse[1] = g;
        diffuse[2] = b;
        diffuse[3] = a;
    }

    public void SetAmbient(float r, float g, float b, float a) {
        ambient[0] = r;
        ambient[1] = g;
        ambient[2] = b;
        ambient[3] = a;
    }

    public void SetSpecular(float r, float g, float b, float a) {
        specular[0] = r;
        specular[1] = g;
        specular[2] = b;
        specular[3] = a;
    }

    public void SetEmissive(float r, float g, float b, float a) {
        emissive[0] = r;
        emissive[1] = g;
        emissive[2] = b;
        emissive[3] = a;
    }

    public void SetShininess(float value) {
        if (value > 128) value = 128;
        shininess = value;
    }

    public void Update() {
        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE,  diffuse);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular);
        glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, emissive);
        glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shininess);
    }
}

class Font {
    private int texture_id;
    private STBTTBakedChar.Buffer char_data;
    private int bitmap_width, bitmap_height;
    private int character_height;

    public Font(String filename, int pixel_size) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(filename));
        ByteBuffer ttf_buffer = BufferUtils.createByteBuffer(bytes.length);
        ttf_buffer.put(bytes).flip();

        bitmap_width  = 1024;
        bitmap_height = 1024;
        character_height = pixel_size;
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmap_width*bitmap_height);
        char_data = STBTTBakedChar.malloc(96);
        int result = STBTruetype.stbtt_BakeFontBitmap(ttf_buffer, pixel_size, bitmap, bitmap_width, bitmap_height, 32, char_data);
        if (result <= 0) {
            throw new RuntimeException("Failed to bake font bitmap.");
        }

        bitmap.rewind();
        texture_id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture_id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, bitmap_width, bitmap_height, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
    }

    public int GetTexture() { return texture_id; }
    public int GetCharacterHeight() { return character_height; }

    public void DrawText2D(String text, int x, int y) {
        DrawTextScaled2D(text, x, y, character_height, 1,1,1,1);
    }

    public void DrawText2D(String text, int x, int y, float r, float g, float b, float a) {
        DrawTextScaled2D(text, x, y, character_height, r, g, b, a);
    }

    public void DrawTextScaled2D(String text, int x, int y, float scale) {
        DrawTextScaled2D(text, x, y, scale, 1,1,1,1);
    }

    public void DrawTextScaled2D(String text, int x, int y, float scale, float r, float g, float b, float a) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] current_x = {x};
            float[] current_y = {y};
            float baseline = y;
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            glBindTexture(GL_TEXTURE_2D, texture_id);

            float font_scale = scale * (1.0f / character_height);

            glColor4f(r, g, b, a);
            glBegin(GL_TRIANGLES);
            for (int index = 0; index < text.length(); index++) {
                char codepoint = text.charAt(index);
                if (codepoint < 32 || codepoint >= 128) continue;

                STBTruetype.stbtt_GetBakedQuad(char_data, bitmap_width, bitmap_height, (codepoint-32), 
                                               current_x, current_y, q, true);

                float x0 = q.x0() * font_scale;
                float y0 = (baseline + (baseline - q.y0())) * font_scale;
                float x1 = q.x1() * font_scale;
                float y1 = (baseline + (baseline - q.y1())) * font_scale;

                float u0 = q.s0();
                float v0 = q.t0();
                float u1 = q.s1();
                float v1 = q.t1();
                
                glTexCoord2f(u0, v0);
                glVertex3f(x0, y0, 0);
                glTexCoord2f(u1, v0);
                glVertex3f(x1, y0, 0);
                glTexCoord2f(u1, v1);
                glVertex3f(x1, y1, 0);

                glTexCoord2f(u0, v0);
                glVertex3f(x0, y0, 0);
                glTexCoord2f(u1, v1);
                glVertex3f(x1, y1, 0);
                glTexCoord2f(u0, v1);
                glVertex3f(x0, y1, 0);
            }
            glEnd();
        }
    }

    public void DrawText3D(String text, float scale, float r, float g, float b, float a) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] current_x = {0};
            float[] current_y = {0};
            float baseline = 0;
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            glBindTexture(GL_TEXTURE_2D, texture_id);

            float font_scale = scale * (1.0f / character_height);

            glColor4f(r, g, b, a);
            glBegin(GL_TRIANGLES);
            for (int index = 0; index < text.length(); index++) {
                char codepoint = text.charAt(index);
                if (codepoint < 32 || codepoint >= 128) continue;

                STBTruetype.stbtt_GetBakedQuad(char_data, bitmap_width, bitmap_height, (codepoint-32), 
                                               current_x, current_y, q, true);

                float x0 = q.x0() * font_scale;
                float y0 = (baseline + (baseline - q.y0())) * font_scale;
                float x1 = q.x1() * font_scale;
                float y1 = (baseline + (baseline - q.y1())) * font_scale;

                float u0 = q.s0();
                float v0 = q.t0();
                float u1 = q.s1();
                float v1 = q.t1();
                
                glTexCoord2f(u0, v0);
                glVertex3f(x0, y0, 0);
                glTexCoord2f(u1, v0);
                glVertex3f(x1, y0, 0);
                glTexCoord2f(u1, v1);
                glVertex3f(x1, y1, 0);

                glTexCoord2f(u0, v0);
                glVertex3f(x0, y0, 0);
                glTexCoord2f(u1, v1);
                glVertex3f(x1, y1, 0);
                glTexCoord2f(u0, v1);
                glVertex3f(x0, y1, 0);
            }
            glEnd();
        }
    }

    public void Finish() {
        char_data.free();
        glDeleteTextures(texture_id);
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

    private static int pack_argb(ByteBuffer data, int x, int y, int width) {
        int index = (y * width + x) * 4;
        int r = data.get(index+0) & 0xFF;
        int g = data.get(index+1) & 0xFF;
        int b = data.get(index+2) & 0xFF;
        int a = data.get(index+3) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void Build2DMipmaps(int target, int internal_format, int width, int height, int format, int type, ByteBuffer data) {
        int current_width  = width;
        int current_height = height;
        int level = 0;
        ByteBuffer current_data = data;
        while (true) {
            glTexImage2D(target, level, internal_format, current_width, current_height, 0, format, type, current_data);
            if ((current_width == 1) && (current_height == 1)) break;

            int new_width  = Math.max(1, current_width/2);
            int new_height = Math.max(1, current_height/2);
            // We enforce loading RGBA textures.
            ByteBuffer new_data = BufferUtils.createByteBuffer(new_width*new_height*4);

            current_data.rewind();
            for (int y = 0; y < new_height; y++) {
                for (int x = 0; x < new_width; x++) {
                    int x0 = Math.min(x*2,     current_width-1);
                    int x1 = Math.min(x*2 + 1, current_width-1);
                    int y0 = Math.min(y*2,     current_height-1);
                    int y1 = Math.min(y*2 + 1, current_height-1);

                    // Sample pixels.
                    int p00 = pack_argb(current_data, x0, y0, current_width);
                    int p10 = pack_argb(current_data, x1, y0, current_width);
                    int p11 = pack_argb(current_data, x1, y1, current_width);
                    int p01 = pack_argb(current_data, x0, y1, current_width);

                    // Average 4 pixels channels.
                    byte r = (byte)((((p00 >> 16) & 0xFF) + ((p10 >> 16) & 0xFF) + ((p01 >> 16) & 0xFF) + ((p11 >> 16) & 0xFF)) / 4);
                    byte g = (byte)((((p00 >> 8) & 0xFF) + ((p10 >> 8) & 0xFF) + ((p01 >> 8) & 0xFF) + ((p11 >> 8) & 0xFF)) / 4);
                    byte b = (byte)((((p00) & 0xFF) + ((p10) & 0xFF) + ((p01) & 0xFF) + ((p11) & 0xFF)) / 4);
                    byte a = (byte)(((p00 >>> 24) + (p10 >>> 24) + (p01 >>> 24) + (p11 >>> 24)) / 4);

                    new_data.put(r);
                    new_data.put(g);
                    new_data.put(b);
                    new_data.put(a);
                }
            }
            new_data.flip();

            current_data   = new_data;
            current_width  = new_width;
            current_height = new_height;
            level++;
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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR_MIPMAP_LINEAR : gl_filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, gl_filter);
        
            // Set texture wrapping.
            gl_wrap = wrap ? GL_REPEAT : GL_CLAMP;
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, gl_wrap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, gl_wrap);

            // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
            Build2DMipmaps(GL_TEXTURE_2D, GL_RGBA, width[0], height[0], GL_RGBA, GL_UNSIGNED_BYTE, data);
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
            glTexCoord2f(0,0);
            glVertex3f(x0, y0, 0);
            glTexCoord2f(1,0);
            glVertex3f(x1, y0, 0);
            glTexCoord2f(1,1);
            glVertex3f(x1, y1, 0);
            
            glTexCoord2f(0,0);
            glVertex3f(x0, y0, 0);
            glTexCoord2f(1,1);
            glVertex3f(x1, y1, 0);
            glTexCoord2f(0,1);
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
