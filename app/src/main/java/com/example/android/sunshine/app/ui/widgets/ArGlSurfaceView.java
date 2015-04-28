package com.example.android.sunshine.app.ui.widgets;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;

import com.example.android.sunshine.app.ui.RadarViewObject;
import com.example.android.sunshine.app.ui.WeatherArInfo;
import com.example.android.sunshine.app.ui.WeatherViewObject;
import com.example.android.sunshine.app.utils.helper.MatrixGrabber;
import com.example.android.sunshine.app.utils.helper.MatrixTrackingGL;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tlnacl on 22/12/14.
 */
public class ArGlSurfaceView  extends GLSurfaceView {

    private final PropertiesRenderer mRenderer;
    private final MatrixGrabber mMatrixGrabber;
    private final WeatherViewObject mPropertiesView;
    private float[] mRotationMatrix;
    private final RadarViewObject mRadar;

    public ArGlSurfaceView(Context context) {
        this(context, null);
    }

    public ArGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mMatrixGrabber = new MatrixGrabber();
        mRenderer = new PropertiesRenderer();
        mRadar = new RadarViewObject(context);
        mPropertiesView = new WeatherViewObject(context, mMatrixGrabber);

        setRenderer(mRenderer);

        setGLWrapper(new GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }
        });
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        mRotationMatrix = rotationMatrix;
    }

    public void setItemSelectionListener(final WeatherViewObject.ItemSelectionListener listener) {
        mPropertiesView.setSelectionListener(listener);
    }

    public void setRotation(final float x, final float y, final float z, final float dec) {
        queueEvent(new Runnable() {
            public void run() {
                mRenderer.setRotation(x, y, z, dec);
            }
        });
    }

    public Double getAzimuthOfPoint(float[] point) {
        float[] ray = getViewRay(point);

        double azimuth = Math.toDegrees(Math.atan2(ray[2], ray[0])) + 90;
        if (azimuth > 180) {
            azimuth -= 360;
        }

        return azimuth;
    }

    public float[] getViewRay(float[] tap) {
        // view port
        int[] viewport = {0, 0, getWidth(), getHeight()};

        // far eye point
        float[] eye = new float[4];
        GLU.gluUnProject(tap[0], getHeight() - tap[1], 0.9f, mMatrixGrabber.mModelView, 0, mMatrixGrabber.mProjection, 0, viewport, 0, eye, 0);

        // fix
        if (eye[3] != 0) {
            eye[0] = eye[0] / eye[3];
            eye[1] = eye[1] / eye[3];
            eye[2] = eye[2] / eye[3];
        }

        // ray vector
        float[] ray = {eye[0], eye[1], eye[2], 0.0f};
        return ray;
    }

    public void setProperties(List<WeatherArInfo> weatherArInfo) {
        mRadar.setProperties(weatherArInfo);
        mPropertiesView.setWeathers(weatherArInfo);
    }

    class PropertiesRenderer implements Renderer {

        private int width;
        private int height;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // SETTINGS
            // Set the background color to transparent.
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // change to 0.4 in future

            gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
            gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
            //Really Nice Perspective Calculations
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

            // DRAWING SETUP
            // NOTES: As we are always drawing with textures and viewing our
            // elements from the same side all the time we can leave all these
            // settings on the whole time
            // Enable face culling.
            gl.glEnable(GL10.GL_CULL_FACE);
            // What faces to remove with the face culling.
            gl.glCullFace(GL10.GL_BACK);
            // Tell OpenGL to enable the use of UV coordinates.
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            // Blending on
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            //GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);

            gl.glEnable(GL10.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL10.GL_GREATER, 0.2f);

            mRadar.loadGLTexture(gl, getContext());
            mPropertiesView.loadGLTexture(gl, getContext());
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // Stores width and height
            this.width = width;
            this.height = height;
            // Sets the current view port to the new size.
            gl.glViewport(0, 0, width, height);
            // Select the projection matrix
            gl.glMatrixMode(GL10.GL_PROJECTION);
            // Reset the projection matrix
            gl.glLoadIdentity();
            // Orthographic mode for 2d
            gl.glOrthof(0, width, -height, 0, -1, 8);
            // Select the modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // Reset the modelview matrix
            gl.glLoadIdentity();

            mMatrixGrabber.setViewPort(width, height);
            mRadar.setViewPort(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // clear Screen and Depth Buffer
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            // Replace the current matrix with the identity matrix
            gl.glLoadIdentity();

            gluPerspective(gl);
            if (mRotationMatrix != null) {
                gl.glMultMatrixf(mRotationMatrix, 0);
                gl.glRotatef(90f, 1.0f, 0.0f, 0.0f);
            }
            gl.glClearDepthf(1.0f);
            mMatrixGrabber.getCurrentState(gl);
            mPropertiesView.draw(gl);

            gluOrtho2D(gl);
            // Draw in 2D
            // Rotate world by 180 around x axis so positive y is down (like canvas)
            gl.glRotatef(-180, 1, 0, 0);
            mRadar.draw(gl);
        }

        public void setRotation(float r, float g, float b, float d) {
            Double xDegree = getAzimuthOfPoint(new float[]{width / 2, height / 2});

            x = r;
            if (xDegree != null) {
                x = (float) Math.toRadians(xDegree);
            }

            y = g;
            z = b;
            dec = d;

            angle = ((float) Math.toDegrees(x) + 360f) % 360f;

            mRadar.setAngle(angle);
        }

        private float x;
        private float y;
        private float z;
        private float dec;
        private float angle;

        /**
         * Sets the projection to the ortho matrix
         */
        public void gluOrtho2D(GL10 gl) {
            // Sets the current view port to the new size.
            gl.glViewport(0, 0, width, height);
            // Select the projection matrix
            gl.glMatrixMode(GL10.GL_PROJECTION);
            // Reset the projection matrix
            gl.glLoadIdentity();
            // Orthographic mode for 2d
            gl.glOrthof(0, width, -height, 0, -1, 8);
            // Select the modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // Reset the modelview matrix
            gl.glLoadIdentity();
        }

        /**
         * Sets the projection to the perspective matrix
         */
        public void gluPerspective(GL10 gl) {
            gl.glViewport(0, 0, width, height);     //Reset The Current Viewport
            gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
            gl.glLoadIdentity();                    //Reset The Projection Matrix
            //Calculate The Aspect Ratio Of The Window
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Modelview Matrix
            gl.glLoadIdentity();                    //Reset The Modelview Matrix
        }
    }
}
