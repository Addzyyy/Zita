package nl.utwente.processing.pmd.symbols

enum class ProcessingAppletMethodCategory(val category: String) {
    // Drawing
    SHAPE("Shape"),
    SHAPE_2D(SHAPE.name + " / 2D Primitives"),
    SHAPE_CURVES(SHAPE.name + " / Curves"),
    SHAPE_3D(SHAPE.name + " / 3D Primitives"),
    SHAPE_ATTRIBUTES(SHAPE.name + " / Attributes"),
    SHAPE_VERTEX(SHAPE.name + " / Vertex"),
    SHAPE_LD(SHAPE.name + " / Loading & Displaying"),
    TRANSFORM("Transform"),
    LIGHTSCAMERA("Lights, Camera"),
    LIGHTSCAMERA_LIGHTS(LIGHTSCAMERA.name + " / Lights"),
    LIGHTSCAMERA_CAMERA(LIGHTSCAMERA.name + " / Camera"),
    LIGHTSCAMERA_COORDINATES(LIGHTSCAMERA.name + " / Coordinates"),
    LIGHTSCAMERA_MATERIAL(LIGHTSCAMERA.name + " / Material Properties"),
    COLOR("Color"),
    COLOR_SETTING(COLOR.name + " / Setting"),
    COLOR_CR(COLOR.name + " / Creating & Reading"),
    IMAGE("Image"),
    IMAGE_LD(IMAGE.name + " / Loading & Displaying"),
    IMAGE_TEXTURES(IMAGE.name + " / Textures"),
    IMAGE_PIXELS(IMAGE.name + " / Pixels"),
    RENDERING("Rendering"),
    RENDERING_SHADERS(RENDERING.name + " / Shaders"),
    TYPOGRAPHY("Typography"),
    TYPOGRAPHY_LD(TYPOGRAPHY.name + " / Loading & Displaying"),
    TYPOGRAPHY_ATTRIBUTES(TYPOGRAPHY.name + " / Attributes"),
    TYPOGRAPHY_METRICS(TYPOGRAPHY.name + " / Metrics"),

    // Math
    MATH("Math")
}