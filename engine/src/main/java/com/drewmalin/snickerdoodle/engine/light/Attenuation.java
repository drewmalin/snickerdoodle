package com.drewmalin.snickerdoodle.engine.light;

public class Attenuation {

    private final float constant;
    private final float linear;
    private final float exponent;

    public Attenuation(final float constant, final float linear, final float exponent) {
        this.constant = constant;
        this.linear = linear;
        this.exponent = exponent;
    }

    public float getConstant() {
        return this.constant;
    }

    public float getLinear() {
        return this.linear;
    }

    public float getExponent() {
        return this.exponent;
    }
}
