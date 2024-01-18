package net.minestom.lwjgl.framebuffers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.MapColors;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;

/**
 * Abstract base class for GLFW capable framebuffers.
 */
public abstract class GLFWCapableBuffer {
    
    /** The colors of the framebuffer. */
    protected final byte[] colors;
    private final ByteBuffer pixels;
    private final long glfwWindow;
    private final int width;
    private final int height;
    private final ByteBuffer colorsBuffer;
    private boolean onlyMapColors;
    
    /**
     * This constructur is meant to be used for child classes for super constructor. It
     * is accessible with calling super(width, height).
     *
     * @param width The width of the GLFW capable framebuffer
     * @param height The height of the GLFW capable framebuffer
     */
    protected GLFWCapableBuffer(int width, int height) {
        this(width, height, GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context. This constructor is meant
     * to be used for child classes for super constructor. It is accessible with calling
     * super(width, height, apiContext, clientAPI);
     *
     * @param width The width of the framebuffer
     * @param height The height of the framebuffer
     * @param apiContext Context constant of package org.lwjgl.glfw.GLFW. For example
     * org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API
     * @param clientAPI The client api constant of package org.lwjgl.glfw.GLFW. For
     * example org.lwjgl.glfw.GLFW.GLFW_OPENGL_API
     */
    protected GLFWCapableBuffer(int width, int height, int apiContext, int clientAPI) {
        this.width = width;
        this.height = height;
        this.colors = new byte[width*height];
        colorsBuffer = BufferUtils.createByteBuffer(width*height);
        this.pixels = BufferUtils.createByteBuffer(width*height*4);
        if(!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        GLFWErrorCallback.createPrint().set();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_CREATION_API, apiContext);
        glfwWindowHint(GLFW_CLIENT_API, clientAPI);

        this.glfwWindow = glfwCreateWindow(width, height, "", 0L, 0L);
        if(glfwWindow == 0L) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer desc = stack.mallocPointer(1);
                int errcode = glfwGetError(desc);
                throw new RuntimeException("("+errcode+") Failed to create GLFW Window.");
            }
        }
    }

    /**
     * This function unbinds the context, like the OpenGL context, from the Thread.
     *
     * @return GLFWCapableBuffer The framebuffer that has been currently used on this
     * thread ( this )
     */
    public GLFWCapableBuffer unbindContextFromThread() {
        glfwMakeContextCurrent(0L);
        return this;
    }

    /**
     * This function binds the rendering context to this thread with this framebuffer.
     */
    public void changeRenderingThreadToCurrent() {
        glfwMakeContextCurrent(glfwWindow);
        GL.createCapabilities();
    }

    /**
     * This function settes the rendering loop to render on this framebuffer. It
     * will then call in a interval of the input millisecond count the Runnable,
     * with must define the function run that will be callen. Over the return value
     * Task, you can then manage this thread that is managed by the Minestom thread
     * pool.
     *
     * @param millis The interval to call the render function in milliseconds
     * @param rendering The Runnable, whichs run function will be callen for render on
     * on this framebuffer
     *
     * @return Task The Minestom task object to manage the render loop task
     */
    public Task setupLoop(long millis, Runnable rendering) {
        return MinecraftServer.getSchedulerManager()
                .buildTask(new Runnable() {
                    private boolean first = true;

                    @Override
                    public void run() {
                        if(first) {
                            changeRenderingThreadToCurrent();
                            first = false;
                        }
                        render(rendering);
                    }
                })
                .repeat(millis, ChronoUnit.MILLIS )
                .schedule();
    }

    /**
     * @deprecated Please use setupLoop instead.
     * 
     * This method can be used, but the input period will be interpreted as milli seconds.
     *
     * @param period Intervall to call the render loop
     * @param unit The timeunit to interpret the period, will ignore and instead use milliseconds
     * @param rendering The rendering Runnable to call for render on this framebuffer
     *
     * @return Task The task created by the Minestom thread pool, to manage it
     */
    @Deprecated
    public Task setupRenderLoop(long period, TimeUnit unit, Runnable rendering) {
        return MinecraftServer.getSchedulerManager()
                .buildTask(new Runnable() {
                    private boolean first = true;

                    @Override
                    public void run() {
                        if(first) {
                            changeRenderingThreadToCurrent();
                            first = false;
                        }
                        render(rendering);
                    }
                })
                .repeat(period, ChronoUnit.MILLIS )
                .schedule();
    }

    /**
     * Starts rendering on the overgiven Runnable. By calling it, it executes the run function
     * of the Runnable to render on this thread, and then swaps all buffers.
     *
     * @param rendering The Runnable with the render loop to execute rendering on this framebuffer
     */
    public void render(Runnable rendering) {
        rendering.run();
        glfwSwapBuffers(glfwWindow);
        prepareMapColors();
    }

    /**
     * Called in render after glFlush to read the pixel buffer contents and convert it to map colors.
     * Only call if you do not use {@link #render(Runnable)} nor {@link #setupRenderLoop(long, TimeUnit, Runnable)}
     */
    public void prepareMapColors() {
        if(onlyMapColors) {
            colorsBuffer.rewind();
            glReadPixels(0, 0, width, height, GL_RED, GL_UNSIGNED_BYTE, colorsBuffer);
            colorsBuffer.get(colors);
        } else {
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = Framebuffer.index(x, y, width)*4;
                    int red = pixels.get(i) & 0xFF;
                    int green = pixels.get(i+1) & 0xFF;
                    int blue = pixels.get(i+2) & 0xFF;
                    int alpha = pixels.get(i+3) & 0xFF;
                    int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    colors[Framebuffer.index(x, y, width)] = MapColors.closestColor(argb).getIndex();
                }
            }
        }
    }

    /**
     * This function cleans up the native apis. In fact it terminates glfw completely.
     */
    public void cleanup() {
        glfwTerminate();
    }

    /**
     * Returns the glfw window used for the framebuffer
     *
     * @return long Id of the glfw window, accessible for the lwjgl GLFW bindings
     */
    public long getGLFWWindow() {
        return glfwWindow;
    }

    /**
     * Returns the width of the framebuffer.
     *
     * @return int The framebuffer width
     */
    public int width() {
        return width;
    }

    /**
     * Returns the height of the framebuffer.
     *
     * @return int The framebuffer height
     */
    public int height() {
        return height;
    }

    /**
     * Tells this buffer that the **RED** channel contains the index of the map color to use.
     *
     * This allows for optimizations and fast rendering (because there is no need for a conversion)
     */
    public void useMapColors() {
        onlyMapColors = true;
    }

    /**
     * Opposite to {@link #useMapColors()}
     */
    public void useRGB() {
        onlyMapColors = false;
    }
}
