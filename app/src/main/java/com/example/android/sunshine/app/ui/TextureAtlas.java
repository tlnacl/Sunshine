package com.example.android.sunshine.app.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.utils.helper.ClusterHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tlnacl on 22/12/14.
 */
public class TextureAtlas {

    private final Context mContext;
    private final GL10 mGl;
    private final ArrayList<Integer> mTextureIds;
    private final ArrayList<Integer> mLoadingTextureIds;
    private final Handler mHandler;
    private final TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private static final HandlerThread sHandlerThread;

    static {
        sHandlerThread = new HandlerThread("ar_view_image_loading");
        sHandlerThread.start();
    }

    private final float textureCoordinates[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };
    private final FloatBuffer mTextureBuffer;
    private final Stack<Texture> mStack;
    private final Stack<Texture> mPreparedTextures;
    private final float mSmallTextSize;
    private final float mBigTextSize;
    private final int mClusterSize;
    private final View mMarkerView;
    private final ImageView mImageView;
    private final TextView mTextView;

    public TextureAtlas(Context context, GL10 gl) {
        mContext = context;
        mGl = gl;

        final Resources resources = mContext.getResources();
        mSmallTextSize = resources.getDimensionPixelSize(R.dimen.camera_view_normal_font);
        mBigTextSize = resources.getDimensionPixelSize(R.dimen.camera_view_big_font);
        mClusterSize = resources.getDimensionPixelSize(R.dimen.camera_view_cluster);

        mMarkerView = LayoutInflater.from(mContext).inflate(R.layout.layout_camera_marker, null);
        mImageView = (ImageView) mMarkerView.findViewById(R.id.image);
        mTextView = (TextView) mMarkerView.findViewById(R.id.text);

        mStack = new Stack<>();
        mPreparedTextures = new Stack<>();
        mTextureIds = new ArrayList<>();
        mLoadingTextureIds = new ArrayList<>();
        mHandler = new Handler(sHandlerThread.getLooper());

        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTextureBuffer = tbb.asFloatBuffer();
        mTextureBuffer.put(textureCoordinates);
        mTextureBuffer.position(0);
    }

    private void bindTexture(int textureId, Bitmap bitmap) {
        if (mTextureIds.contains(textureId)) {
            mGl.glDeleteTextures(1, new int[]{textureId}, 0);
            mTextureIds.remove((Integer) textureId);
        }

        mGl.glGenTextures(1, new int[]{textureId}, 0);
        mGl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
        // Scale up if the texture is smaller.
        mGl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        // Scale down if the mesh is smaller.
        mGl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        // Clamp to edge behaviour at edge of texture (repeats last pixel)
        mGl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        mGl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // Attach bitmap to current texture
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        mTextureIds.add(textureId);
        mLoadingTextureIds.remove((Integer) textureId);
    }

    public void loadQueuedTextures() {
        try {
            final Texture texture = mStack.pop();
            // We will draw in separated thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadTexture(texture);
                }
            });
        } catch (EmptyStackException e) {
        }

        try {
            final Texture texture = mPreparedTextures.pop();
            bindTexture(texture.mId, texture.mBitmap);
            texture.recycle();
        } catch (EmptyStackException e) {
        }
    }

    private void loadTexture(Texture texture) {
        // Here we gonna draw
        if(texture.mText.contains(" ")) {
            mTextView.setTextSize(mSmallTextSize);
        } else {
            mTextView.setTextSize(mBigTextSize);
        }
        String text = texture.getText().replaceAll("\\s+", "\n");

        texture.setBitmap(drawMarker(texture.mImgRes, text));
        // Jo draw ur text on bitmap here
        mPreparedTextures.push(texture);
    }

    public Bitmap drawMarker(int imgRes, String text) {
        try {
            mMarkerView.setDrawingCacheEnabled(true);

            mImageView.setImageResource(imgRes);
            mTextView.setText(text);

            //"setLayoutParams" is needed just for S3 and some Jelly Beans device to avoid Relative Layout
            //from crash when measuring its children
            mMarkerView.setLayoutParams(new ViewGroup.LayoutParams(mClusterSize, mClusterSize));
            mMarkerView.measure(mClusterSize, mClusterSize);
            mMarkerView.layout(0, 0, mClusterSize, mClusterSize);
            mMarkerView.buildDrawingCache();
            return Bitmap.createBitmap(mMarkerView.getDrawingCache());
        } finally {
            mMarkerView.setDrawingCacheEnabled(false);
        }
    }

    public int getTextureId(ClusterHelper.ArCluster cluster) {
        int id = String
                .format(Locale.ENGLISH, "%1$d", cluster.getSize())
                .hashCode() * (-1);

        if(mTextureIds.contains(id)) {
            return id;
        }
        int resId;
        String text;
                resId = R.drawable.art_clear;
        //for test
                text = "Weather";

        if(!mLoadingTextureIds.contains(id)) {
            if (cluster.getSize() > 1) {
                text = String.valueOf(cluster.getSize());
            }

            Texture texture = new Texture(id, resId, text);
            mLoadingTextureIds.add(id);
            mStack.push(texture);
        }
        return resId;
    }

    private class Texture {
        private final String mText;
        private final int mImgRes;
        private int mId;
        private Bitmap mBitmap;

        private Texture(int id, int imgRes, String text) {
            mId = id;
            mImgRes = imgRes;
            mText = text;
        }

        public int getId() {
            return mId;
        }

        public int getImgRes() {
            return mImgRes;
        }

        public String getText() {
            return mText;
        }

        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public void recycle() {
            if(mBitmap!=null) mBitmap.recycle();
        }
    }
}
