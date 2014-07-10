package me.heirteir.antiff.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Class that contains all api functions from npc's in NPCFactory
 * 
 * @author lenis0012
 */
public interface NPC {

    /**
     * Get an org.bukkit.entity.Player instance from the npc.
     * 
     * @return NPC's player instance.
     */
    public Player getBukkitEntity();

    /**
     * Check wether or not an npc can get damaged
     * 
     * @return NPC can be damaged?
     */
    public boolean isInvulnerable();

    /**
     * Set wether or not an npc can get damaged
     * 
     * @param invulnerable
     *            NPC can be damaged?
     */
    public void setInvulnerable(boolean invulnerable);

    /**
     * Check wether or not an npc has gravity enabled.
     * 
     * @return NPC had grvity?
     */
    public boolean isGravity();

    /**
     * Set wether or not an npc has gravity enabled.
     * 
     * @param gravity
     *            NPC has gravity?
     */
    public void setGravity(boolean gravity);

    /**
     * Make NPC look at an entity.
     * 
     * @param target
     *            Entity to look at
     */
    public void setTarget(Entity target);

    /**
     * Get the entity the ncp is looking at
     * 
     * @return Entity npc is looking at (null if not found)
     */
    public Entity getTarget();

    /**
     * Make npc look at a certain location
     * 
     * @param location
     *            Location to look at
     */
    public void lookAt(Location location);

    /**
     * Change npc's yaw the proper way
     * 
     * @param yaw
     *            New npc yaw
     */
    public void setYaw(float yaw);

    /**
     * Get if an npc with collide with other entities.
     * 
     * @param location
     *            Get entity collision for npc.
     * @return
     */
    public boolean getEntityCollision();

    /**
     * Set if an npc with collide with other entities.
     * 
     * @param location
     *            Set entity collision for npc.
     * @return
     */
    public void setEntityCollision(boolean entityCollision);

}
