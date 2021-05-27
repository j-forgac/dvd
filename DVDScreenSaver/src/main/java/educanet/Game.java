package educanet;


import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Game {
    public static float topLeftX = -0.0125f;
    public static float topLeftY = 0.125f;
    public static float speedX = 0.00003f;
    public static float speedY = 0.00005f;

    private final static float[] vertices = {
            0.125f, 0.125f, 0f, // 0 -> Top right
            0.125f, -0.125f, 0f, // 1 -> Bottom right
            -0.125f, -0.125f, 0f, // 2 -> Bottom left
            -0.125f, 0.125f, 0f, // 3 -> Top left
    };

    private static final float[] colors = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
    };

    private final static int[] indices = {
            0, 1, 3, // First triangle
            1, 2, 3 // Second triangle
    };
    private static final float[] textures = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    private static int squareVaoId;
    private static int squareVboId;
    private static int squareEboId;
    private static int colorsId;
    private static int textureIndicesId;
    private static int uniformColorLocation;
    private static int uniformMatrixLocation;
    private static int textureId;

    private static Matrix4f matrix = new Matrix4f()
            .identity();
    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public static void init() {
        Shaders.initShaders();
        loadImage();

        uniformColorLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "outColor");
        uniformMatrixLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");
        squareVaoId = GL33.glGenVertexArrays();
        squareVboId = GL33.glGenBuffers();
        squareEboId = GL33.glGenBuffers();
        colorsId = GL33.glGenBuffers();
        textureIndicesId = GL33.glGenBuffers();
        textureId = GL33.glGenTextures();

        GL33.glBindVertexArray(squareVaoId);

        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, squareEboId);
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length)
                .put(indices)
                .flip();
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ib, GL33.GL_STATIC_DRAW);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareVboId);

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length)
                .put(vertices)
                .flip();

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);


        GL33.glUseProgram(Shaders.shaderProgramId);
        GL33.glUniform3f(uniformColorLocation, 1.0f, 0.0f, 0.0f);

        matrix.get(matrixBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixBuffer);

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0,3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, colorsId);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors.length)
                .put(colors)
                .flip();

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1,3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(1);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, textureIndicesId);

        FloatBuffer tb = BufferUtils.createFloatBuffer(textures.length)
                .put(textures)
                .flip();

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(2);

        MemoryUtil.memFree(fb);
    }

    public static void render() {
        matrix.get(matrixBuffer);

        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixBuffer);
        GL33.glUseProgram(Shaders.shaderProgramId);
        GL33.glBindVertexArray(squareVaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, vertices.length, GL33.GL_UNSIGNED_INT, 0);
    }

    public static void update() {
        topLeftX += speedX;
        topLeftY += speedY;

        if(topLeftX > 1f - 0.125f || topLeftX < -1f + 0.125f){
            speedX *= -1;
        }
        if(topLeftY > 1f || topLeftY < -1f + 0.25f){
            speedY *= -1;
        }

        matrix = matrix.translate(speedX, speedY, 0f);
    }

    public static void loadImage() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer img = STBImage.stbi_load("res/img.png", w, h, comp, 3);
            if (img != null) {
                img.flip();

                GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
                GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGB, w.get(), h.get(), 0, GL33.GL_RGB, GL33.GL_UNSIGNED_BYTE, img);
                GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

                STBImage.stbi_image_free(img);
            }
        }
    }

}
