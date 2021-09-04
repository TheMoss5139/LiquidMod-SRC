/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow
    protected FoodStats foodStats = new FoodStats();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract String getSwimSound();

    @Shadow
    public abstract FoodStats getFoodStats();

    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int getItemInUseDuration();

    @Shadow
    public abstract ItemStack getItemInUse();

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public InventoryPlayer inventory = new InventoryPlayer((EntityPlayer) (Object)this);

    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (ForgeHooks.onPlayerAttackTarget((EntityPlayer) (Object)this, targetEntity)) {
            if (targetEntity.canAttackWithItem() && !targetEntity.hitByEntity((EntityPlayer) (Object)this)) {
                float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;
                if (targetEntity instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                }

                i = i + EnchantmentHelper.getKnockbackModifier((EntityPlayer) (Object)this);
                if (this.isSprinting()) {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;
                    if (flag && f > 0.0F) {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier((EntityLivingBase) (Object)this);
                    if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                        flag1 = true;
                        targetEntity.setFire(1);
                    }

                    double d0 = targetEntity.motionX;
                    double d1 = targetEntity.motionY;
                    double d2 = targetEntity.motionZ;
                    boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object)this), f);
                    if (flag2) {
                        if (i > 0) {
                            Velocity velocity = LiquidBounce.moduleManager.getVelocity();
                            //Superknockback Reduce
                            if (targetEntity instanceof EntityPlayerSP) {
                                if (velocity.getRemoveSuperKnockValue().get()) {

                                }else {
                                    targetEntity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F));
                                }
                            }
                            //AACV5Bypass
//                            if (((EntityPlayerSP) (Object) this).hurtTime > 0) {
//                                if (velocity.getAacreduce_sprint_value().get()) {
//                                    ((EntityPlayerSP) (Object) this).setSprinting(false);
//                                }
//
//                                if (velocity.getAacreduce_nomotion_value().get()) {
//                                    this.motionX *= 0.6;
//                                    this.motionZ *= 0.6;
//                                }
//                            }
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP)targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = d0;
                            targetEntity.motionY = d1;
                            targetEntity.motionZ = d2;
                        }

                        if (flag) {
                            this.onCriticalHit(targetEntity);
                        }

                        if (f1 > 0.0F) {
                            this.onEnchantmentCritical(targetEntity);
                        }

                        if (f >= 18.0F) {
                            this.triggerAchievement(AchievementList.overkill);
                        }

                        this.setLastAttacker(targetEntity);
                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, (Entity) (Object)this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) (Object)this, targetEntity);
                        ItemStack itemstack = this.getCurrentEquippedItem();
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;
                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase)ientitymultipart;
                            }
                        }

                        if (itemstack != null && entity instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase)entity, (EntityPlayer) (Object)this);
                            if (itemstack.stackSize <= 0) {
                                this.destroyCurrentEquippedItem();
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            this.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));
                            if (j > 0) {
                                targetEntity.setFire(j * 4);
                            }
                        }

                        this.addExhaustion(0.3F);
                    } else if (flag1) {
                        targetEntity.extinguish();
                    }
                }
            }

        }
    }

    @Shadow
    public void addExhaustion(float p_71020_1_) {
        if (!this.capabilities.disableDamage && !this.worldObj.isRemote) {
            this.foodStats.addExhaustion(p_71020_1_);
        }

    }

    @Shadow
    public void onCriticalHit(Entity entityHit) {
    }

    @Shadow
    public void onEnchantmentCritical(Entity entityHit) {
    }

    @Shadow
    public void triggerAchievement(StatBase achievementIn) {
        this.addStat(achievementIn, 1);
    }

    @Shadow
    public void addStat(StatBase stat, int amount) {
    }

    @Shadow
    public ItemStack getCurrentEquippedItem() {
        return this.inventory.getCurrentItem();
    }

    @Shadow
    public void destroyCurrentEquippedItem() {
        ItemStack orig = this.getCurrentEquippedItem();
        this.inventory.setInventorySlotContents(this.inventory.currentItem, (ItemStack)null);
        ForgeEventFactory.onPlayerDestroyItem((EntityPlayer) (Object)this, orig);
    }
}