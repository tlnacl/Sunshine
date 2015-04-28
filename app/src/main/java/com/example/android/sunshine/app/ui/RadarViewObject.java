package com.example.android.sunshine.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLUtils;
import android.util.TypedValue;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.utils.helper.SpriteData;

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
public class RadarViewObject {


    private final Context mContext;
    private final Bitmap mRadarBackground;
    private final Bitmap mRadarPointNormal;
    private final int mImageSize;
    private final int mSpace;
    private final int mRadius;
    private final Paint mPaint;
    private int mViewWidth;
    private int mViewHeight;
    private SpriteData mSprite;
    private float mAngle;
    private final List<WeatherArInfo> mProperties = new ArrayList<>();

    public RadarViewObject(Context ctx) {
        mContext = ctx;

        mRadarBackground = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.radar_base);
        mRadarPointNormal = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.radar_house);

        mImageSize = mRadarBackground.getHeight();

        mSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, mContext.getResources().getDisplayMetrics());
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22f, mContext.getResources().getDisplayMetrics());

        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setAntiAlias(true);
    }

    public void setViewPort(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void loadGLTexture(GL10 gl, Context context) {
        mSprite = new SpriteData(mImageSize, mImageSize);
    }

    public void draw(GL10 gl) {
        mSprite.addSprite(
                new Rect(0, 0, mImageSize, mImageSize), // src
                new Rect(
                        mViewWidth - mSprite.getWidth() - mSpace,
                        mSpace,
                        mViewWidth - mSpace,
                        mSpace + mSprite.getHeight()
                ), // dst
                0); // angle

        createTexture(gl);

        // CONVERT INTO ARRAY
        float[] vertices = mSprite.getVertices();
        short[] indices = mSprite.getIndices();
        float[] textureCoords = mSprite.getTextureCoords();

        // Vertex buffer (position information of every draw command)
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // Index buffer (which vertices go together to make the
        // elements)
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        ShortBuffer indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        // How to paste the texture over each element so that the right
        // image is shown
        ByteBuffer tbb = ByteBuffer
                .allocateDirect(textureCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        FloatBuffer textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // DRAW COMMAND
        // Tell OpenGL where our texture is located.
        gl.glBindTexture(GL10.GL_TEXTURE_2D, R.drawable.radar_base);
        // Telling OpenGL where our textureCoords are.
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        // Specifies the location and data format of the array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        // Draw elements command using indices so it knows which
        // vertices go together to form each element
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
                GL10.GL_UNSIGNED_SHORT, indexBuffer);

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        mSprite.clear();
    }

    private void createTexture(GL10 gl) {
        Bitmap bitmap = drawRadar();

        gl.glDeleteTextures(1, new int[]{R.drawable.radar_base}, 0);

        gl.glGenTextures(1, new int[]{R.drawable.radar_base}, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, R.drawable.radar_base);
        // SETTINGS
        // Scale up if the texture is smaller.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        // Scale down if the mesh is smaller.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        // Clamp to edge behaviour at edge of texture (repeats last pixel)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // Attach bitmap to current texture
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }

    private Bitmap drawRadar() {
        final double halfImage = mImageSize / 2.0;
        Bitmap bitmap = Bitmap.createBitmap(mImageSize, mImageSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mRadarBackground, 0f, 0f, mPaint);

        final float angle = 360 - mAngle;
        canvas.translate((int) halfImage, (int) halfImage);

        Rect srcRect = new Rect(0, 0, mRadarPointNormal.getWidth(), mRadarPointNormal.getHeight());
        synchronized (mProperties) {
            for (WeatherArInfo propertyArInfo : mProperties) {
                drawPropertyOnRadar(canvas, angle, srcRect, propertyArInfo);
            }
        }

        return bitmap;
    }

    private void drawPropertyOnRadar(Canvas canvas, float angle, Rect srcRect, WeatherArInfo propertyArInfo) {
        final float azimuth = propertyArInfo.getAzimuth();
        final float[] distanceArray = propertyArInfo.getDistanceArray();
        // distance in pixels
        final float distance = mRadius * (distanceArray[0] / WeatherArInfo.MAX_DISTANCE);
        final double bearing = Math.toRadians(azimuth + angle);

        final float x = (float) (distance * Math.sin(bearing));
        final float y = (float) (distance * Math.cos(bearing));

        Rect dstRect = new Rect(srcRect);
        dstRect.offsetTo((int) x - srcRect.width() / 2, (int) -y - srcRect.height() / 2);
        canvas.drawBitmap(mRadarPointNormal, srcRect, dstRect, mPaint);
    }

    public void setProperties(List<WeatherArInfo> properties) {
        synchronized (mProperties) {
            mProperties.clear();
            mProperties.addAll(properties);
        }
    }
}
