# Hướng dẫn sử dụng SceneView

Đây là một dự án demo minh họa cách sử dụng thư viện [SceneView](https://github.com/SceneView/sceneview-android) để hiển thị và tương tác với các đối tượng 3D trong một ứng dụng Android gốc.


## Các bước thực hiện chính

### 1. Thêm `SceneView` vào Layout

`SceneView` là một `View` của Android và có thể được thêm vào file layout XML của bạn.

`app/src/main/res/layout/activity_main.xml`
```xml
<io.github.sceneview.SceneView
    android:id="@+id/sceneView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 2. Thiết lập môi trường Scene

Trong `MainActivity.kt`, chúng ta cần thiết lập môi trường 3D, bao gồm skybox (nền), ánh sáng gián tiếp (IBL) và nguồn sáng chính.

#### Thiết lập Skybox
Skybox tạo ra một nền trời hoặc môi trường xung quanh cho scene.
```kotlin
fun setSkyBox(sceneView: SceneView) {
    val skybox = KTX1Loader.createSkybox(
        sceneView.engine,
        assets.readBuffer(
            fileLocation = "light/test_ibl_skybox.ktx"
        )
    )
    binding.sceneView.skybox = skybox
}
```

#### Thiết lập Ánh sáng gián tiếp (Indirect Light)
Ánh sáng gián tiếp (IBL) sử dụng một hình ảnh môi trường để chiếu sáng các đối tượng trong scene một cách chân thực.
```kotlin
fun setIndirectLight(sceneView: SceneView) {
    val indirectLight = KTX1Loader.createIndirectLight(
        binding.sceneView.engine,
        assets.readBuffer(
            fileLocation = "light/test_ibl_ibl.ktx"
        )
    )
    sceneView.indirectLight = indirectLight
}
```

#### Thiết lập Nguồn sáng chính (Main Light)
Đây là nguồn sáng định hướng chính, ví dụ như mặt trời, tạo ra bóng đổ.
```kotlin
fun setMainLight(sceneView: SceneView) {
    sceneView.mainLightNode = LightNode(
        engine = sceneView.engine,
        type = LightManager.Type.DIRECTIONAL,
        apply = {
            color(DEFAULT_MAIN_LIGHT_COLOR)
            intensity(DEFAULT_MAIN_LIGHT_COLOR_INTENSITY)
            direction(0.0f, -1.0f, 0.0f) // Hướng từ trên xuống
            castShadows(true)
        })
}
```

### 3. Tải và hiển thị Model 3D

Model 3D (`.glb`) được tải không đồng bộ từ thư mục `assets`. Sau khi tải xong, nó được thêm vào scene dưới dạng một `ModelNode`.

```kotlin
private fun addModel(sceneView: SceneView) {
   sceneView.modelLoader.loadModelInstanceAsync("models/cockroach.glb") {
        it?.let { modelInstance ->
            sceneView.addChildNode(ModelNode(modelInstance = modelInstance))
        }
    }
}
```

### 4. Tạo đối tượng và áp dụng vật liệu

Bạn có thể tạo các đối tượng cơ bản (như hình lập phương) và áp dụng vật liệu tùy chỉnh cho chúng.

#### Tạo một `Node`
```kotlin
fun addNote(sceneView: SceneView) {
    val engine = sceneView.engine
    val cubeNote = CubeNode(
        engine,
        size = Size(0.2f),
        center = Position(0f, 0f, 0f),
    )
    sceneView.addChildNode(cubeNote)
}
```

#### Áp dụng vật liệu
Vật liệu (`.filamat`) được tải không đồng bộ và áp dụng cho `materialInstance` của một `RenderableNode`.
```kotlin
private fun setMaterialMetallic(sceneView: SceneView,node: RenderableNode) {
    sceneView.materialLoader.loadMaterialAsync("materials/metallic.filamat") {
        val materialInstance = it?.createInstance()
        // Tùy chỉnh các tham số của vật liệu
        materialInstance?.setParameter("baseColor", Colors.RgbaType.SRGB, 0.7f, 0.7f, 0.72f, 1.0f)
        materialInstance?.setParameter("roughness", 0.35f)
        node.materialInstance = materialInstance
    }
}
```

### 5. Thay đổi vị trí, xoay và tỷ lệ của đối tượng
Di chuyển vật thể
```kotlin
private fun transformObject(note:Node) {
    node.transform(
        position = Position(x, y, z),// Di chuyển theo trục x, y, z đơn vị mét (m)
    )
}
```

Xoay vật thể
```kotlin
private fun rotateObject(note:Node) {
    node.transform(
        rotation = Rotation(x,y,z), // Xoay theo trục x, y, z đơn vị độ
    )
}
```


Thay đổi tỉ lệ vật thể
```kotlin
private fun scaleObject(note:Node) {
    node.transform(
        scale = Scale(x,y,z) // Tỷ lệ theo trục x, y, z
    )
}
```
có thể kết hợp với nhau để thay đổi vị trí, xoay và tỷ lệ của đối tượng.
```kotlin
private fun transformObject(note:Node) {
    node.transform(
        position = Position(x, y, z),
        rotation = Rotation(x,y,z), 
        scale = Scale(x,y,z)
    )
}
```
Thứ tự vật thể sẽ luôn được di chuyển đến vị trí `position` trước rồi xoay theo `rotate` và thay đổi tỉ lệ theo `scale` sau với tâm là tâm của vật thể


### 6. Tạo Animation

Sử dụng `ValueAnimator` của Android để tạo các animation đơn giản bằng cách thay đổi thuộc tính `transform` (vị trí, xoay, tỷ lệ) của một `Node` theo thời gian.

```kotlin
fun setAnim(node: Node) {
    ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener {
            val value = it.animatedValue as Float
            node.transform(
                position = Position(0f, 0f, 1f * value),
                rotation = Rotation(x = 360 * value),
                scale = Scale(z = 1f + value, x = 1f, y = 1f)
            )
        }
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        duration = 2000
        start()
    }
}
```

### 7. Quan hệ Cha-Con (Parent-Child)

Bạn có thể gắn một `Node` vào một `Node` khác để tạo ra hệ thống phân cấp. Khi `Node` cha di chuyển, các `Node` con cũng sẽ di chuyển theo.

```kotlin
fun addChildToParent(parentNode: Node){
    val engine = binding.sceneView.engine
    val cubeNote = CubeNode(...)
    // ...
    parentNode.addChildNode(cubeNote)
}
```

### 8. Tối Ưu Hiệu Suất
- Có thể sử dụng DynamicResolutionOptions để thay đổi chất lượng hiển thị (minScale số càng nhỏ càng bị mờ như hiệu xuất sẽ tốt hơn)
```kotlin
    val dynamic = sceneView.view.dynamicResolutionOptions
    dynamic.apply {
        enabled = true
        minScale = 0.7f
        maxScale = 1.0f
        homogeneousScaling = true
        quality = View.QualityLevel.MEDIUM
    }
    sceneView.view.setDynamicResolutionOptions(dynamic)
