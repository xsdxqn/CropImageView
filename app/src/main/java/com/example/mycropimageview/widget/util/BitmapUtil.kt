package com.epoint.app.widget.crop.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

/**
 * 作者：kk
 * 创建时间： 2022/3/15 14:55
 * 版本： [1.0, 2022/3/15]
 * 版权：
 * 描述： <描述>
 */
object BitmapUtil {

    /**
     * 将本地图片转成Bitmap
     *
     * @param path 已有图片的路径
     * @return
     */
    fun getBitmapFromPath(path: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bis = BufferedInputStream(FileInputStream(path))
            bitmap = BitmapFactory.decodeStream(bis)
            bis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 保存Bitmap图片到指定路径
     */
    fun saveBitmap(bm: Bitmap, filePathRoot: String?, fileName: String?) {
        val fRoot = File(filePathRoot)
        if (!fRoot.exists()) {
            fRoot.mkdirs()
        }
        val f = File(filePathRoot + "/" + fileName)
        if (f.exists()) {
            f.delete()
        }
        try {
            val out = FileOutputStream(f)
            bm.compress(Bitmap.CompressFormat.PNG, 70, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}