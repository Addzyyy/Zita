package nl.utwente.processing.pmd.symbols

import nl.utwente.processing.pmd.symbols.ProcessingAppletMethodCategory.*

/**
 * Object which defines Processing Applet definitions.
 */
object ProcessingApplet {

    val PARAM_FLOAT_PIXEL = ProcessingAppletParameter("float", true)
    val PARAM_FLOAT_NON_PIXEL = ProcessingAppletParameter("float", false)
    val PARAM_INT_PIXEL = ProcessingAppletParameter("int", true)
    val PARAM_INT_NON_PIXEL = ProcessingAppletParameter("int", false)
    val PARAM_PSHAPE = ProcessingAppletParameter("PShape", false)
    val PARAM_PIMAGE = ProcessingAppletParameter("PImage", false)
    val PARAM_PSHADER = ProcessingAppletParameter("PShader", false)
    val PARAM_PVECTOR = ProcessingAppletParameter("PVector", false)
    val PARAM_PFONT = ProcessingAppletParameter("PFont", false)
    val PARAM_STRING = ProcessingAppletParameter("String", false)
    val PARAM_CHAR = ProcessingAppletParameter("char", false)
    val PARAM_CHAR_ARRAY = ProcessingAppletParameter("char[]", false)
    val PARAM_FLOAT_ARRAY = ProcessingAppletParameter("float[]", false);

    val SETUP_METHOD_SIGNATURE = setOf("setup()");



