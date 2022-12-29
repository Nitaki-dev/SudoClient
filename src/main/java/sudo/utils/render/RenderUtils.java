package sudo.utils.render;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import sudo.mixins.accessors.WorldRendererAccessor;

public class RenderUtils {
	public static MinecraftClient mc = MinecraftClient.getInstance();


    @SuppressWarnings("resource")
    public static Frustum getFrustum() {
        return ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).getFrustum();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double rad, double samples) { 
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer(); 
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR); 
 
        double toX1 = toX - rad; 
        double toY1 = toY - rad; 
        double fromX1 = fromX + rad; 
        double fromY1 = fromY + rad; 
        double[][] map = new double[][]{new double[]{toX1, toY1}, new double[]{toX1, fromY1}, new double[]{fromX1, fromY1}, new double[]{fromX1, toY1}}; 
        for (int i = 0; i < 4; i++) { 
            double[] current = map[i]; 
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) { 
                float rad1 = (float) Math.toRadians(r); 
                float sin = (float) (Math.sin(rad1) * rad); 
                float cos = (float) (Math.cos(rad1) * rad); 
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next(); 
            } 
        } 
        BufferRenderer.drawWithShader(bufferBuilder.end()); 
    }
	
    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double rad, double samples) { 
        int color = c.getRGB(); 
        Matrix4f matrix = matrices.peek().getPositionMatrix(); 
        float f = (float) (color >> 24 & 255) / 255.0F; 
        float g = (float) (color >> 16 & 255) / 255.0F; 
        float h = (float) (color >> 8 & 255) / 255.0F; 
        float k = (float) (color & 255) / 255.0F; 
        RenderSystem.enableBlend(); 
        RenderSystem.disableTexture(); 
        RenderSystem.setShader(GameRenderer::getPositionColorShader); 
 
        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, samples); 
 
        RenderSystem.enableTexture(); 
        RenderSystem.disableBlend(); 
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f); 
    }

    public static void setup2DRender(boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
    }

    public static void end2DRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public static void setup3DRender(boolean disableDepth) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public static void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    public static void fill(MatrixStack matrixStack, double x1, double y1, double x2, double y2, int color) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        double j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(g, h, k, f).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @SuppressWarnings("resource")
    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static void drawOutlineCircle(MatrixStack matrices, double xx, double yy, double radius, Color color) {
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderUtils.bindTexture(new Identifier("hypnotic", "textures/outlinecircle.png"));
        RenderSystem.enableBlend();
        RenderUtils.drawTexture(matrices, (float) xx,(float) yy, (float)radius, (float)radius, 0, 0, (float)radius,(float) radius,(float) radius,(float) radius);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void bindTexture(Identifier identifier) {
        RenderSystem.setShaderTexture(0, identifier);
    }

    public static void drawTexture(MatrixStack matrices, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        RenderSystem.enableBlend();
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.disableBlend();
    }

    public static void drawTexture(MatrixStack matrices, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public static void drawTexture(MatrixStack matrices, float x0, float y0, float x1, float y1, int z, float regionWidth, float regionHeight, float u, float v, float textureWidth, float textureHeight) {
        drawTexturedQuad(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    public static void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public static void drawBoxFill(Box box, QuadColor color, Direction... excludeDirs) {
        if (!getFrustum().isVisible(box)) {
            return;
        }

        setup3DRender(true);

        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Fill
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vertexer1.vertexBoxQuads(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
        tessellator.draw();

        end3DRender();
    }

    public static void drawBoxOutline(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
        if (!getFrustum().isVisible(box)) {
            return;
        }

        setup3DRender(true);

        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Outline
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(lineWidth);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer1.vertexBoxLines(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
        tessellator.draw();

        RenderSystem.enableCull();

        end3DRender();
    }

	 public static void drawFilledBox(MatrixStack matrixStack, Box bb, Color color, boolean draw) {
	        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
	        Color color1 = color;
	        setup3DRender(true);
	        
	        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
	        if (draw)
	        	bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
	        float minX = (float)bb.minX;
	        float minY = (float)bb.minY;
	        float minZ = (float)bb.minZ;
	        float maxX = (float)bb.maxX;
	        float maxY = (float)bb.maxY;
	        float maxZ = (float)bb.maxZ;

	        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

	        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

	        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

	        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

	        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

	        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
	        if (draw) {
		        BufferRenderer.drawWithShader(bufferBuilder.end());
	        }
	        end3DRender();
	    }
		
		public static Vec3d getEntityRenderPosition(Entity entity, double partial) {
		        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - mc.getEntityRenderDispatcher().camera.getPos().x;
		        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - mc.getEntityRenderDispatcher().camera.getPos().y;
		        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - mc.getEntityRenderDispatcher().camera.getPos().z;
		        return new Vec3d(x, y, z);
		}

    public static void drawEntityBox(MatrixStack matrixstack, Entity entity, double x, double y, double z, Color color) {
        setup3DRender(true);
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, -1, 0), 0, true));
        matrixstack.translate(-x, -y, -z);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);


        drawFilledBox(matrixstack, bb, new Color(color.getRed(), color.getGreen(), color.getBlue(), 130), true);
        RenderSystem.lineWidth(1.5f);

        //drawOutlineBox(matrixstack, bb, color, true);

        end3DRender();
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, 1, 0), 0, true));
        matrixstack.translate(-x, -y, -z);
    }

    public static void drawText(Text text, double x, double y, double z, double scale, boolean fill) {
        drawText(text, x, y, z, 0, 0, scale, fill);
    }

    public static Vec3d getInterpolationOffset(Entity e) {
        if (MinecraftClient.getInstance().isPaused()) {
            return Vec3d.ZERO;
        }

        double tickDelta = MinecraftClient.getInstance().getTickDelta();
        return new Vec3d(
                e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
                e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
                e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
    }

    public static void drawText(Text text, double x, double y, double z, double offX, double offY, double scale, boolean fill) {
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        if (fill) {
            int opacity = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.6F) * 255.0F) << 24;
            mc.textRenderer.draw(text, -halfWidth, 0f, 553648127, false, matrices.peek().getPositionMatrix(), immediate, true, opacity, 0xf000f0);
            immediate.draw();
        } else {
            matrices.push();
            matrices.translate(1, 1, 0);
            immediate.draw();
            matrices.pop();
        }

        mc.textRenderer.draw(text, -halfWidth, 0f, -1, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
    }

}
