package com.spx.glshaderimage


import android.content.res.Resources
import android.opengl.GLES30
import android.util.Log

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.charset.Charset

/**
 * 加载顶点Shader与片元Shader的工具类
 */

object ShaderUtil {
    var TAG ="ShaderUtil"
    // 顶点着色器的脚本
    val verticesShader = ("attribute vec2 vPosition;            \n" // 顶点位置属性vPosition

            + "void main(){                         \n"
            + "   gl_Position = vec4(vPosition,0,1);\n" // 确定顶点位置

            + "}")

    // 片元着色器的脚本
    val fragmentShader = ("precision mediump float;         \n" // 声明float类型的精度为中等(精度越高越耗资源)

            + "uniform vec4 uColor;             \n" // uniform的属性uColor

            + "void main(){                     \n"
            + "   gl_FragColor = uColor;        \n" // 给此片元的填充色

            + "}")

    /**
     * 获取图形的顶点
     * 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
     * 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
     *
     * @return 顶点Buffer
     */
    // 创建顶点坐标数据缓冲
    // vertices.length*4是因为一个float占四个字节
    //设置字节顺序
    //转换为Float型缓冲
    //向缓冲区中放入顶点坐标数据
    //设置缓冲区起始位置
    val vertices: FloatBuffer
        get() {
            val vertices = floatArrayOf(0.0f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f)
            val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            val vertexBuf = vbb.asFloatBuffer()
            vertexBuf.put(vertices)
            vertexBuf.position(0)

            return vertexBuf
        }

    /**
     * 加载制定shader的方法
     * @param shaderType shader的类型  GLES30.GL_VERTEX_SHADER   GLES30.GL_FRAGMENT_SHADER
     * @param source shader的脚本字符串
     * @return 着色器id
     */
    fun loadShader(shaderType: Int, source: String): Int {
        // 创建一个新shader
        var shader = GLES30.glCreateShader(shaderType)
        // 若创建成功则加载shader
        if (shader != 0) {
            //加载shader的源代码
            GLES30.glShaderSource(shader, source)
            //编译shader
            GLES30.glCompileShader(shader)
            //存放编译成功shader数量的数组
            val compiled = IntArray(1)
            //获取Shader的编译情况
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {//若编译失败则显示错误日志并删除此shader
                Log.e("ES20_ERROR", "Could not compile shader $shaderType:")
                Log.e("ES20_ERROR", GLES30.glGetShaderInfoLog(shader))
                GLES30.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }


    /**
     * 创建shader程序的方法
     */
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        Log.e(TAG, "createProgram ... ")
        //加载顶点着色器
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            Log.e(TAG, "loadShader fail! ... $vertexSource")
            checkGlError("loadShader")
            return 0
        }

        //加载片元着色器
        val pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            Log.e(TAG, "loadShader fail! ... $fragmentSource")
            checkGlError("loadShader")
            return 0
        }

//        checkGlError("xxxxx")

        //创建程序
        var program = GLES30.glCreateProgram()
        //若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {
            //向程序中加入顶点着色器
            Log.e(TAG, "vertexShader: $vertexSource")
            GLES30.glAttachShader(program, vertexShader)
//            checkGlError("glAttachShader")
            //向程序中加入片元着色器
            Log.e(TAG, "pixelShader: $fragmentSource")
            GLES30.glAttachShader(program, pixelShader)
//            checkGlError("glAttachShader")
            //链接程序
            GLES30.glLinkProgram(program)
            //存放链接成功program数量的数组
            val linkStatus = IntArray(1)
            //获取program的链接情况
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            //若链接失败则报错并删除程序
            if (linkStatus[0] != GLES30.GL_TRUE) {
                Log.e("ES20_ERROR", "Could not link program: ")
                Log.e("ES20_ERROR", GLES30.glGetProgramInfoLog(program))
                GLES30.glDeleteProgram(program)
                program = 0
            }
        } else {
            Log.e(TAG, "glCreateProgram fail! $program")
            checkGlError("glCreateProgram")
        }
        return program
    }

    //检查每一步操作是否有错误的方法
    fun checkGlError(op: String) {
        val error: Int  = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            Log.e("ES20_ERROR", "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

    //从sh脚本中加载shader内容的方法
    fun loadFromAssetsFile(fname: String, r: Resources): String? {
        var result: String? = null
        try {
            result = loadFromInputStream(r.assets.open(fname))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun loadFromInputStream(stream:InputStream): String {
        var result = ""
        try {
            val baos = ByteArrayOutputStream()
            var ch = stream.read()
            while (ch != -1) {
                baos.write(ch)
                ch = stream.read()
            }
            val buff = baos.toByteArray()
            baos.close()
            stream.close()
            result = String(buff, Charset.defaultCharset())
            result = result.replace("\\r\\n".toRegex(), "\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}