    val DRAW_METHOD_SIGNATURE = setOf ("draw()")
    val DRAW_METHODS = setOf(
            // Shape / 2D Primitives
            //Arc method: https://processing.org/reference/arc_.html
            ProcessingAppletMethod("arc", repeatedFloatParam(4,2), SHAPE_2D),
            ProcessingAppletMethod("arc", repeatedFloatParam(6, 2), SHAPE_2D),

            //Circle method: https://processing.org/reference/circle_.html
            ProcessingAppletMethod("circle", repeatedFloatParam(3, 2), SHAPE_2D),

            //Ellipse method: https://processing.org/reference/ellipse_.html
            ProcessingAppletMethod("ellipse", repeatedFloatParam(4, 2), SHAPE_2D),

            //Line method: https://processing.org/reference/line_.html
            ProcessingAppletMethod("line", repeatedFloatParam(4), SHAPE_2D),
            ProcessingAppletMethod("line", repeatedFloatParam(6), SHAPE_2D),

            //Point method: https://processing.org/reference/point_.html
            ProcessingAppletMethod("point", repeatedFloatParam(2), SHAPE_2D),
            ProcessingAppletMethod("point", repeatedFloatParam(3), SHAPE_2D),

            //Quad method: https://processing.org/reference/quad_.html
            ProcessingAppletMethod("quad", repeatedFloatParam(8), SHAPE_2D),

            //Rect method: https://processing.org/reference/rect_.html
            ProcessingAppletMethod("rect", repeatedFloatParam(4, 2), SHAPE_2D),
            ProcessingAppletMethod("rect", repeatedFloatParam(5, 2), SHAPE_2D),
            ProcessingAppletMethod("rect", repeatedFloatParam(8, 2), SHAPE_2D),

            //Square method: https://processing.org/reference/square_.html
            ProcessingAppletMethod("square", repeatedFloatParam(3, 2), SHAPE_2D),

            //Triangle method: https://processing.org/reference/triangle_.html
            ProcessingAppletMethod("triangle", repeatedFloatParam(6, 6), SHAPE_2D),

            // Shape / Curves
            //Bezier method: https://processing.org/reference/bezier_.html
            ProcessingAppletMethod("bezier", repeatedFloatParam(8), SHAPE_CURVES),
            ProcessingAppletMethod("bezier", repeatedFloatParam(12), SHAPE_CURVES),

            //BezierPoint method: https://processing.org/reference/bezierPoint_.html
            ProcessingAppletMethod("bezierPoint", repeatedFloatParam(5, 4), SHAPE_CURVES),

            //BezierTangent method: https://processing.org/reference/bezierTangent_.html
            ProcessingAppletMethod("bezierTangent", repeatedFloatParam(5, 4), SHAPE_CURVES),

            //Curve method: https://processing.org/reference/curve_.html
            ProcessingAppletMethod("curve", repeatedFloatParam(8), SHAPE_CURVES),
            ProcessingAppletMethod("curve", repeatedFloatParam(12), SHAPE_CURVES),

            //CurvePoint method: https://processing.org/reference/curvePoint_.html
            ProcessingAppletMethod("curvePoint", repeatedFloatParam(5, 4), SHAPE_CURVES),

            //CurveTangent method: https://processing.org/reference/curveTangent_.html
            ProcessingAppletMethod("curveTangent", repeatedFloatParam(5, 4), SHAPE_CURVES),

            // Shape / 3D Primitives
            //Box method: https://processing.org/reference/box_.html
            ProcessingAppletMethod("box", repeatedFloatParam(1), SHAPE_3D),
            ProcessingAppletMethod("box", repeatedFloatParam(3), SHAPE_3D),

            //Sphere method: https://processing.org/reference/sphere_.html
            ProcessingAppletMethod("sphere", repeatedFloatParam(1), SHAPE_3D),

            // Shape / Attributes
            //EllipseMode method: https://processing.org/reference/ellipseMode_.html
            ProcessingAppletMethod("ellipseMode", listOf(PARAM_INT_NON_PIXEL), SHAPE_ATTRIBUTES),

            //RectMode method: https://processing.org/reference/rectMode_.html
            ProcessingAppletMethod("rectMode", listOf(PARAM_INT_NON_PIXEL), SHAPE_ATTRIBUTES),

            //StrokeCap method: https://processing.org/reference/strokeCap_.html
            ProcessingAppletMethod("strokeCap", listOf(PARAM_INT_NON_PIXEL), SHAPE_ATTRIBUTES),
            
            //StrokeJoin method: https://processing.org/reference/strokeJoin_.html
            ProcessingAppletMethod("strokeJoin", listOf(PARAM_INT_NON_PIXEL), SHAPE_ATTRIBUTES),

            //StrokeWeight method: https://processing.org/reference/strokeWeight_.html
            ProcessingAppletMethod("strokeWeight", listOf(PARAM_FLOAT_NON_PIXEL), SHAPE_ATTRIBUTES),

            // Shape / Vertex
            //Vertex method: https://processing.org/reference/vertex_.html
            ProcessingAppletMethod("vertex", repeatedFloatParam(2), SHAPE_VERTEX),
            ProcessingAppletMethod("vertex", repeatedFloatParam(3), SHAPE_VERTEX),
            ProcessingAppletMethod("vertex", listOf(PARAM_FLOAT_ARRAY), SHAPE_VERTEX),
            ProcessingAppletMethod("vertex", repeatedFloatParam(4), SHAPE_VERTEX),
            ProcessingAppletMethod("vertex", repeatedFloatParam(5), SHAPE_VERTEX),

            //BezierVertex method: https://processing.org/reference/bezierVertex_.html
            ProcessingAppletMethod("bezierVertex", repeatedFloatParam(6), SHAPE_VERTEX),
            ProcessingAppletMethod("bezierVertex", repeatedFloatParam(9), SHAPE_VERTEX),

            //CurveVertex method: https://processing.org/reference/curveVertex_.html
            ProcessingAppletMethod("curveVertex", repeatedFloatParam(2), SHAPE_VERTEX),
            ProcessingAppletMethod("curveVertex", repeatedFloatParam(3), SHAPE_VERTEX),

            //QuadraticVertex method: https://processing.org/reference/quadraticVertex_.html
            ProcessingAppletMethod("quadraticVertex", repeatedFloatParam(4), SHAPE_VERTEX),
            ProcessingAppletMethod("quadraticVertex", repeatedFloatParam(6), SHAPE_VERTEX),

            //BeginContour method: https://processing.org/reference/beginContour_.html
            ProcessingAppletMethod("beginContour", listOf(), SHAPE_VERTEX),
            //EndContour method: https://processing.org/reference/endContour_.html
            ProcessingAppletMethod("endContour", listOf(), SHAPE_VERTEX),

            //BeginShape method: https://processing.org/reference/beginShape_.html
            ProcessingAppletMethod("beginShape", listOf(), SHAPE_VERTEX),
            ProcessingAppletMethod("beginShape", listOf(PARAM_INT_NON_PIXEL), SHAPE_VERTEX),
            //EndShape method: https://processing.org/reference/endShape_.html
            ProcessingAppletMethod("endShape", listOf(), SHAPE_VERTEX),
            ProcessingAppletMethod("endShape", listOf(PARAM_INT_NON_PIXEL), SHAPE_VERTEX),
            
            // Shape / Loading & Displaying
            //Shape method: https://processing.org/reference/shape_.html
            ProcessingAppletMethod("shape", listOf(PARAM_PSHAPE), SHAPE_LD),
            ProcessingAppletMethod("shape", listOf(PARAM_PSHAPE, *repeatedFloatParam(2).toTypedArray()), SHAPE_LD),
            ProcessingAppletMethod("shape", listOf(PARAM_PSHAPE, *repeatedFloatParam(4).toTypedArray()), SHAPE_LD),

            // Transform
            //PushMatrix method: https://processing.org/reference/pushMatrix_.html
            ProcessingAppletMethod("pushMatrix", listOf(), TRANSFORM),
            //PopMatrix method: https://processing.org/reference/popMatrix_.html
            ProcessingAppletMethod("popMatrix", listOf(), TRANSFORM),
            //ResetMatrix method: https://processing.org/reference/resetMatrix_.html
            ProcessingAppletMethod("resetMatrix", listOf(), TRANSFORM),
            //ApplyMatrix method: https://processing.org/reference/applyMatrix_.html
            ProcessingAppletMethod("applyMatrix", repeatedFloatParam(6, 0), TRANSFORM),
            ProcessingAppletMethod("applyMatrix", repeatedFloatParam(16, 0), TRANSFORM),

            //Rotate method: https://processing.org/reference/rotate_.html
            ProcessingAppletMethod("rotate", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),
            //RotateX method: https://processing.org/reference/rotateX_.html
            ProcessingAppletMethod("rotateX", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),
            //RotateY method: https://processing.org/reference/rotateY_.html
            ProcessingAppletMethod("rotateY", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),
            //RotateZ method: https://processing.org/reference/rotateZ_.html
            ProcessingAppletMethod("rotateZ", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),

            //Scale method: https://processing.org/reference/scale_.html
            ProcessingAppletMethod("scale", repeatedFloatParam(1, 0), TRANSFORM),
            ProcessingAppletMethod("scale", repeatedFloatParam(2, 0), TRANSFORM),
            ProcessingAppletMethod("scale", repeatedFloatParam(3, 0), TRANSFORM),

            //ShearX method: https://processing.org/reference/shearX_.html
            ProcessingAppletMethod("shearX", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),
            //ShearY method: https://processing.org/reference/shearY_.html
            ProcessingAppletMethod("shearY", listOf(PARAM_FLOAT_NON_PIXEL), TRANSFORM),

            //Translate method: https://processing.org/reference/translate_.html
            ProcessingAppletMethod("scale", repeatedFloatParam(2, 0), TRANSFORM),
            ProcessingAppletMethod("scale", repeatedFloatParam(3, 0), TRANSFORM),

            // Color
            // Color / Setting
            //Background method: https://processing.org/reference/background_.html
            ProcessingAppletMethod("background", listOf(PARAM_INT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("background", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("background", repeatedFloatParam(1, 0), COLOR_SETTING),
            ProcessingAppletMethod("background", repeatedFloatParam(2, 0), COLOR_SETTING),
            ProcessingAppletMethod("background", repeatedFloatParam(3, 0), COLOR_SETTING),
            ProcessingAppletMethod("background", repeatedFloatParam(4, 0), COLOR_SETTING),
            ProcessingAppletMethod("background", listOf(PARAM_PIMAGE), COLOR_SETTING),

            //ColorMode method: https://processing.org/reference/colorMode_.html
            ProcessingAppletMethod("colorMode", listOf(PARAM_INT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("colorMode", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("colorMode", listOf(PARAM_INT_NON_PIXEL, *repeatedFloatParam(3, 0).toTypedArray()), COLOR_SETTING),
            ProcessingAppletMethod("colorMode", listOf(PARAM_INT_NON_PIXEL, *repeatedFloatParam(4, 0).toTypedArray()), COLOR_SETTING),

            //Fill method: https://processing.org/reference/fill_.html
            ProcessingAppletMethod("fill", listOf(PARAM_INT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("fill", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("fill", repeatedFloatParam(1, 0), COLOR_SETTING),
            ProcessingAppletMethod("fill", repeatedFloatParam(2, 0), COLOR_SETTING),
            ProcessingAppletMethod("fill", repeatedFloatParam(3, 0), COLOR_SETTING),
            ProcessingAppletMethod("fill", repeatedFloatParam(4, 0), COLOR_SETTING),

            //NoFill method: https://processing.org/reference/noFill_.html
            ProcessingAppletMethod("noFill", listOf(), COLOR_SETTING),

            //Stroke method: https://processing.org/reference/stroke_.html
            ProcessingAppletMethod("stroke", listOf(PARAM_INT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("stroke", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), COLOR_SETTING),
            ProcessingAppletMethod("stroke", repeatedFloatParam(1, 0), COLOR_SETTING),
            ProcessingAppletMethod("stroke", repeatedFloatParam(2, 0), COLOR_SETTING),
            ProcessingAppletMethod("stroke", repeatedFloatParam(3, 0), COLOR_SETTING),
            ProcessingAppletMethod("stroke", repeatedFloatParam(4, 0), COLOR_SETTING),

            //NoStroke method: https://processing.org/reference/noStroke_.html
            ProcessingAppletMethod("noStroke", listOf(), COLOR_SETTING),

            // Image
            // Image / Loading & Displaying
            //Image method: https://processing.org/reference/image_.html
            ProcessingAppletMethod("image", listOf(PARAM_PIMAGE, *repeatedFloatParam(2).toTypedArray()), IMAGE_LD),
            ProcessingAppletMethod("image", listOf(PARAM_PIMAGE, *repeatedFloatParam(4).toTypedArray()), IMAGE_LD),

            //Tint method: https://processing.org/reference/tint_.html
            ProcessingAppletMethod("tint", listOf(PARAM_INT_NON_PIXEL), IMAGE_LD),
            ProcessingAppletMethod("tint", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), IMAGE_LD),
            ProcessingAppletMethod("tint", repeatedFloatParam(1, 0), IMAGE_LD),
            ProcessingAppletMethod("tint", repeatedFloatParam(2, 0), IMAGE_LD),
            ProcessingAppletMethod("tint", repeatedFloatParam(3, 0), IMAGE_LD),
            ProcessingAppletMethod("tint", repeatedFloatParam(4, 0), IMAGE_LD),

            //NoTint method: https://processing.org/reference/noTint_.html
            ProcessingAppletMethod("noTint", listOf(), IMAGE_LD),

            // Image / Textures
            //Texture method: https://processing.org/reference/texture_.html
            ProcessingAppletMethod("texture", listOf(PARAM_PIMAGE), IMAGE_TEXTURES),

            // Image / Pixels
            //Blend method: https://processing.org/reference/blend_.html
            ProcessingAppletMethod("blend", repeatedIntParam(9, 8), IMAGE_PIXELS),
            ProcessingAppletMethod("blend", listOf(PARAM_PIMAGE) + repeatedIntParam(9, 8), IMAGE_PIXELS),

            //Copy method: https://processing.org/reference/copy_.html
            ProcessingAppletMethod("copy", listOf(), IMAGE_PIXELS),
            ProcessingAppletMethod("copy", repeatedIntParam(8), IMAGE_PIXELS),
            ProcessingAppletMethod("copy", listOf(PARAM_PIMAGE) + repeatedIntParam(8), IMAGE_PIXELS),

            //Filter method: https://processing.org/reference/filter_.html
            ProcessingAppletMethod("filter", listOf(PARAM_PSHADER), IMAGE_PIXELS),
            ProcessingAppletMethod("filter", listOf(PARAM_INT_NON_PIXEL), IMAGE_PIXELS),
            ProcessingAppletMethod("filter", listOf(PARAM_INT_NON_PIXEL, PARAM_FLOAT_NON_PIXEL), IMAGE_PIXELS),

            //Set method: https://processing.org/reference/set_.html
            ProcessingAppletMethod("set", repeatedIntParam(3, 2), IMAGE_PIXELS),
            ProcessingAppletMethod("set", repeatedIntParam(2) + PARAM_PIMAGE, IMAGE_PIXELS),

            //UpdatePixels method: https://processing.org/reference/updatePixels_.html
            ProcessingAppletMethod("updatePixels", listOf(), IMAGE_PIXELS),

            // Rendering
            //BlendMode method: https://processing.org/reference/blendMode_.html
            ProcessingAppletMethod("blendMode", listOf(PARAM_INT_NON_PIXEL), RENDERING),

            //Clip method: https://processing.org/reference/clip_.html
            ProcessingAppletMethod("clip", repeatedFloatParam(4, 2), RENDERING),

            //NoClip method: https://processing.org/reference/noClip_.html
            ProcessingAppletMethod("noClip", listOf(), RENDERING),

            // Rendering / Shaders
            //Shader method: https://processing.org/reference/shader_.html
            ProcessingAppletMethod("shader", listOf(PARAM_PSHADER), RENDERING_SHADERS),
            ProcessingAppletMethod("shader", listOf(PARAM_PSHADER, PARAM_INT_NON_PIXEL), RENDERING_SHADERS),

            //ResetShader method: https://processing.org/reference/resetShader_.html
            ProcessingAppletMethod("resetShader", listOf(), RENDERING_SHADERS),
            ProcessingAppletMethod("resetShader", listOf(PARAM_INT_NON_PIXEL), RENDERING_SHADERS),

            // Typography
            // Typography / Loading & Displaying
            //Text method: https://processing.org/reference/text_.html
            ProcessingAppletMethod("text", listOf(PARAM_CHAR, *repeatedFloatParam(2).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_CHAR, *repeatedFloatParam(3).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_STRING, *repeatedFloatParam(2).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_CHAR_ARRAY, PARAM_INT_NON_PIXEL, PARAM_INT_NON_PIXEL,
                    *repeatedFloatParam(2).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_STRING, *repeatedFloatParam(3).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_CHAR_ARRAY, PARAM_INT_NON_PIXEL, PARAM_INT_NON_PIXEL,
                    *repeatedFloatParam(3).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_STRING, *repeatedFloatParam(4).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_INT_NON_PIXEL, *repeatedFloatParam(2).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_INT_NON_PIXEL, *repeatedFloatParam(3).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_FLOAT_NON_PIXEL, *repeatedFloatParam(2).toTypedArray()), TYPOGRAPHY_LD),
            ProcessingAppletMethod("text", listOf(PARAM_FLOAT_NON_PIXEL, *repeatedFloatParam(3).toTypedArray()), TYPOGRAPHY_LD),

            //TextFont method: https://processing.org/reference/textFont_.html
            ProcessingAppletMethod("textFont", listOf(PARAM_PFONT), TYPOGRAPHY_LD),
            ProcessingAppletMethod("textFont", listOf(PARAM_PFONT, PARAM_INT_NON_PIXEL), TYPOGRAPHY_LD),

            // Typography / Attributes
            //TextAlign method: https://processing.org/reference/textAlign_.html
            ProcessingAppletMethod("textAlign", repeatedIntParam(1, 0), TYPOGRAPHY_ATTRIBUTES),
            ProcessingAppletMethod("textAlign", repeatedIntParam(2, 0), TYPOGRAPHY_ATTRIBUTES),

            //TextLeading method: https://processing.org/reference/textLeading_.html
            ProcessingAppletMethod("textLeading", listOf(PARAM_FLOAT_NON_PIXEL), TYPOGRAPHY_ATTRIBUTES),

            //TextSize method: https://processing.org/reference/textSize_.html
            ProcessingAppletMethod("textSize", listOf(PARAM_FLOAT_NON_PIXEL), TYPOGRAPHY_ATTRIBUTES)
    )

    val MATRIX_METHOD_SIGNATURES = setOf(
            //MouseClicked handler: https://processing.org/reference/mouseClicked_.html
            "pushMatrix",
            "popMatrix"
    )

    val PVECTOR_INSTANCE_METHODS = setOf(
            // http://processing.github.io/processing-javadocs/core/processing/core/PVector.html
            ProcessingAppletMethod("add", listOf(PARAM_PVECTOR), MATH),
            ProcessingAppletMethod("cross", listOf(PARAM_PVECTOR), MATH),
            ProcessingAppletMethod("dist", listOf(PARAM_PVECTOR), MATH),
            ProcessingAppletMethod("div", repeatedFloatParam(1, 0), MATH),
            ProcessingAppletMethod("dot", listOf(PARAM_PVECTOR), MATH),
            ProcessingAppletMethod("get", listOf(), MATH),
            ProcessingAppletMethod("heading", listOf(), MATH),
            ProcessingAppletMethod("lerp", listOf(PARAM_PVECTOR, PARAM_FLOAT_NON_PIXEL), MATH),
            ProcessingAppletMethod("limit", repeatedFloatParam(1, 0), MATH),
            ProcessingAppletMethod("mag", listOf(), MATH),
            ProcessingAppletMethod("magSq", listOf(), MATH),
            ProcessingAppletMethod("mult", repeatedFloatParam(1, 0), MATH),
            ProcessingAppletMethod("normalize", listOf(), MATH),
            ProcessingAppletMethod("rotate", repeatedFloatParam(1, 0), MATH),
            ProcessingAppletMethod("setMag", repeatedFloatParam(1, 0), MATH),
            ProcessingAppletMethod("sub", listOf(PARAM_PVECTOR), MATH)
    )

    val EVENT_METHOD_SIGNATURES = setOf(
            //MouseClicked handler: https://processing.org/reference/mouseClicked_.html
            "mouseClicked()",
            "mouseClicked(MouseEvent)",

            //MouseDragged handler: https://processing.org/reference/mouseDragged_.html
            "mouseDragged()",
            "mouseDragged(MouseEvent)",

            //MouseMoved handler: https://processing.org/reference/mouseMoved_.html
            "mouseMoved()",
            "mouseMoved(MouseEvent)",

            //MousePressed handler: https://processing.org/reference/mousePressed_.html
            "mousePressed()",
            "mousePressed(MouseEvent)",

            //MouseReleased handler: https://processing.org/reference/mouseReleased_.html
            "mouseReleased()",
            "mouseReleased(MouseEvent)",

            //MouseWheel handler: https://processing.org/reference/mouseWheel_.html
            "mouseWheel()",
            "mouseWheel(MouseEvent)",

            //KeyPressed handler: https://processing.org/reference/keyPressed_.html
            "keyPressed()",
            "keyPressed(KeyEvent)",

            //KeyReleased handler: https://processing.org/reference/keyReleased_.html
            "keyReleased()",
            "keyReleased(KeyEvent)",

            //KeyTyped handler: https://processing.org/reference/keyTyped_.html
            "keyTyped()",
            "keyTyped(KeyEvent)"
    )
    val EVENT_GLOBALS = setOf(
            "mouseButton", //https://processing.org/reference/mouseButton.html
            "mousePressed", //https://processing.org/reference/mousePressed.html
            "mouseX", //https://processing.org/reference/mouseX.html
            "mouseY", //https://processing.org/reference/mouseY.html
            "pmouseX", //https://processing.org/reference/pmouseX.html
            "pmouseY", //https://processing.org/reference/pmouseY.html

            "key", //https://processing.org/reference/key.html
            "keyCode", //https://processing.org/reference/keyCode.html
            "keyPressed" //https://processing.org/reference/keyPressed.html
    )

    private fun repeatedFloatParam(amount: Int, amountPixels: Int) : List<ProcessingAppletParameter> {
        val result = ArrayList<ProcessingAppletParameter>(amount)
        kotlin.repeat(amount, { i -> result.add(if (i < amountPixels) PARAM_FLOAT_PIXEL else PARAM_FLOAT_NON_PIXEL) })
        return result
    }

    private fun repeatedFloatParam(amount: Int) : List<ProcessingAppletParameter> {
        return this.repeatedFloatParam(amount, amount)
    }

    private fun repeatedIntParam(amount: Int, amountPixels: Int) : List<ProcessingAppletParameter> {
        val result = ArrayList<ProcessingAppletParameter>(amount)
        kotlin.repeat(amount, { i -> result.add(if (i < amountPixels) PARAM_INT_PIXEL else PARAM_INT_NON_PIXEL) })
        return result
    }

    private fun repeatedIntParam(amount: Int) : List<ProcessingAppletParameter> {
        return this.repeatedIntParam(amount, amount)
    }
}