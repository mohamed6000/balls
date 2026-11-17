package com.example;

public class MathHelper {
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

    public static boolean CheckCollision(float px, float pz, float radius, 
                                         Level level) {
        int left   = (int)Math.floor(px - radius - 0.5f);
        int right  = (int)Math.floor(px + radius + 0.5f);
        int top    = (int)Math.floor(pz - radius - 0.5f);
        int bottom = (int)Math.floor(pz + radius + 0.5f);

        left   = Math.max(left, 0);
        right  = Math.min(right, level.getWidth()-1);
        top    = Math.max(top, 0);
        bottom = Math.min(bottom, level.getHeight()-1);
        
        int level_data[] = level.getMap();
        for (int z = top; z <= bottom; z++) {
            for (int x = left; x <= right; x++) {
                int tile = level_data[z * level.getWidth() + x];
                if (tile != 0) {
                    float closest_x = Math.max(x-0.5f, Math.min(px, x+0.5f));
                    float closest_z = Math.max(z-0.5f, Math.min(pz, z+0.5f));
                    float dist_x = px - closest_x;
                    float dist_z = pz - closest_z;
                    if ((dist_x*dist_x + dist_z*dist_z) < (radius*radius)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static float DistanceSquared(float[] p0, float[] p1) {
        return (p1[0]-p0[0])*(p1[0]-p0[0]) + (p1[1]-p0[1])*(p1[1]-p0[1]) + (p1[2]-p0[2])*(p1[2]-p0[2]);
    }

    public static float Distance(float[] p0, float[] p1) {
        return (float)Math.sqrt(DistanceSquared(p0, p1));
    }
}
