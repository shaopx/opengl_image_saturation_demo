package com.spx.glshaderimage

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val myRenderer = MyRenderer()
        myRenderer.mBitmap = BitmapFactory.decodeResource(resources, R.drawable.image1)

        //顶点着色器脚本R.raw.vertex
        myRenderer.vertexShaderSource = ShaderUtil.loadFromInputStream(resources.openRawResource(R.raw.vertex))
        //片元着色器脚本:R.raw.fragment
        myRenderer.fragmentShaderSource = ShaderUtil.loadFromInputStream(resources.openRawResource(R.raw.fragment))

        glsurfaceview.setEGLContextClientVersion(3) // 一定要设置
        glsurfaceview.setRenderer(myRenderer)
        glsurfaceview.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glsurfaceview.requestRender()

        /**
         * 这是一个滑动条百分比的实时处理, 也就是页面上饱和度的值设置给render对象,
         * 由于我们是自动更新页面(RENDERMODE_CONTINUOUSLY), 所以这个饱和度值会自动体现在图片上
         */
        saturation_seakbar.max = 200
        saturation_seakbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                myRenderer.saturationF = progress * 1f / 100f
                saturation_tv.text = "饱和度:${progress}%"
                Log.d(TAG, "onProgressChanged  progress:${myRenderer.saturationF}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
