package com.tl.sunshine.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;

import com.tl.sunshine.R;
import com.tl.sunshine.utils.helper.ClusterHelper;
import com.tl.sunshine.utils.helper.GL10Helper;
import com.tl.sunshine.utils.helper.MatrixGrabber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tlnacl on 22/12/14.
 */
public class WeatherViewObject {

    private static final int ITEM_SELECTION_DELAY = 1000;

    private static final String TAG = WeatherViewObject.class.getName();

    private final MatrixGrabber mMatrixGrabber;
    private final Context mContext;
    private final FloatBuffer mTextureBuffer;
    private final float mTapRadius;
    private final Handler mHandler;

    private FloatBuffer vertexBuffer;    // buffer holding the vertices
    private float vertices[] = {
            -0.35f, -0.35f, 0.0f,    // V1 - first vertex (x,y,z)
            0.35f, -0.35f, 0.0f,        // V2 - second vertex
            -0.35f, 0.35f, 0.0f,    // V3 - third vertex
            0.35f, 0.35f, 0.0f        // V4 - forth
    };
    private final float textureCoordinates[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    private final List<ClusterHelper.ArCluster> mProperties = new ArrayList<>();
    private CreateClustersTask mTask;
    private ClusterHelper.ArCluster mSelectedCluster;
    private TextureAtlas mTextureAtlas;
    private ItemSelectionListener mSelectionListener;

    //Used to verify if the selection is still valid when the handler is going to handle over the event
    //to the callback. It's needed to avoid the user go too fast across the clusters and the properties pop
    //up in this moment.
    private long mSelectionElapsed;
    private long mUnselectedElapsed;
    private boolean mShouldNotifyUnselected;
//    private int mOutsideBouncing;

    public WeatherViewObject(final Context ctx, final MatrixGrabber matrixGrabber) {
        mContext = ctx;
        mMatrixGrabber = matrixGrabber;
        mHandler = new Handler(Looper.getMainLooper());

        mTapRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, ctx.getResources().getDisplayMetrics());

        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());

        // allocates the memory from the byte buffer
        vertexBuffer = vertexByteBuffer.asFloatBuffer();

