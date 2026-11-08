package com.tsundere.mod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.UUID;

public class TsundereEntity extends PathAwareEntity {
    private UUID ownerUuid;
    private boolean following = true;
    private int affection = 0;

    private static final String[] FOLLOW = {
            "Hmpf! Na gut... aber glaub bloß nicht, dass ich das gerne mache!",
            "I-ich laufe dir nicht hinterher! Ich gehe zufällig denselben Weg, Baka!",
            "Tch... meinetwegen. Aber trödel nicht rum!"};
    private static final String[] STAY = {
            "Du schickst mich weg?! ...Na schön, dann bleib ich eben hier. Idiot!",
            "Fein! Ich brauche dich sowieso nicht! ...Komm aber bald wieder.",
            "Hmpf. Ich warte. A-aber nur, weil ich Lust dazu habe!"};
    private static final String[] TALK_COLD = {
            "Was willst du denn schon wieder? Ich hab keine Zeit für dich!",
            "Sprich nicht so mit mir, als wären wir Freunde, Baka!",
            "Hmpf. Was ist? Mach's kurz."};
    private static final String[] TALK_MID = {
            "D-du nervst... aber du bist okay, glaub ich. Ein bisschen.",
            "Das Wetter ist heute... n-nicht schlecht. Mit dir zusammen. Ich meine, allgemein!",
            "Wenn du verletzt wirst, ist das nur lästig für mich! Pass halt auf dich auf."};
    private static final String[] TALK_WARM = {
            "I-ich bin gern bei dir... Sag das niemandem, sonst ist es aus!",
            "Du bist mein Lieblings-Baka, okay? ...Vergiss, dass ich das gesagt habe!",
            "Bleib bei mir, ja? N-nicht dass ich dich vermissen würde oder so..."};

    public TsundereEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FollowOwnerGoal2(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    public PlayerEntity getOwner() {
        return ownerUuid == null ? null : this.getEntityWorld().getPlayerByUuid(ownerUuid);
    }

    public boolean isFollowing() { return following; }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) { return false; }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (player instanceof ServerPlayerEntity sp) {
            if (ownerUuid == null) {
                ownerUuid = player.getUuid();
                say(sp, "W-was starrst du mich so an?! ...Von mir aus darfst du mich begleiten.");
            }
            ServerPlayNetworking.send(sp, new OpenMenuPayload(this.getId()));
        }
        return ActionResult.SUCCESS;
    }

    /** 0 = folgen, 1 = stehen bleiben, 2 = reden, 3 = Geschenk (Essen in der Hand) */
    public void handleAction(ServerPlayerEntity p, int action) {
        switch (action) {
            case 0 -> { following = true; say(p, pick(FOLLOW)); }
            case 1 -> { following = false; getNavigation().stop(); say(p, pick(STAY)); }
            case 2 -> {
                affection = Math.min(affection + 1, 30);
                say(p, affection < 5 ? pick(TALK_COLD) : affection < 12 ? pick(TALK_MID) : pick(TALK_WARM));
            }
            case 3 -> {
                ItemStack s = p.getMainHandStack();
                if (s.isEmpty()) {
                    say(p, "Du hast ja gar nichts in der Hand, Baka!");
                } else if (s.contains(DataComponentTypes.FOOD)) {
                    if (!p.isCreative()) s.decrement(1);
                    affection = Math.min(affection + 2, 30);
                    heal(2.0f);
                    say(p, "D-das ist nicht für mich gedacht?! ...Danke. Aber bilde dir nichts darauf ein!");
                } else {
                    say(p, "Was soll ich denn damit?! Hmpf!");
                }
            }
            default -> {}
        }
    }

    private void say(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal("<Tsundere> " + msg), false);
    }

    private String pick(String[] arr) { return arr[this.random.nextInt(arr.length)]; }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (ownerUuid != null) view.putString("Owner", ownerUuid.toString());
        view.putBoolean("Following", following);
        view.putInt("Affection", affection);
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        view.getOptionalString("Owner").ifPresent(s -> {
            try { ownerUuid = UUID.fromString(s); } catch (IllegalArgumentException ignored) { }
        });
        following = view.getBoolean("Following", true);
        affection = view.getInt("Affection", 0);
    }

    /** Läuft dem Besitzer hinterher, bleibt bei ~2 Blöcken Abstand stehen. */
    static class FollowOwnerGoal2 extends Goal {
        private final TsundereEntity mob;
        private int cooldown;

        FollowOwnerGoal2(TsundereEntity mob) {
            this.mob = mob;
            setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override public boolean canStart() {
            PlayerEntity o = mob.getOwner();
            return mob.following && o != null && !o.isSpectator() && mob.squaredDistanceTo(o) > 9.0;
        }

        @Override public boolean shouldContinue() {
            PlayerEntity o = mob.getOwner();
            return mob.following && o != null && mob.squaredDistanceTo(o) > 4.0;
        }

        @Override public void tick() {
            PlayerEntity o = mob.getOwner();
            if (o == null) return;
            mob.getLookControl().lookAt(o, 10.0f, mob.getMaxLookPitchChange());
            if (--cooldown <= 0) {
                cooldown = 10;
                if (mob.squaredDistanceTo(o) > 576.0) { // > 24 Blöcke: teleportieren
                    mob.requestTeleport(o.getX(), o.getY(), o.getZ());
                    mob.getNavigation().stop();
                } else {
                    mob.getNavigation().startMovingTo(o, 1.2);
                }
            }
        }

        @Override public void stop() { mob.getNavigation().stop(); }
    }
}
