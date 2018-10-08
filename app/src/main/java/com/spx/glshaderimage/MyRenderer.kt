package com.spx.glshaderimage

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 渲染逻辑都在这
 *
 */
class MyRenderer : GLSurfaceView.Renderer {

    var TAG ="MyRenderer"

    private var program: Int = 0
    private var vPosition: Int = 0
    private var vCoordinate: Int = 0
    private var vTexture: Int = 0
    private var saturationIndex: Int = 0
    private var textureId: Int = 0

    private val bPos: FloatBuffer
    private val bCoord: FloatBuffer

    private val sPos = floatArrayOf(-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f)

    private val sCoord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)
    var saturationF = 0.5f
    init {
        val bb = ByteBuffer.allocateDirect(sPos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        bPos = bb.asFloatBuffer()
        bPos.put(sPos)
        bPos.position(0)
        val cc = ByteBuffer.allocateDirect(sCoord.size * 4)
        cc.order(ByteOrder.nativeOrder())
        bCoord = cc.asFloatBuffer()
        bCoord.put(sCoord)
        bCoord.position(0)
    }


    lateinit var mBitmap: Bitmap
    lateinit var vertexShaderSource: String
    lateinit var fragmentShaderSource: String


    /**
     */
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {

        // Create a minimum supported OpenGL ES context, then check:
        gl10.glGetString(GL10.GL_VERSION).also {
            Log.w(TAG, "onSurfaceCreated Version: $it")
        }

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_TEXTURE_2D)
        // 初始化着色器
        // 基于顶点着色器与片元着色器创建程序
        program = ShaderUtil.createProgram(vertexShaderSource, fragmentShaderSource)
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES30.glGetAttribLocation(program, "vPosition")
        vCoordinate = GLES30.glGetAttribLocation(program, "vCoordinate")
        vTexture = GLES30.glGetUniformLocation(program, "vTexture")
        saturationIndex = GLES30.glGetUniformLocation(program, "saturation")


        // 使用某套shader程序
        GLES30.glUseProgram(program)

        // 允许顶点位置数据数组
        GLES30.glEnableVertexAttribArray(vPosition)
        GLES30.glEnableVertexAttribArray(saturationIndex)
        GLES30.glEnableVertexAttribArray(vCoordinate)
        GLES30.glUniform1i(vTexture, 0)

        textureId = createTexture()

    }

    /**
     * 当GLSurfaceView中的Surface被改变的时候回调此方法(一般是大小变化)
     *
     * @param gl10   同onSurfaceCreated()
     * @param width  Surface的宽度
     * @param height Surface的高度
     */
    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        // 设置绘图的窗口(可以理解成在画布上划出一块区域来画图)
        GLES30.glViewport(0, 0, width, height)
    }

    /**
     * 当Surface需要绘制的时候回调此方法
     * 根据GLSurfaceView.setRenderMode()设置的渲染模式不同回调的策略也不同：
     * GLSurfaceView.RENDERMODE_CONTINUOUSLY : 固定一秒回调60次(60fps)
     * GLSurfaceView.RENDERMODE_WHEN_DIRTY   : 当调用GLSurfaceView.requestRender()之后回调一次
     *
     * @param gl10 同onSurfaceCreated()
     */
    override fun onDrawFrame(gl10: GL10) {

        // 清屏
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)


        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)


        GLES30.glVertexAttribPointer(vPosition, 2, GLES30.GL_FLOAT, false, 0, bPos)
        GLES30.glVertexAttribPointer(vCoordinate, 2, GLES30.GL_FLOAT, false, 0, bCoord)
        GLES30.glUniform1f(saturationIndex, saturationF)

        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }


    private fun createTexture(): Int {
        val texture = IntArray(1)
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理
            GLES30.glGenTextures(1, texture, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0)
            return texture[0]
        }
        return 0
    }
}

