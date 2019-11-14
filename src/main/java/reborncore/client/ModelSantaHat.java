/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Date: 27/11/2016 17:33:21
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package reborncore.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class ModelSantaHat extends EntityModel<AbstractClientPlayerEntity> {
	private ModelPart hatband1;
	private ModelPart hatband2;
	private ModelPart hatband3;
	private ModelPart hatband4;
	private ModelPart hatbase1;
	private ModelPart hatband5;
	private ModelPart hatband6;
	private ModelPart hatbase2;
	private ModelPart hatextension1;
	private ModelPart hatextension2;
	private ModelPart hatextension3;
	private ModelPart hatextension4;
	private ModelPart hatball1;
	private ModelPart hatball2;
	private ModelPart hatball3;
	private ModelPart hatball4;
	private ModelPart hatball5;
	private ModelPart hatball6;

	public ModelSantaHat() {
		textureWidth = 64;
		textureHeight = 64;

		hatband1 = new ModelPart(this, 0, 32);
		hatband1.addCuboid(-4F, -8F, -5F, 8, 1, 1);
		hatband1.setPivot(0F, 0F, 0F);
		hatband1.setTextureSize(64, 64);
		hatband1.mirror = true;
		setRotation(hatband1, 0F, 0F, 0F);
		hatband2 = new ModelPart(this, 0, 32);
		hatband2.addCuboid(-4F, -8F, 4F, 8, 1, 1);
		hatband2.setPivot(0F, 0F, 0F);
		hatband2.setTextureSize(64, 64);
		hatband2.mirror = true;
		setRotation(hatband2, 0F, 0F, 0F);
		hatband3 = new ModelPart(this, 0, 34);
		hatband3.addCuboid(-5F, -8F, -4F, 1, 1, 8);
		hatband3.setPivot(0F, 0F, 0F);
		hatband3.setTextureSize(64, 64);
		hatband3.mirror = true;
		setRotation(hatband3, 0F, 0F, 0F);
		hatband4 = new ModelPart(this, 0, 34);
		hatband4.addCuboid(4F, -8F, -4F, 1, 1, 8);
		hatband4.setPivot(0F, 0F, 0F);
		hatband4.setTextureSize(64, 64);
		hatband4.mirror = true;
		setRotation(hatband4, 0F, 0F, 0F);
		hatbase1 = new ModelPart(this, 0, 43);
		hatbase1.addCuboid(-4F, -9F, -4F, 8, 1, 8);
		hatbase1.setPivot(0F, 0F, 0F);
		hatbase1.setTextureSize(64, 64);
		hatbase1.mirror = true;
		setRotation(hatbase1, 0F, 0F, 0F);
		hatband5 = new ModelPart(this, 18, 41);
		hatband5.addCuboid(0F, -7F, -5F, 4, 1, 1);
		hatband5.setPivot(0F, 0F, 0F);
		hatband5.setTextureSize(64, 64);
		hatband5.mirror = true;
		setRotation(hatband5, 0F, 0F, 0F);
		hatband6 = new ModelPart(this, 18, 41);
		hatband6.addCuboid(-4F, -7F, 0F, 4, 1, 1);
		hatband6.setPivot(0F, 0F, 4F);
		hatband6.setTextureSize(64, 64);
		hatband6.mirror = true;
		setRotation(hatband6, 0F, 0F, 0F);
		hatbase2 = new ModelPart(this, 18, 34);
		hatbase2.addCuboid(-3F, -10F, -3F, 6, 1, 6);
		hatbase2.setPivot(0F, 0F, 0F);
		hatbase2.setTextureSize(64, 64);
		hatbase2.mirror = true;
		setRotation(hatbase2, 0F, 0.1115358F, 0F);
		hatextension1 = new ModelPart(this, 0, 52);
		hatextension1.addCuboid(-3F, -11F, -2F, 4, 2, 4);
		hatextension1.setPivot(0F, 0F, 0F);
		hatextension1.setTextureSize(64, 64);
		hatextension1.mirror = true;
		setRotation(hatextension1, 0F, -0.0371786F, 0.0743572F);
		hatextension2 = new ModelPart(this, 16, 52);
		hatextension2.addCuboid(-2.4F, -12F, -1.5F, 3, 2, 3);
		hatextension2.setPivot(0F, 0F, 0F);
		hatextension2.setTextureSize(64, 64);
		hatextension2.mirror = true;
		setRotation(hatextension2, 0F, 0.0743572F, 0.0743572F);
		hatextension3 = new ModelPart(this, 28, 52);
		hatextension3.addCuboid(-3.5F, -13F, -1F, 2, 2, 2);
		hatextension3.setPivot(0F, 0F, 0F);
		hatextension3.setTextureSize(64, 64);
		hatextension3.mirror = true;
		setRotation(hatextension3, 0F, 0F, 0.2230717F);
		hatextension4 = new ModelPart(this, 0, 58);
		hatextension4.addCuboid(-13F, -6.6F, -1F, 2, 3, 2);
		hatextension4.setPivot(0F, 0F, 0F);
		hatextension4.setTextureSize(64, 64);
		hatextension4.mirror = true;
		setRotation(hatextension4, 0F, 0F, 1.264073F);
		hatball1 = new ModelPart(this, 8, 58);
		hatball1.addCuboid(2F, -14.4F, -1.001F, 2, 2, 2);
		hatball1.setPivot(0F, 0F, 0F);
		hatball1.setTextureSize(64, 64);
		hatball1.mirror = true;
		setRotation(hatball1, 0F, 0F, 0F);
		hatball2 = new ModelPart(this, 16, 57);
		hatball2.addCuboid(2.5F, -14.8F, -0.5F, 1, 1, 1);
		hatball2.setPivot(0F, 0F, 0F);
		hatball2.setTextureSize(64, 64);
		hatball2.mirror = true;
		setRotation(hatball2, 0F, 0F, 0F);
		hatball3 = new ModelPart(this, 16, 57);
		hatball3.addCuboid(2.5F, -13F, -0.5F, 1, 1, 1);
		hatball3.setPivot(0F, 0F, 0F);
		hatball3.setTextureSize(64, 64);
		hatball3.mirror = true;
		setRotation(hatball3, 0F, 0F, 0F);
		hatball4 = new ModelPart(this, 16, 57);
		hatball4.addCuboid(3.4F, -14F, -0.5F, 1, 1, 1);
		hatball4.setPivot(0F, 0F, 0F);
		hatball4.setTextureSize(64, 64);
		hatball4.mirror = true;
		setRotation(hatball4, 0F, 0F, 0F);
		hatball5 = new ModelPart(this, 16, 57);
		hatball5.addCuboid(2.5F, -14F, 0.4F, 1, 1, 1);
		hatball5.setPivot(0F, 0F, 0F);
		hatball5.setTextureSize(64, 64);
		hatball5.mirror = true;
		setRotation(hatball5, 0F, 0F, 0F);
		hatball6 = new ModelPart(this, 16, 57);
		hatball6.addCuboid(2.5F, -14F, -1.4F, 1, 1, 1);
		hatball6.setPivot(0F, 0F, 0F);
		hatball6.setTextureSize(64, 64);
		hatball6.mirror = true;
		setRotation(hatball6, 0F, 0F, 0F);
	}

	@Override
	public void setAngles(AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, float r, float g, float b) {
		final Sprite sprite = null;
		hatband1.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatband2.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatband3.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatband4.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatbase1.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatband5.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatband6.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatbase2.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatextension1.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatextension2.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatextension3.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatextension4.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball1.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball2.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball3.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball4.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball5.render(matrixStack, vertexConsumer, light, overlay, sprite);
		hatball6.render(matrixStack, vertexConsumer, light, overlay, sprite);
	}

	private void setRotation(ModelPart model, float x, float y, float z) {
		model.pitch = x;
		model.yaw = y;
		model.roll = z;
	}

	@Override
	public void accept(ModelPart modelPart) {

	}
}