        // fill the vertexBuffer with the vertices
        vertexBuffer.put(vertices);

        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTextureBuffer = tbb.asFloatBuffer();
        mTextureBuffer.put(textureCoordinates);
        mTextureBuffer.position(0);
    }

    public void setSelectionListener(ItemSelectionListener selectionListener) {
        mSelectionListener = selectionListener;
    }

    public void loadGLTexture(GL10 gl, Context context) {
        addTexture(gl, R.drawable.art_clear);

        mTextureAtlas = new TextureAtlas(context, gl);
    }

    private void addTexture(GL10 gl, int resId) {
        gl.glGenTextures(1, new int[]{resId}, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, resId);
        // Scale up if the texture is smaller.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        // Scale down if the mesh is smaller.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        // Clamp to edge behaviour at edge of texture (repeats last pixel)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // Attach bitmap to current texture
        GLUtils.texImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                BitmapFactory.decodeResource(mContext.getResources(), resId),
                0
        );
    }

    public void draw(GL10 gl) {
        if (mProperties.size() == 0) return;

        mTextureAtlas.loadQueuedTextures();

        // Index buffer (which vertices go together to make the
        // elements)
        ByteBuffer ibb = ByteBuffer.allocateDirect(6 * 2);
        ibb.order(ByteOrder.nativeOrder());
        ShortBuffer indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(new short[]{0, 1, 2, 1, 2, 3});
        indexBuffer.position(0);

        synchronized (mProperties) {
            for (ClusterHelper.ArCluster cluster : mProperties) {
                pointCheck(cluster);
                float[] vector = GL10Helper.convertAnglesToVector(Math.toRadians(cluster.getAzimuth()), 0);
                float[] winPosition = GL10Helper.convert3dTo2d(mMatrixGrabber, vector);
                cluster.setWindowPosition(winPosition);

                float delta_angle = cluster.getAzimuth();// + mCorrection;
                gl.glPushMatrix();
                gl.glRotatef(360 - delta_angle, 0.0f, 1.0f, 0.0f);
                gl.glTranslatef(0.0f, 0.0f, -5.0f - cluster.getGlDistance());
                //(isSelected ? -1 : property.getGlDistance()));

                // Enabled the vertices buffer for writing and to be used during
                // rendering.
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

                gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

                // DRAW COMMAND
                // Tell OpenGL where our texture is located.
                int textureId = mTextureAtlas.getTextureId(cluster);
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
                // Telling OpenGL where our textureCoordinates are.
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);

                // Specifies the location and data format of the array of vertex
                // coordinates to use when rendering.
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                // Draw elements command using indices so it knows which
                // vertices go together to form each element
                // Draw the vertices as triangle strip
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

                gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

                gl.glPopMatrix();
            }
        }
    }

    public void pointCheck(ClusterHelper.ArCluster cluster) {
        if (mMatrixGrabber == null) return;

        float x = mMatrixGrabber.mViewPort[2] / 2;
        float y = mMatrixGrabber.mViewPort[3] / 2;

        if (cluster.isTouched(x, y, mTapRadius)) {
            Log.d(TAG, "<<< INSIDE");
            if (mSelectedCluster != null) {
                setSelection(mSelectedCluster, false);
            }

            if(cluster != mSelectedCluster) {
                mSelectionElapsed = SystemClock.elapsedRealtime();
                notifySelectionListener(cluster);
            }

            mUnselectedElapsed = SystemClock.elapsedRealtime();
            mSelectedCluster = cluster;
            setSelection(mSelectedCluster, true);
        } else {
            Log.d(TAG, ">>> OUTSIDE");
            synchronized (this) {
                if(SystemClock.elapsedRealtime() - mUnselectedElapsed >= ITEM_SELECTION_DELAY) {
                    if(mSelectionListener != null && mShouldNotifyUnselected) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSelectionListener.onNothingSelected();
                            }
                        });
                        setSelection(mSelectedCluster, false);
                        mSelectedCluster = null;
                        mShouldNotifyUnselected = false;
                    }
                }
            }
        }
    }

    private void setSelection(final ClusterHelper.ArCluster cluster, final boolean focused) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (cluster == null) return;

                cluster.setSelected(focused);
            }
        });
    }

    private void notifySelectionListener(final ClusterHelper.ArCluster cluster) {
        if(mSelectionListener != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (WeatherViewObject.this) {
                        if (SystemClock.elapsedRealtime() - mSelectionElapsed >= ITEM_SELECTION_DELAY) {
                            final List<WeatherArInfo> propertiesOpenGL = cluster.getWeatheres();
                            final WeatherUI [] weatherUIs = new WeatherUI[propertiesOpenGL.size()];
                            for (int i=0; i<propertiesOpenGL.size(); i++) {
                                weatherUIs[i] = propertiesOpenGL.get(i).getWeatherUI();
                            }

                            mSelectionListener.onSelection(weatherUIs);
                            mShouldNotifyUnselected = true;
                        }
                    }
                }
            }, ITEM_SELECTION_DELAY);
        }
    }

    public void setWeathers(final List<WeatherArInfo> weathers) {
        if(mTask != null && !mTask.isCancelled()) {
            mTask.cancel(true);
        }
        mTask = new CreateClustersTask(weathers);
        mTask.execute();
    }

    private class CreateClustersTask extends AsyncTask<Void, Void, List<ClusterHelper.ArCluster>> {

        private final List<WeatherArInfo> mData;

        public CreateClustersTask(List<WeatherArInfo> weathers) {
            mData = weathers;
        }

        @Override
        protected List<ClusterHelper.ArCluster> doInBackground(Void... args) {
            return ClusterHelper.prepareClusters(mData);
        }

        @Override
        protected void onPostExecute(List<ClusterHelper.ArCluster> arClusters) {
            if(!isCancelled()) {
                synchronized (mProperties) {
                    mProperties.clear();
                    mProperties.addAll(arClusters);
                }
            }
        }
    }

    public interface ItemSelectionListener {
        void onSelection(final WeatherUI... properties);
        void onNothingSelected();
    }
}
