package net.minestom.lwjgl.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;
import net.minestom.server.map.framebuffers.LargeFramebufferDefaultView;
import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;

/**
 * Describes a large GLFW framebuffer, which higher rendering resolution
 * then the normal maps.
 */
public class LargeGLFWFramebuffer extends GLFWCapableBuffer implements LargeFramebuffer {

    /**
     * Constructor for the large framebuffer. Initializes the framebuffer by the given
     * input dimensions.
     *
     * @param width The framebuffer width
     * @param height The framebuffer height
     */
    public LargeGLFWFramebuffer(int width, int height) {
        this(width, height, GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Constructor for the large framebuffer by the input context and client api to use.
     *
     * @param width The framebuffer width
     * @param height The framebuffer height
     * @param apiContext The api to use
     * @param clientAPI The client rendering api to use
     */
    public LargeGLFWFramebuffer(int width, int height, int apiContext, int clientAPI) {
        super(width, height, apiContext, clientAPI);
    }

    /**
     * Creates the map view of this object.
     *
     * @param left The left offset of this framebuffer
     * @param top The top offset
     *
     * @return Framebuffer The framebuffer of this object
     */
    @Override
    public Framebuffer createSubView(int left, int top) {
        return new LargeFramebufferDefaultView(this, left, top);
    }

    /**
     * Maps the color of the framebuffer.
     *
     * @param x Get the x position
     * @param y Get the y position
     *
     * @return byte The value of the color on this x-y-position
     */
    @Override
    public byte getMapColor(int x, int y) {
        return colors[Framebuffer.index(x, y, width())];
    }
}
