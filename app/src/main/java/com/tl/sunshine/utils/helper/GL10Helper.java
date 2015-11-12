package com.tl.sunshine.utils.helper;

import android.opengl.GLU;
import android.opengl.Matrix;

/**
 * Created by tlnacl on 22/12/14.
 */
public class GL10Helper {

    private static final float[] sInitialMatrix = new float[]{
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
    };

    /**
     * Conversation point which represented by angles to Vector
     *
     * @param azimuth Azimuth in radiance
     * @param zenith  Zenith in radiance
     * @return 4 dimension vector
     */
    public static float[] convertAnglesToVector(double azimuth, double zenith) {
        float[] rotationMatrix = sInitialMatrix.clone();

        float[] baseVector = new float[]{0f, 0f, -10f, 0f};
        float[] resultVector = new float[]{0f, 0f, 0f, 0f};

        float delta_angle = (float) Math.toDegrees(azimuth);
        Matrix.rotateM(rotationMatrix, 0, 360 - delta_angle, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(rotationMatrix, 0, (float) Math.toDegrees(zenith), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMV(resultVector, 0, rotationMatrix, 0, baseVector, 0);

        return resultVector;
    }

    public static float[] convert3dTo2d(MatrixGrabber grabber, float[] vector) {
        float[] modelMatrix = grabber.mModelView;
        float[] projectionMatrix = grabber.mProjection;
        int[] viewPort = grabber.mViewPort;

        float[] winPoint = new float[3];

        GLU.gluProject(vector[0], vector[1], vector[2],
                modelMatrix, 0,
                projectionMatrix, 0,
                viewPort, 0,
                winPoint, 0);

        return winPoint;
    }

}
