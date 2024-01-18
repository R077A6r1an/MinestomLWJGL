package net.minestom.lwjgl.framebuffers;

import net.minestom.server.map.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;

/**
 * GLFW-based framebuffer.
 *
 * Due to its interfacing with OpenGL(-ES), extra care needs to be applied when using this framebuffer.
 * Rendering to this framebuffer should only be done via the thread on which the context is present.
 * To perform map conversion at the end of a frame, it is advised to use {@link #render(Runnable)} to render to the map.
 *
 * Use {@link #changeRenderingThreadToCurrent} in a thread to switch the thread on which to render.
 *
 * Use {@link #setupRenderLoop} with a callback to setup a task in the {@link net.minestom.server.timer.SchedulerManager}
 * to automatically render to the offscreen buffer on a specialized thread.
 *
 * GLFWFramebuffer does not provide guarantee that the result of {@link #toMapColors()} is synchronized with rendering, but
 * it will be updated after each frame rendered through {@link #render(Runnable)} or {@link #setupLoop(long, Runnable)}.
 *
 * This framebuffer is meant to render to a single map (ie it is only compatible with 128x128 rendering)
 */
public class GLFWFramebuffer extends GLFWCapableBuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH*HEIGHT];
    private final ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH*HEIGHT*4);

    /**
     * Constructor for a GLFW framebuffer. Initializes the framebuffer with default bindings to OpenGL and
     * native context api.
     */
    public GLFWFramebuffer() {
        this(GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context
     *
     * @param apiContext The api context to init, for example GLFW_NATIVE_CONTEXT_API
     * @param clientAPI The render client api, for example for OpenGL use GLFW_OPENGL_API
     */
    public GLFWFramebuffer(int apiContext, int clientAPI) {
        super(WIDTH, HEIGHT, apiContext, clientAPI);
    }

    /**
     * Maps the colors.
     *
     * @return byte[] The array with the colors
     */
    @Override
    public byte[] toMapColors() {
        return colors;
    }
}