```

- Giản tần xuất render (FPS) mặc định SceneView sẽ sử dụng Choreographer để cập nhật nên sẽ luôn render tần xuất tối đa của máy nên có thể bị nóng máy nên phải clone về sửa hàm `onFrame(frameTimeNanos: Long)` (có thể check thời gian) để giảm tần xuất render

- Chỉ gọi sceneView.addChild() khi thật sự cần, tránh thêm/xóa liên tục trong mỗi frame.

- Giảm kích thước model .glb (có thể dùng gltf-transform để nén định dạng etc1s)

- Giảm bớt hiệu ứng vật lí và mesh trong model .glb không cần thiết

### 9. Set cho Wallpaper
Tạo Wallpaper Service
```kotlin
class Wallpaper3DService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return Engine3D(this)
    }
    private inner class Engine3D(val context: Context) : Engine() {
        private var sceneView: SceneView? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            sceneView = object : SceneView(context) {
                override fun getHolder(): SurfaceHolder {
                    // Override để SceneView dùng Surface của Live Wallpaper
                    return surfaceHolder
                }

            }
        }
        
        override fun onDestroy() {
            sceneView?.destroy()
            sceneView = null
            super.onDestroy()
        }
        // thêm logic gọi sceneView?.onFrame(System.nanoTime()) khi cần cập nhật
        // thêm logic dừng cập play pause 
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                //play
            } else {
                //pause
            }
        }
    }
}

```

### 10. Lỗi Thường Gặp

- Các vật liệu đổ bóng bị lỗi: thay đổi giá trị constantBias trong mainLight
```kotlin
fun setMainLight(sceneView: SceneView) {
       sceneView.mainLightNode = LightNode(
            ...
            apply = {
                ...
                shadowOptions(LightManager.ShadowOptions().apply {
                    constantBias = 0.05f /(độ lệch bóng)
                })
            })
    }
```
- Không thấy bóng của vật thể:
  - kiểm tra lại hướng nguần sáng (mainLight)
  - Vật thể đổ bóng phải đặt `isShadowCaster = true`
  - Vật thể nhận bóng phải đặt `isShadowReceiver = true`
- Hiển thị đen:
  - Kiểm tra có set null indirectLight và mainLightNode không (2 nguần sáng này phải luôn có mặc định SceneView đã setup sẵn)
  - Kiểm tra có set sai viewport không (mặc định đã set chuẩn)
  - Kiểm tra xem có camera hoặc set sai không (mặc định đã set chuẩn)



