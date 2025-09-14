package com.namdp.sceneviewdemo

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.filament.Colors
import com.google.android.filament.LightManager
import com.google.android.filament.utils.KTX1Loader
import com.namdp.sceneviewdemo.databinding.ActivityMainBinding
import io.github.sceneview.SceneView.Companion.DEFAULT_MAIN_LIGHT_COLOR
import io.github.sceneview.SceneView.Companion.DEFAULT_MAIN_LIGHT_COLOR_INTENSITY
import io.github.sceneview.managers.color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.RenderableNode
import io.github.sceneview.utils.readBuffer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSkyBox()
        setIndirectLight()

        addNote()
        addModel()

        setMainLight()
    }

    private fun addModel() {
        binding.sceneView.modelLoader.loadModelInstanceAsync("models/cockroach.glb") {
            it?.let { modelInstance ->
                binding.sceneView.addChildNode(ModelNode(modelInstance = modelInstance))
            }
        }
    }

    fun addNote() {
        val engine = binding.sceneView.engine
        val cubeNote = CubeNode(
            engine,
            size = Size(0.2f),
            center = Position(0f, 0f, 0f),
        )
        binding.sceneView.addChildNode(cubeNote)
        setMaterialMetallic(cubeNote)
        addChildToParent(cubeNote)
        setAnim(cubeNote)

    }
    fun addChildToParent(parentNode: Node){
        val engine = binding.sceneView.engine
        val cubeNote = CubeNode(
            engine,
            size = Size(x=0.4f,y=0.2f,z=0.2f),
            center = Position(0.3001f, 0f, 0f),
        ).apply {
            isShadowCaster = false
        }
        setMaterialGlass(cubeNote)
        parentNode.addChildNode(cubeNote)
    }

    private fun setMaterialGlass(node: RenderableNode) {
        binding.sceneView.materialLoader.loadMaterialAsync("materials/glass.filamat") {
            val materialInstance = it?.createInstance()
            materialInstance?.setParameter("baseColor", Colors.RgbaType.SRGB, 1f, 1f, 1f, 0.001f)
            if (materialInstance != null) {
                node.materialInstance = materialInstance
            }
        }

    }

    private fun setMaterialMetallic(node: RenderableNode) {
        binding.sceneView.materialLoader.loadMaterialAsync("materials/metallic.filamat") {
            val materialInstance = it?.createInstance()
            materialInstance?.setParameter(
                "baseColor",
                Colors.RgbaType.SRGB,
                0.7f,
                0.7f,
                0.72f,
                1.0f
            )// R, G, B, A
            materialInstance?.setParameter("roughness", 0.35f)
            if (materialInstance != null) {
                node.materialInstance = materialInstance
            }
        }
    }

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

    fun setSkyBox() {
        val skybox = KTX1Loader.createSkybox(
            binding.sceneView.engine,
            assets.readBuffer(
                fileLocation = "light/test_ibl_skybox.ktx"
            )
        )
        binding.sceneView.skybox = skybox
    }

    fun setIndirectLight() {
        val indirectLight = KTX1Loader.createIndirectLight(
            binding.sceneView.engine,
            assets.readBuffer(
                fileLocation = "light/test_ibl_ibl.ktx"
            )
        )
        binding.sceneView.indirectLight = indirectLight
    }

    fun setMainLight() {
        binding.sceneView.mainLightNode = LightNode(
            engine = binding.sceneView.engine,
            type = LightManager.Type.DIRECTIONAL,
            apply = {
                color(DEFAULT_MAIN_LIGHT_COLOR)
                intensity(DEFAULT_MAIN_LIGHT_COLOR_INTENSITY)
                direction(0.0f, -1.0f, 0.0f)
                castShadows(true)
                shadowOptions(LightManager.ShadowOptions().apply {
                    constantBias = 0.05f
                    blurWidth = 3f
                })
            })
    }

}