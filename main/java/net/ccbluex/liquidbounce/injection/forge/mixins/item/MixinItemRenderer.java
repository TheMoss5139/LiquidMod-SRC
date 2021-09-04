/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.render.SwingAnimation;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinItemRenderer {

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;

    @Shadow
    @Final
    private RenderItem itemRenderer;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBlockTransformations();

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow
    protected abstract boolean isBlockTranslucent(Block p_isBlockTranslucent_1_);

    @Overwrite
    public void renderItem(EntityLivingBase p_renderItem_1_, ItemStack p_renderItem_2_, ItemCameraTransforms.TransformType p_renderItem_3_) {
        if (p_renderItem_2_ == null) return;
        if (p_renderItem_1_ instanceof EntityPlayerSP) {
            Item item = p_renderItem_2_.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();
            SwingAnimation swingmod = LiquidBounce.moduleManager.getSwingAnimation();
            float scale = this.itemRenderer.shouldRenderItemIn3D(p_renderItem_2_) ? 2.0f : 1.0f;
            if (swingmod.getState() && swingmod.getItemSize().get() != 1.0f) scale = swingmod.getItemSize().get();
            if (scale != 1.0f) {
                GlStateManager.scale(scale, scale, scale);
            }
            float x = swingmod.getItemx().get();
            float y = swingmod.getItemy().get();
            float z = swingmod.getItemz().get();
            GlStateManager.translate(x,y,z);
            if (this.itemRenderer.shouldRenderItemIn3D(p_renderItem_2_)) {
                if (this.isBlockTranslucent(block)) {
                    GlStateManager.depthMask(false);
                }
            }

            this.itemRenderer.renderItemModelForEntity(p_renderItem_2_, p_renderItem_1_, p_renderItem_3_);
            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }else {
            Item item = p_renderItem_2_.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();
            if (this.itemRenderer.shouldRenderItemIn3D(p_renderItem_2_)) {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                if (this.isBlockTranslucent(block)) {
                    GlStateManager.depthMask(false);
                }
            }

            this.itemRenderer.renderItemModelForEntity(p_renderItem_2_, p_renderItem_1_, p_renderItem_3_);
            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }
    }

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    public void blockmanager(float f,float f1, SwingAnimation swingmod) {
        AbstractClientPlayer abstractclientplayer = mc.thePlayer;
        float f6 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * 3.1));
        float var15 =MathHelper.sin(f1 * f1 * 3.1415927F);
        switch (swingmod.getMode().get()) {
            case "Liquidbounce":
                //LiquidBounce
                this.transformFirstPersonItem(f + 0.1F, f1);
                this.doBlockTransformations();
                GlStateManager.translate(-0.5F, 0.2F, 0.0F);
                break;
            case "None":
                //None
                this.avatar(f, f1);
                this.doBlockTransformations();
                break;
            case "Tap":
                //Tap
                this.tap(f, f1);
                this.doBlockTransformations();
                break;
            case"Tap2":
                //Tap2
                this.tap2(f, f1);
                this.doBlockTransformations();
                break;
            case"Vanilla":
                //Vanilla
                this.transformFirstPersonItem(0, f1);
                this.doBlockTransformations();
                break;
            case"Slide":
                //Slide
                this.sigma2(f * 0.2F, f1);
                this.doBlockTransformations();
                break;
            case"Sigma":
                //Sigma
                this.transformFirstPersonItem(f*0.5f, 0);
                GlStateManager.rotate(-var15 * 55 / 2.0F, -8.0F, -0.0F, 9.0F);
                GlStateManager.rotate(-var15 * 45, 1.0F, var15 / 2, -0.0F);
                this.doBlockTransformations();
                GL11.glTranslated(1.2, 0.3, 0.5);
                GL11.glTranslatef(-1, this.mc.thePlayer.isSneaking() ? -0.1F : -0.2F, 0.2F);
                break;
            case"Exhibition":
                //Exhibition
                GL11.glTranslated(-0.1D, 0.00D, 0.0D);
                this.transformFirstPersonItem(-0.45F, 0F);
                GlStateManager.rotate(-f6 * 60.0F / 2.0F, f6 / 2.0F, -0.0F, 4.0F);
                GlStateManager.rotate(-f6 * 30.0F, 1.0F, f6 / 2.0F, -0.0F);
                this.doBlockTransformations();
                break;
            case"Remix":
                //Remix
                GL11.glTranslated(-0.1D, 0.00D, 0.0D);
                this.transformFirstPersonItem(-0.45F, 0F);
                GlStateManager.rotate(-f6 * 60.0F / 2.0F, f6 / 2.0F, -0.0F, 4.0F);
                GlStateManager.rotate(-f6 * 40.0F, 1.0F, f6 / 2.0F, -0.0F);
                this.doBlockTransformations();
                break;
            case"1.8Slide":
                this.transformFirstPersonItem(f, 0.0F);
                GlStateManager.rotate(f1*30, 0.0F,0.0F,1.0F);
                this.doBlockTransformations();
                break;
            case"1.8Slide2":
                this.transformFirstPersonItem(f, 0.0F);
                GlStateManager.rotate(-f1*30, 0.0F,0.0F,1.0F);
                this.doBlockTransformations();
                break;
            case "Jello": {
                this.transformFirstPersonItem(0.0f, 0.0f);
                this.doBlockTransformations();
                final int alpha = (int)Math.min(255L, ((System.currentTimeMillis() % 255L > 127L) ? Math.abs(Math.abs(System.currentTimeMillis()) % 255L - 255L) : (System.currentTimeMillis() % 255L)) * 2L);
                GlStateManager.translate(0.3f, -0.0f, 0.4f);
                GlStateManager.rotate(0.0f, 0.0f, 0.0f, 1.0f);
                GlStateManager.translate(0.0f, 0.5f, 0.0f);
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, -1.0f);
                GlStateManager.translate(0.6f, 0.5f, 0.0f);
                GlStateManager.rotate(-90.0f, 1.0f, 0.0f, -1.0f);
                GlStateManager.rotate(-10.0f, 1.0f, 0.0f, -1.0f);
                GlStateManager.rotate(abstractclientplayer.isSwingInProgress ? (-alpha / 5.0f) : 1.0f, 1.0f, -0.0f, 1.0f);
                break;
            }
        }
    }
    /**
     * @author CCBlueX
     */
    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        AbstractClientPlayer abstractclientplayer = this.mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        this.rotateArroundXAndY(f2, f3);
        this.setLightMapFromPlayer(abstractclientplayer);
        this.rotateWithPlayerRotations((EntityPlayerSP) abstractclientplayer, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        if(this.itemToRender != null) {
            final KillAura killAura = LiquidBounce.moduleManager.getKillAura();
            final SwingAnimation swingmod = LiquidBounce.moduleManager.getSwingAnimation();
            if(this.itemToRender.getItem() instanceof net.minecraft.item.ItemMap) {
                this.renderItemMap(abstractclientplayer, f2, f, f1);
            } else if (abstractclientplayer.getItemInUseCount() > 0 || (itemToRender.getItem() instanceof ItemSword && killAura.getBlockingStatus())) {
                EnumAction enumaction = killAura.getBlockingStatus() ? EnumAction.BLOCK : this.itemToRender.getItemUseAction();

                switch(enumaction) {
                    case NONE:
                        this.transformFirstPersonItem(f, 0.0F);
                        break;
                    case EAT:
                    case DRINK:
                        this.performDrinking(abstractclientplayer, partialTicks);
                        this.transformFirstPersonItem(f, f1);
                        break;
                    case BLOCK:
                        blockmanager(f, f1, swingmod);
                        break;
                    case BOW:
                        this.transformFirstPersonItem(f, f1);
                        this.doBowTransformations(partialTicks, abstractclientplayer);
                }
            }else if (killAura.getState() && killAura.getTarget() != null && killAura.getFakeBlockValue().get()) {
                if (swingmod.getSwordonly().get()) {
                    if (itemToRender.getItem() instanceof ItemSword) {
                        blockmanager(f, f1, swingmod);
                    }else {
                        if (swingmod.getState() && swingmod.getSwing().get()) {
                        }else {
                            this.doItemUsedTransformations(f1);
                        }
                        this.transformFirstPersonItem(f, f1);
                    }
                }else {
                    blockmanager(f, f1, swingmod);
                }
            }else {
                if (swingmod.getState() && swingmod.getSwing().get()) {
                }else {
                    this.doItemUsedTransformations(f1);
                }
                this.transformFirstPersonItem(f, f1);
            }

            this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        }else if(!abstractclientplayer.isInvisible()) {
            this.renderPlayerArm(abstractclientplayer, f, f1);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderFireInFirstPerson(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = LiquidBounce.moduleManager.getAntiBlind();

        if(antiBlind.getState() && antiBlind.getFireEffect().get()) callbackInfo.cancel();
    }

    private void tap2(float var2, float swing) {
        float var3 = MathHelper.sin(swing * swing * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(swing) * (float) Math.PI);
        GlStateManager.translate(0.56F, -0.42F, -0.71999997F);
        GlStateManager.translate(0.0F,  var2 * -0.15F, 0.0F);
        GlStateManager.rotate(30 , 0.0F, 1.0F, 0.0F);
        //GlStateManager.rotate(var3*-20 , 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -30.0F, 0.0F, 1.0F, 0.0F);
        //  GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        //GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
//        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    private void sigma2(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.42F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-25.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0F, 0.37F, 0.17F);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-10.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0F, -0.27F, 0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(f1 * -40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }
    private void tap(float var2, float swingProgress) {
        float smooth = (swingProgress*0.8f - (swingProgress*swingProgress)*0.8f);
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F,  var2 * -0.15F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(smooth * -90.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.scale(0.37F, 0.37F, 0.37F);
    }
    private void avatar(float equipProgress, float swingProgress){
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -40.0F, 1.0F, 0.0F, 0.0F);
//        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }
}