package com.example.mycropimageview

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.epoint.app.widget.crop.util.BitmapUtil.getBitmapFromPath
import com.example.mycropimageview.databinding.ActivityCropBinding

import android.annotation.SuppressLint

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.epoint.app.widget.crop.util.BitmapUtil
import com.example.mycropimageview.widget.PaintCropImageView
import com.example.mycropimageview.widget.SourceImageView
import com.example.mycropimageview.widget.util.Matrix3
import com.example.mycropimageview.widget.util.PermissionUtil
import java.lang.IllegalArgumentException

import java.io.InputStream


/**
 * 作者：kk
 * 创建时间： 2022/3/10 17:59
 * 版本： [1.0, 2022/3/10]
 * 版权：
 * 描述： <图片裁剪Activity--AI数钢筋功能会用到>
 */
class CropActivity : AppCompatActivity() {

    var binding: ActivityCropBinding? = null

    lateinit var sourceBitmap: Bitmap

    lateinit var resultBitmap: Bitmap

    /**
     * 是否为框选模式，true是，false为画圈模式
     */
    var isRectCrop = true

    lateinit var fileRoot: String

    val fileName: String = "111111.png"

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!PermissionUtil.checkPermissionAllGranted(this, permissions)) {
            PermissionUtil.startRequestPermissions(
                this, permissions, PermissionUtil.REQUESTCODE_PERMISSION_STORAGE
            )
        }

        fileRoot = application.getExternalFilesDir(null)?.getAbsolutePath().toString()

        initView()
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.O)
    fun initView() {
        //代码为测试代码，所以直接用的资源文件中的图片，实际可以通过本地图片路径获取Bitmap
        val inputStream: InputStream = resources.openRawResource(R.drawable.img_test1)
        sourceBitmap = BitmapFactory.decodeStream(inputStream)
        binding!!.ivSource.setImageBitmap(sourceBitmap)
        // 禁用缩放
//        binding!!.ivSource.setScaleEnabled(false)
        // 设置完与屏幕匹配的尺寸  确保变换矩阵设置生效后才设置裁剪区域
        binding!!.ivSource.post {
            val r = binding!!.ivSource.bitmapRect
            binding!!.ivRectcrop.cropRect = r
        }
        binding!!.rvGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rb_rect -> {
                    //框选
                    isRectCrop = true
                    binding!!.ivRectcrop.visibility = View.VISIBLE
                    binding!!.ivPaintcrop.visibility = View.GONE
                }
                R.id.rb_round -> {
                    //画圈
                    isRectCrop = false
                    binding!!.ivRectcrop.visibility = View.GONE
                    binding!!.ivPaintcrop.visibility = View.VISIBLE
                }
            }
        })

        binding!!.btnOk.setOnClickListener({

            Log.i("kkk", "fileRoot=" + fileRoot)
            Log.i("kkk", "fileName=" + fileName)
            if (isRectCrop) {
                //框选
                Thread(Runnable {
                    // 剪切区域矩形
                    val cropRect: RectF = binding!!.ivRectcrop.getCropRect()
                    val touchMatrix: Matrix = binding!!.ivSource.getImageViewMatrix()
                    val data = FloatArray(9)
                    // 底部图片变化记录矩阵原始数据
                    touchMatrix.getValues(data)
                    // 辅助矩阵计算类
                    val cal = Matrix3(data)
                    // 计算逆矩阵
                    val inverseMatrix = cal.inverseMatrix()
                    val m = Matrix()
                    m.setValues(inverseMatrix.values)
                    //变化剪切矩形
                    m.mapRect(cropRect)
                    resultBitmap = Bitmap.createBitmap(
                        sourceBitmap,
                        cropRect.left.toInt(),
                        cropRect.top.toInt(),
                        cropRect.width().toInt(),
                        cropRect.height().toInt()
                    )
                    BitmapUtil.saveBitmap(
                        resultBitmap, fileRoot, fileName
                    )
                }).start()
            } else {
                //画圈
                PaintCropImageView.saveBitmap(
                    sourceBitmap, fileRoot, fileName
                )
            }
        })
    }
}