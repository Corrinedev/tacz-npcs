package com.corrinedev.tacznpcs.common.entity;

public enum Rank {
    ROOKIE("Rookie", 0),
    EXPERIENCED("Experienced", 1),
    VETERAN("Veteran", 2),
    EXPERT("Expert", 3);
    public final String rankname;
    public final int id;
    Rank(String name, int identifier) {
        rankname = name;
        id = identifier;
    }

    @Override
    public String toString() {
        return rankname;
    }
}
