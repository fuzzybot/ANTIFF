package me.heirteir.antiff;

public enum Policy {
    /**
     * All entities are invisible by default. Only entities specifically made visible may be seen.
     */
    WHITELIST,
    
    /**
     * All entities are visible by default. An entity can only be hidden explicitly.
     */
    BLACKLIST
}
