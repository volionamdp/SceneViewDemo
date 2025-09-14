# SceneView Android --- Hướng Dẫn Sử Dụng (View XML)

Thư viện **SceneView** cho phép bạn thêm Scene 3D hoặc AR vào ứng dụng
Android thông qua View trong XML, không cần Jetpack Compose.\
Repo chính: [SceneView
Android](https://github.com/SceneView/sceneview-android)

------------------------------------------------------------------------

## 1. Cài đặt

Trong `build.gradle (app)` thêm:

``` gradle
dependencies {
    // SceneView cơ bản (Filament 3D)
    implementation("io.github.sceneview:sceneview:2.3.0")

}
```

Đảm bảo `minSdkVersion` ≥ 24 và `compileSdkVersion` ≥ 33.

------------------------------------------------------------------------

## 2. Thêm SceneView trong XML

Ví dụ `activity_main.xml`:

``` xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:sceneview="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <io.github.sceneview.SceneView
        android:id="@+id/sceneView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        sceneview:enableFrameTime="true"
        sceneview:enableStatistics="false"
        sceneview:enablePbr="true" />

</FrameLayout>
```


------------------------------------------------------------------------

## 3. Khởi tạo trong Activity / Fragment

``` kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)

        // Thiết lập ánh sáng chính
        sceneView.lightEstimation = true

        // Tạo node mô hình
        val modelNode = ModelNode(
            context = this,
            lifecycle = lifecycle,
            glbFileLocation = "models/damaged_helmet.glb",
            scaleToUnits = 1.0f
        )

        // Thêm node vào scene
        sceneView.addChild(modelNode)

        // Đặt camera xa hơn để thấy mô hình
        sceneView.camera.position = Position(z = 4.0f)

        // Thêm Skybox/HDRI environment
        sceneView.environment = Environment(context = this, lifecycle = lifecycle) {
            createHDREnvironment("environments/sky_2k.hdr")
        }
    }

    override fun onResume() {
        super.onResume()
        sceneView.onResume()
    }

    override fun onPause() {
        sceneView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        sceneView.destroy()
        super.onDestroy()
    }
}
```

------------------------------------------------------------------------

## 4. AR SceneView (Đặt mô hình vào mặt phẳng)

Ví dụ `ARSceneView` với tap để đặt mô hình:

``` kotlin
class ARActivity : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        arSceneView = findViewById(R.id.arSceneView)

        arSceneView.onTapAr = { hitResult ->
            val anchorNode = AnchorNode(hitResult.createAnchor())
            val modelNode = ModelNode(
                context = this,
                lifecycle = lifecycle,
                glbFileLocation = "models/chair.glb",
                scaleToUnits = 0.5f
            )

            anchorNode.addChild(modelNode)
            arSceneView.addChild(anchorNode)
        }
    }

    override fun onResume() {
        super.onResume()
        arSceneView.onResume()
    }

    override fun onPause() {
        arSceneView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        arSceneView.destroy()
        super.onDestroy()
    }
}
```

------------------------------------------------------------------------

## 5. Các API Hữu Ích

  --------------------------------------------------------------------------------------------------
  Mục đích                                      API
  --------------------------------------------- ----------------------------------------------------
  Di chuyển mô hình                             `modelNode.position = Position(x, y, z)`

  Xoay mô hình                                  `modelNode.rotation = Rotation(x, y, z)`

  Phóng to/thu nhỏ                              `modelNode.scale = Scale(2.0f)`

  Thay đổi ánh sáng                             `sceneView.sunlight.intensity = 50_000f`

  Bắt sự kiện chạm                              `sceneView.setOnTouchListener { v, event -> ... }`

  Thêm nhiều node                               `sceneView.addChild(node)`
  --------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 6. Tối Ưu Hiệu Suất

-   Giảm kích thước model `.glb` (dùng Draco compression).
-   Giảm texture size (512/1024 thay vì 4K).
-   Tắt `enableStatistics` trong production.
-   Chỉ gọi `sceneView.addChild()` khi thật sự cần, tránh thêm/xóa liên
    tục trong mỗi frame.

------------------------------------------------------------------------

## 7. Lỗi Thường Gặp

-   **Model không hiển thị** → kiểm tra đường dẫn file
    `assets/models/xxx.glb`.
-   **App crash khi khởi động** → kiểm tra minSdkVersion ≥ 24.
-   **AR không chạy** → thiết bị không hỗ trợ ARCore hoặc chưa cài
    Google Play Services for AR.

------------------------------------------------------------------------


