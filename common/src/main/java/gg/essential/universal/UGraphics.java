package gg.essential.universal;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D;
import static org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import gg.essential.universal.render.ScissorState;
import gg.essential.universal.utils.ReleasedDynamicTexture;
import gg.essential.universal.vertex.UVertexConsumer;
import kotlin.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import xyz.bluspring.unitytranslate.client.renderer.BatchedGuiRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;


@SuppressWarnings("deprecation") // lots of MC methods are deprecated on some versions but only replaced on the next one
public class UGraphics {
    private static final Pattern formattingCodePattern = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");

    private static UMatrixStack UNIT_STACK = UMatrixStack.UNIT;


    public static Style EMPTY_WITH_FONT_ID = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "alt")));
    public static int ZERO_TEXT_ALPHA = 10;
    private BufferBuilder instance;
    private VertexFormat vertexFormat;

    private static final Font.DisplayMode TEXT_LAYER_TYPE = Font.DisplayMode.NORMAL;

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(65536);

    public UGraphics(BufferBuilder instance) {
        this.instance = instance;
    }

    public UVertexConsumer asUVertexConsumer() {
        return UVertexConsumer.of(instance);
    }

    public static UGraphics getFromTessellator() {
        return new UGraphics(null);
    }



    // No possible alternative on 1.21. A compile time error here is better than a run time one.

    public static boolean isCoreProfile() {
        return true;
    }

    @Deprecated // only works on Forge 1.12.2 and below (relies on a Forge patch)
    public static void enableStencil() {
    }

    @Deprecated // see UGraphics.Globals
    public static void cullFace(int mode) {

    }

    public static void enableLighting() {
    }

    public static void disableLighting() {
    }

    public static void disableLight(int mode) {
    }

    public static void enableLight(int mode) {
    }

    @Deprecated // see UGraphics.Globals
    public static void enableBlend() {
    }

    /**
     * @deprecated see {@link #enableTexture2D()}
     */
    @Deprecated
    public static void disableTexture2D() {
        // no-op

    }

    public static void disableAlpha() {
    }

    public static void alphaFunc(int func, float ref) {
    }

    public static void shadeModel(int mode) {
    }

    @Deprecated // see UGraphics.Globals
    public static void blendEquation(int equation) {
    }

    @Deprecated // see UGraphics.Globals
    public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
    }

    /**
     * @deprecated Relies on the global {@link #setActiveTexture(int) activeTexture} state.<br>
     *     Instead of manually managing TEXTURE_2D state, prefer using
     *     {@link #beginWithDefaultShader(DrawMode, CommonVertexFormats)} or any of the other non-deprecated begin methods as
     *     these will set (and restore) the appropriate state for the given {@link CommonVertexFormats} right before/after
     *     rendering.<br>
     *     Also incompatible with OpenGL Core / MC 1.17.
     */
    @Deprecated
    public static void enableTexture2D() {
        // no-op
    }

    @Deprecated // see UGraphics.Globals
    public static void disableBlend() {
    }

    public static void deleteTexture(int glTextureId) {
        GL11.glDeleteTextures(glTextureId);
    }

    public static void enableAlpha() {
    }

    public static void configureTexture(int glTextureId, Runnable block) {
        int prevTextureBinding = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(glTextureId);

        block.run();

        GlStateManager._bindTexture(prevTextureBinding);
    }

    public static void configureTextureUnit(int index, Runnable block) {
        int prevActiveTexture = getActiveTexture();
        setActiveTexture(GL_TEXTURE0 + index);

        block.run();

        setActiveTexture(prevActiveTexture);
    }

    /**
     * @deprecated Changes global state and may as such easily lead to bug if the caller forgets to store the previous
     * state and restore it afterwards.<br>
     * Prefer {@link #configureTextureUnit(int, Runnable)} instead (which only changes the global state for the duration
     * of the given block).<br>
     * If you must change it for longer, then use {@link #getActiveTexture()} before {@link #setActiveTexture(int)} and
     * make sure to restore it afterwards.
     */
    @Deprecated
    public static void activeTexture(int glId) {
        setActiveTexture(glId);
    }

    public static int getActiveTexture() {
        return GL11.glGetInteger(GL_ACTIVE_TEXTURE);
    }

    public static void setActiveTexture(int glId) {
        GlStateManager._activeTexture(glId);
    }

    /**
     * @deprecated Relies on the global {@link #setActiveTexture(int) activeTexture} state.<br>
     * Prefer {@link #bindTexture(int, int)} instead.
     */
    @Deprecated
    public static void bindTexture(int glTextureId) {
        bindTexture(getActiveTexture() - GL_TEXTURE0, glTextureId);
    }

    /**
     * @deprecated Relies on the global {@link #setActiveTexture(int) activeTexture} state.<br>
     * Prefer {@link #bindTexture(int, Identifier)} instead.
     */
    @Deprecated
    public static void bindTexture(Identifier resourceLocation) {
        bindTexture(getOrLoadTextureId(resourceLocation));
    }

    @Deprecated // see UGraphics.Globals
    public static void bindTexture(int index, int glTextureId) {
        throw new UnsupportedOperationException("No longer supported on 1.21.11+, use `UBufferBuilder`/`URenderPipeline` instead.");
    }

    @Deprecated // see UGraphics.Globals
    public static void bindTexture(int index, Identifier resourceLocation) {
        bindTexture(index, getOrLoadTextureId(resourceLocation));
    }

    private static int getOrLoadTextureId(Identifier resourceLocation) {
        TextureManager textureManager = UMinecraft.getMinecraft().getTextureManager();
        AbstractTexture texture = textureManager.getTexture(resourceLocation);
        if (texture == null) {
            texture = new SimpleTexture(resourceLocation);
            textureManager.register(resourceLocation, texture);
        }
        return ((GlTexture) texture.getTexture()).glId();
    }

    public static int getStringWidth(String in) {
        return UMinecraft.getFontRenderer().width(in);
    }

    public static int getFontHeight() {
        return UMinecraft.getFontRenderer().lineHeight;
    }

    @Deprecated // Pass UMatrixStack as first arg, required for 1.17+
    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        drawString(UNIT_STACK, text, x, y, color, shadow);
    }

    public static void drawString(UMatrixStack stack, String text, float x, float y, int color, boolean shadow) {
        if ((color >> 24 & 255) <= 10) return;
        GlyphDrawerImpl drawerImpl = new GlyphDrawerImpl(stack.peek().getModel());
        UMinecraft.getFontRenderer().prepareText(text, x, y, color, shadow, 0).visit(drawerImpl);
        drawerImpl.flush();
    }

    @Deprecated // Pass UMatrixStack as first arg, required for 1.17+
    public static void drawString(String text, float x, float y, int color, int shadowColor) {
        drawString(UNIT_STACK, text, x, y, color, shadowColor);
    }

    public static void drawString(UMatrixStack stack, String text, float x, float y, int color, int shadowColor) {
        if ((color >> 24 & 255) <= 10) return;
        String shadowText = ChatColor.Companion.stripColorCodes(text);
        GlyphDrawerImpl drawerImpl = new GlyphDrawerImpl(stack.peek().getModel());
        UMinecraft.getFontRenderer().prepareText(shadowText, x + 1f, y + 1f, shadowColor, false, 0).visit(drawerImpl);
        drawerImpl.matrix = drawerImpl.matrix.translate(0f, 0f, Font.SHADOW_DEPTH, new org.joml.Matrix4f());
        UMinecraft.getFontRenderer().prepareText(text, x, y, color, false, 0).visit(drawerImpl);
        drawerImpl.flush();
    }

    private static class GlyphDrawerImpl implements Font.GlyphVisitor {
        private final GpuTextureView lightTexture = Minecraft.getInstance().gameRenderer.lightmap();
        // lightmap() may return either the uiLightmap, which is 1x1, or the regular lightmap (like pre-26.1)
        // and the text shader uses texelFetch which ignores the wrapping mode, so we need to pass it 0/0 as the
        // light coord or it will simply return 0 as the color.
        // for the 0, see GlyphRenderState.buildVertices
        // for the 0x00F0_00F0, see e.g. DrawableGizmoPrimitives.Group.renderTexts
        private final int light = lightTexture.getWidth(0) == 1 ? 0 : 0x00F0_00F0;
        private org.joml.Matrix4f matrix;
        private RenderPipeline pipeline;
        private GpuTextureView texture;
        private BufferBuilder bufferBuilder;

        private GlyphDrawerImpl(org.joml.Matrix4f matrix) {
            this.matrix = matrix;
        }

        public void flush() {
            if (bufferBuilder == null) {
                return;
            }
            RenderPipeline pipeline = this.pipeline;
            GpuTextureView texture = this.texture;
            BufferBuilder bufferBuilder = this.bufferBuilder;
            this.pipeline = null;
            this.texture = null;
            this.bufferBuilder = null;

            try (MeshData builtBuffer = bufferBuilder.build()) {
                if (builtBuffer == null) return;
                BatchedGuiRenderer.INSTANCE.queue(pipeline, TextureSetup.singleTextureWithLightmap(texture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)), null, consumer -> {
                    BatchedGuiRenderer.copyBuffer(builtBuffer, consumer);
                    return Unit.INSTANCE;
                });
            }
        }

        private void draw(TextRenderable drawable) {
            if (pipeline != drawable.guiPipeline() || texture != drawable.textureView()) {
                flush();
                pipeline = drawable.guiPipeline();
                texture = drawable.textureView();
                bufferBuilder = new BufferBuilder(ALLOCATOR, pipeline.getPrimitiveTopology(), pipeline.getVertexFormatBinding(0));
            }
            drawable.render(matrix, bufferBuilder, light, false);
        }
        @Override public void acceptGlyph(TextRenderable.Styled glyph) { draw(glyph); }
        @Override public void acceptEffect(TextRenderable drawable) { draw(drawable); }
    }

    public static List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return listFormattedStringToWidth(str, wrapWidth, true);
    }

    public static List<String> listFormattedStringToWidth(String str, int wrapWidth, boolean safe) {
        if (safe) {
            String tmp = formattingCodePattern.matcher(str).replaceAll("");
            int max = 0;
            for (String s : tmp.split(" "))
                max = Math.max(max, getStringWidth(s));
            wrapWidth = Math.max(max, wrapWidth);
        }

        // TODO: Validate this code
        List<String> strings = new ArrayList<>();

        StringSplitter charManager = UMinecraft.getFontRenderer().getSplitter();
        FormattedText properties = charManager.headByWidth(Component.literal(str).withStyle(EMPTY_WITH_FONT_ID), wrapWidth, Style.EMPTY);
        // From net.minecraft.util.text.ITextProperties line 88
        properties.visit(string -> {
            strings.add(string);
            return Optional.empty();
        });
        return strings;
    }

    public static float getCharWidth(char character) {
        return getStringWidth(String.valueOf(character));
    }

    public static void clearColor(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
    }

    public static void clearDepth(double depth) {
        GL11.glClearDepth(depth);
    }

    public static void glClear(int mode) {
        GL11.glClear(mode);
    }

    public static void glClearStencil(int mode) {
        GL11.glClearStencil(mode);
    }

    public static ReleasedDynamicTexture getTexture(InputStream stream) {
        try {
            return new ReleasedDynamicTexture(NativeImage.read(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to read image");
    }

    public static ReleasedDynamicTexture getTexture(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos );
            return new ReleasedDynamicTexture(NativeImage.read(new ByteArrayInputStream(baos.toByteArray())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to create texture");
    }

    public static ReleasedDynamicTexture getEmptyTexture() {
        return new ReleasedDynamicTexture(0, 0);
    }

    public static void glUseProgram(int program) {
        GlStateManager._glUseProgram(program);
    }

    public static boolean isOpenGl21Supported() {
        return true;
    }

    public static boolean areShadersSupported() {
        return true;
    }

    public static int glCreateProgram() {
        return GlStateManager.glCreateProgram();
    }

    public static int glCreateShader(int type) {
        return GlStateManager.glCreateShader(type);
    }

    public static void glCompileShader(int shaderIn) {
        GlStateManager.glCompileShader(shaderIn);
    }

    public static int glGetShaderi(int shaderIn, int pname) {
        return GlStateManager.glGetShaderi(shaderIn,pname);
    }

    public static String glGetShaderInfoLog(int shader, int maxLen) {
        return GlStateManager.glGetShaderInfoLog( shader,maxLen);
    }

    public static void glAttachShader(int program, int shaderIn) {
        GlStateManager.glAttachShader(program,shaderIn);
    }

    public static void glLinkProgram(int program) {
        GlStateManager.glLinkProgram(program);
    }

    public static int glGetProgrami(int program, int pname) {
        return GlStateManager.glGetProgrami(program,pname);
    }

    public static String glGetProgramInfoLog(int program, int maxLen) {
        return GlStateManager.glGetProgramInfoLog(program, maxLen);
    }

    public static void color4f(float red, float green, float blue, float alpha) {
    }

    public static void directColor3f(float red, float green, float blue) {
        color4f(red, green, blue, 1f);
    }

    @Deprecated // see UGraphics.Globals
    public static void enableDepth() {
    }

    @Deprecated // see UGraphics.Globals
    public static void depthFunc(int mode) {
    }

    @Deprecated // see UGraphics.Globals
    public static void depthMask(boolean flag) {
    }

    @Deprecated // see UGraphics.Globals
    public static void disableDepth() {
    }

    public static void enableScissor(int x, int y, int width, int height) {
        new ScissorState(true, x, y, width, height).activate();
    }

    public static void disableScissor() {
        new ScissorState(false, 0, 0, 0, 0).activate();
    }


    public enum DrawMode {
        LINES(GL11.GL_LINES),
        /**
         * @deprecated No longer properly supported as of 1.21.11, use {@link #LINES} instead
         */
        @Deprecated
        LINE_STRIP(GL11.GL_LINE_STRIP),
        TRIANGLES(GL11.GL_TRIANGLES),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN),
        QUADS(GL11.GL_QUADS),
        ;

        public final int glMode;
        //? if > 26.1 {
        public final PrimitiveTopology mcMode;
        //? } else {
        /*public final VertexFormat.Mode mcMode;
        *///? }

        DrawMode(int glMode) {
            this.glMode = glMode;
            this.mcMode = glToMcDrawMode(glMode);
        }

        private static /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/ glToMcDrawMode(int glMode) {
            switch (glMode) {
                case GL11.GL_LINES: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.LINES;
                case GL11.GL_LINE_STRIP: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.DEBUG_LINE_STRIP;
                case GL11.GL_TRIANGLES: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.TRIANGLES;
                case GL11.GL_TRIANGLE_STRIP: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.TRIANGLE_STRIP;
                case GL11.GL_TRIANGLE_FAN: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.TRIANGLE_FAN;
                case GL11.GL_QUADS: return /*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/.QUADS;
                default: throw new IllegalArgumentException("Unsupported draw mode " + glMode);
            }
        }

        private static DrawMode fromMc(/*? if > 26.1 { */PrimitiveTopology/*? } else { *//*VertexFormat.Mode*//*? }*/ mcMode) {
            switch (mcMode) {
                case LINES: return DrawMode.LINES;
                case DEBUG_LINE_STRIP: return DrawMode.LINE_STRIP;
                case TRIANGLES: return DrawMode.TRIANGLES;
                case TRIANGLE_STRIP: return DrawMode.TRIANGLE_STRIP;
                case TRIANGLE_FAN: return DrawMode.TRIANGLE_FAN;
                case QUADS: return DrawMode.QUADS;
                default: throw new IllegalArgumentException("Unsupported draw mode " + mcMode);
            }
        }

        public static DrawMode fromGl(int glMode) {
            switch (glMode) {
                case GL11.GL_LINES: return LINES;
                case GL11.GL_LINE_STRIP: return LINE_STRIP;
                case GL11.GL_TRIANGLES: return TRIANGLES;
                case GL11.GL_TRIANGLE_STRIP: return TRIANGLE_STRIP;
                case GL11.GL_TRIANGLE_FAN: return TRIANGLE_FAN;
                case GL11.GL_QUADS: return QUADS;
                default: throw new IllegalArgumentException("Unsupported draw mode " + glMode);
            }
        }

        public static DrawMode fromRenderLayer(RenderType renderLayer) {
            return fromMc(renderLayer./*? if > 26.1 { */primitiveTopology/*? } else { *//*mode*//*? }*/());
        }
    }

    public enum CommonVertexFormats {
        POSITION(DefaultVertexFormat.POSITION),
        POSITION_COLOR(DefaultVertexFormat.POSITION_COLOR),
        POSITION_TEXTURE(DefaultVertexFormat.POSITION_TEX),
        POSITION_TEXTURE_COLOR(DefaultVertexFormat.POSITION_TEX_COLOR),
        POSITION_COLOR_TEXTURE_LIGHT(DefaultVertexFormat.BLOCK),
        /**
         * @deprecated Minecraft removed the built-in shader for this vertex format in 1.20.5, so it is no
         * longer universal across all versions.
         */
        @Deprecated
        POSITION_TEXTURE_LIGHT_COLOR(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
        POSITION_TEXTURE_COLOR_LIGHT(DefaultVertexFormat.PARTICLE),
        /**
         * @deprecated Minecraft removed the built-in shader for this vertex format in 1.20.5, so it is no
         * longer universal across all versions.
         */
        @Deprecated
        POSITION_TEXTURE_COLOR_NORMAL(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
        ;

        public final VertexFormat mc;

        CommonVertexFormats(VertexFormat mc) {
            this.mc = mc;
        }
    }

    @Deprecated // see UGraphics.Globals
    public UGraphics beginWithActiveShader(DrawMode mode, CommonVertexFormats format) {
        return beginWithActiveShader(mode, format.mc);
    }

    @Deprecated // see UGraphics.Globals
    public UGraphics beginWithActiveShader(DrawMode mode, VertexFormat format) {
        return beginInternal(mode, format);
    }
    private UGraphics beginInternal(DrawMode mode, VertexFormat format) {
        vertexFormat = format;
        instance = new BufferBuilder(ALLOCATOR, mode.mcMode, format);
        return this;
    }

    @ApiStatus.Internal
    public static final Map<VertexFormat, String> DEFAULT_SHADERS = new HashMap<>();
    static {
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_NORMAL, "core/rendertype_lines");
        DEFAULT_SHADERS.put(DefaultVertexFormat.PARTICLE, "core/particle");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION, "core/position");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR, "core/position_color");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, "core/position_color_lightmap");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX, "core/position_tex");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX_COLOR, "core/position_tex_color");
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, "core/position_color_tex_lightmap");
    }

    @Deprecated // see UGraphics.Globals
    public UGraphics beginWithDefaultShader(DrawMode mode, CommonVertexFormats format) {
        return beginWithDefaultShader(mode, format.mc);
    }

    @Deprecated // see UGraphics.Globals
    public UGraphics beginWithDefaultShader(DrawMode mode, VertexFormat format) {
        return beginWithActiveShader(mode, format);
    }

    private RenderType renderLayer;
    public UGraphics beginRenderLayer(RenderType renderLayer) {
        this.renderLayer = renderLayer;
        beginInternal(DrawMode.fromRenderLayer(renderLayer), renderLayer.format());
        return this;
    }

    @Deprecated // use `beginWithDefaultShader` or `beginWithActiveShader` or `beginRenderLayer` instead
    public UGraphics begin(int glMode, CommonVertexFormats format) {
        return begin(glMode, format.mc);
    }

    @Deprecated // use `beginWithDefaultShader` or `beginWithActiveShader` or `beginRenderLayer` instead
    public UGraphics begin(int glMode, VertexFormat format) {
        beginWithDefaultShader(DrawMode.fromGl(glMode), format);
        return this;
    }

    public void drawDirect() {
        MeshData builtBuffer = instance.build();
        if (builtBuffer == null) return;
        if (renderLayer != null) {
            //? if <= 26.1 {
            /*renderLayer.draw(builtBuffer);
            *///? } else {
            BatchedGuiRenderer.INSTANCE.queue(renderLayer.prepare(), builtBuffer);
            //? }
            return;
        }
        doDraw(
            builtBuffer
        );
    }

    public void drawSorted(int cameraX, int cameraY, int cameraZ) {
        MeshData builtBuffer = instance.build();
        if (builtBuffer == null) return;
        builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
        if (renderLayer != null) {
            //? if <= 26.1 {
            /*renderLayer.draw(builtBuffer);
             *///? } else {
            BatchedGuiRenderer.INSTANCE.queue(renderLayer.prepare(), builtBuffer);
            //? }
            return;
        }
        // Sorting handled above.
        doDraw(
            builtBuffer
        );
    }


    private void doDraw(
        MeshData builtBuffer
    ) {
        throw new UnsupportedOperationException("Drawing via UGraphics on 1.21.5+ is only supported via `beginRenderLayer`. Use that or `UBufferBulider`/`URenderPipeline` instead.");
    }

    @Deprecated // Pass UMatrixStack as first arg, required for 1.17+
    public UGraphics pos(double x, double y, double z) {
        return pos(UNIT_STACK, x, y, z);
    }

    public UGraphics pos(UMatrixStack stack, double x, double y, double z) {
        if (stack == UNIT_STACK) {
            instance.addVertex((float) x, (float) y, (float) z);
        } else {
            instance.addVertex(stack.peek().getModel(), (float) x, (float) y, (float) z);
        }
        return this;
    }

    @Deprecated // Pass UMatrixStack as first arg, required for 1.17+
    public UGraphics norm(float x, float y, float z) {
        return norm(UNIT_STACK, x, y, z);
    }

    public UGraphics norm(UMatrixStack stack, float x, float y, float z) {
        if (stack == UNIT_STACK) {
            instance.setNormal(x, y, z);
        } else {
            Vector3f normal = stack.peek().getNormal().transform(x, y, z, new Vector3f());
            instance.setNormal(normal.x(), normal.y(), normal.z());
        }
        return this;
    }

    public UGraphics color(int red, int green, int blue, int alpha) {
        return color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    public UGraphics color(float red, float green, float blue, float alpha) {
        instance.setColor(red, green, blue, alpha);
        return this;
    }

    public UGraphics color(Color color) {
        return color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public UGraphics endVertex() {
        return this;
    }

    public UGraphics tex(double u, double v) {
        instance.setUv((float)u,(float)v);
        return this;
    }

    public UGraphics overlay(int u, int v) {
        instance.setUv1(u, v);
        return this;
    }

    public UGraphics light(int u, int v) {
        instance.setUv2(u, v);
        return this;
    }


    /**
     * Using UMatrixStack should be preferred for all versions as direct GL transforms will break in 1.17.
     *
     * These methods are no different than transformation methods in the UGraphics class except they are not deprecated
     * and as such can be used in version-specific code.
     */

    /**
     * Minecraft 1.21.5 switches to a more Vulkan-style rendering, that is, almost all state is now specified directly
     * as part of the draw call and most global OpenGL state is no longer useful because it is overwritten right before
     * each draw call.
     * <p>
     * The recommended replacement is to use {@link gg.essential.universal.render.URenderPipeline}
     * (via {@link gg.essential.universal.vertex.UBufferBuilder}) instead, which provides the same
     * all-state-is-declared-explicitly system for all versions.<br>
     * Note that unlike the vanilla {@code RenderLayer} system, which still uses the global
     * {@code RenderSystem.setShaderTexture}, but just like the vanilla {@code RenderPipeline},
     * {@code URenderPipeline} also requires textures to be set explicitly, despite {@code UGraphics.bindTexture} not
     * yet being deprecated (because it's still used for {@code RenderLayer}).
     * <br>
     * Update: As of 1.21.11, {@code UGraphics.bindTexture} is now deprecated too.
     * <p>
     * If you need to still use the old global state on versions prior to 1.21.5, you may use the methods declared in
     * this class. They are functionally identical to the ones in UGraphics but are not deprecated with the
     * understanding that they will only be used in explicitly version-dependent code.
     */
    public static class Globals {

    }
}
