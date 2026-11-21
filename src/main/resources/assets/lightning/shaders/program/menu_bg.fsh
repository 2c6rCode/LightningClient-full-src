#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MenuBgConfig {
    vec4 Params; // x: Time, y: Alpha, z: unused, w: unused
};

in vec2 texCoord;
out vec4 fragColor;

// Lightning - blue storm background: thunderclouds, drifting mist, forked lightning

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), u.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm4(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * vnoise(p);
        p = p * 2.03 + vec2(7.1, 3.7);
        a *= 0.5;
    }
    return v;
}

// one layer of procedural points of light (stars / sparks)
vec3 starLayer(vec2 uv, float scale, float density, float t, float seed, vec3 tint) {
    vec2 g = uv * scale;
    vec2 id = floor(g);
    vec2 f = fract(g) - 0.5;

    float h = hash21(id + seed);
    float present = step(h, density);

    vec2 off = vec2(hash21(id + seed + 11.3), hash21(id + seed + 27.7)) - 0.5;
    vec2 p = f - off * 0.5;
    float d = length(p);

    float sz = mix(0.025, 0.075, hash21(id + seed + 5.1));
    float tw = 0.65 + 0.35 * sin(t * (0.8 + 2.5 * hash21(id + seed + 9.9)) + h * 60.0);

    float core = exp(-(d * d) / (sz * sz * 0.35));
    float halo = exp(-d / (sz * 2.0)) * 0.16;
    float win = smoothstep(0.0, 0.30, 0.5 - max(abs(f.x), abs(f.y)));

    return tint * (core + halo) * win * tw * present;
}

// one forked bolt: returns (core, glow, envelope)
vec3 strike(vec2 uv, float lt, float id, float start) {
    float dt1 = lt - start;
    float dt2 = lt - start - 0.14;
    float dt3 = lt - start - 0.30;
    float env = 0.0;
    if (dt1 > 0.0) env += exp(-dt1 * 6.0);
    if (dt2 > 0.0) env += 0.8 * exp(-dt2 * 8.0);
    if (dt3 > 0.0) env += 0.5 * exp(-dt3 * 10.0);
    env = min(env, 1.0);
    env *= 0.78 + 0.22 * step(0.5, fract(lt * 26.0));
    if (env < 0.003) return vec3(0.0);

    float bx = (hash11(id * 3.17 + 1.0) - 0.5) * 1.5;
    float y = uv.y;

    float disp = (vnoise(vec2(y * 3.0, id * 5.3)) - 0.5) * 0.40
               + (vnoise(vec2(y * 9.0, id * 2.9 + 7.0)) - 0.5) * 0.14
               + (vnoise(vec2(y * 22.0, id * 1.7 + 3.0)) - 0.5) * 0.04;
    float x0 = bx + disp;

    float d = abs(uv.x - x0);
    float endY = -0.25 + hash11(id * 9.7) * 0.25;
    float mask = (1.0 - smoothstep(0.50, 0.58, y)) * smoothstep(endY - 0.10, endY + 0.08, y);

    float core = exp(-d * 300.0) * mask;
    float glow = (0.55 * exp(-d * 34.0) + 0.25 * exp(-d * 9.0)) * mask;

    // two side forks
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float yb = 0.25 - hash11(id * 4.1 + fk * 3.7) * 0.40;
        float side = hash11(id * 6.6 + fk * 1.9) > 0.5 ? 1.0 : -1.0;
        float xb = bx + (vnoise(vec2(yb * 3.0, id * 5.3)) - 0.5) * 0.40
                      + (vnoise(vec2(yb * 9.0, id * 2.9 + 7.0)) - 0.5) * 0.14
                      + (vnoise(vec2(yb * 22.0, id * 1.7 + 3.0)) - 0.5) * 0.04;
        float by = yb - y;
        float x1 = xb + side * by * 0.55
                 + (vnoise(vec2(y * 14.0, id * 8.1 + fk * 4.0)) - 0.5) * 0.06 * smoothstep(0.0, 0.12, by);
        float db = abs(uv.x - x1);
        float bmask = step(0.0, by) * (1.0 - smoothstep(0.05, 0.30, by));
        core += exp(-db * 340.0) * bmask * 0.7;
        glow += 0.35 * exp(-db * 40.0) * bmask * 0.7;
    }

    return vec3(core * env, glow * env, env);
}

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * OutSize) / OutSize.y;
    float t = Params.x;

    // storm timing: two strikes per 5 second cycle
    float cid = floor(t / 5.0);
    float lt = t - cid * 5.0;
    float idA = cid * 2.0;
    float idB = cid * 2.0 + 1.0;
    vec3 sa = strike(uv, lt, idA, 0.5 + hash11(cid * 7.31) * 1.2);
    vec3 sb = strike(uv, lt, idB, 2.4 + hash11(cid * 5.17) * 1.6);
    float flash = max(sa.z, sb.z * 0.85);
    float bxA = (hash11(idA * 3.17 + 1.0) - 0.5) * 1.5;
    float bxB = (hash11(idB * 3.17 + 1.0) - 0.5) * 1.5;
    float lit = sa.z * exp(-(uv.x - bxA) * (uv.x - bxA) / 0.45)
              + sb.z * 0.85 * exp(-(uv.x - bxB) * (uv.x - bxB) / 0.45);
    lit = min(lit, 1.0);

    // deep blue night sky with a faint glow near the horizon
    float r = length(uv * vec2(0.8, 1.0));
    vec3 col = mix(vec3(0.014, 0.040, 0.110), vec3(0.003, 0.008, 0.030), smoothstep(0.0, 1.1, r));
    col += vec3(0.020, 0.070, 0.160) * (1.0 - smoothstep(-0.5, 0.1, uv.y)) * 0.8;

    // sparse cold stars
    vec3 st = vec3(0.0);
    st += starLayer(uv + vec2(t * 0.0030, 0.0), 24.0, 0.25, t, 1.0, vec3(0.70, 0.85, 1.00)) * 0.55;
    st += starLayer(uv + vec2(t * 0.0060, 0.0), 9.0, 0.18, t, 41.0, vec3(0.65, 0.85, 1.00));
    col += st;

    // thunderclouds: warped fbm drifting sideways, lit from within by lightning
    vec2 p = uv * vec2(1.0, 1.5) + vec2(t * 0.022, t * 0.004);
    vec2 q = vec2(fbm4(p), fbm4(p + vec2(5.2, 1.3)));
    float n = fbm4(p + 1.8 * q + vec2(t * 0.012, 0.0));
    float dens = smoothstep(0.32, 0.80, n);
    dens *= mix(0.55, 1.0, smoothstep(-0.3, 0.45, uv.y));
    vec3 cloudCol = mix(vec3(0.016, 0.040, 0.090), vec3(0.070, 0.190, 0.420), smoothstep(0.40, 0.85, n));
    float amb = 0.14 * (0.5 + 0.5 * sin(t * 0.8 + q.x * 7.0));
    cloudCol += vec3(0.25, 0.50, 1.00) * amb * n;
    cloudCol += vec3(0.30, 0.55, 1.00) * lit * (0.25 + 0.75 * n);
    col = mix(col, cloudCol, dens * 0.92);

    // drifting mist / smoke, thicker near the bottom
    vec2 sp = vec2(uv.x * 1.2 + t * 0.040, uv.y * 2.2 - t * 0.010);
    float w = fbm4(sp * 0.8 + vec2(t * 0.02, 0.0));
    float sm = fbm4(sp + 2.2 * vec2(w, w * 0.6));
    float mist = smoothstep(0.25, 0.75, sm);
    float mistMask = 0.25 + 0.75 * (1.0 - smoothstep(-0.45, 0.35, uv.y));
    vec3 mistCol = vec3(0.17, 0.36, 0.66) + vec3(0.30, 0.50, 0.80) * lit * 0.8;
    col += mistCol * mist * mistMask * 0.80;

    // electric sparks drifting upward
    col += starLayer(uv + vec2(0.0, -t * 0.035), 11.0, 0.12, t * 2.0, 77.0, vec3(0.35, 0.80, 1.00)) * 0.9;

    // keep the menu area calm so text stays readable
    vec2 c = uv * vec2(1.0, 1.35);
    col *= 1.0 - exp(-dot(c, c) / 0.10) * 0.30;

    // vignette
    float vig = 1.0 - smoothstep(0.50, 1.15, length(uv * vec2(0.9, 1.15)));
    col *= mix(0.45, 1.0, vig);

    // thunder & lightning: sky flash + glow + hot core
    col += vec3(0.05, 0.14, 0.32) * flash * (0.30 + 0.70 * smoothstep(-0.5, 0.5, uv.y)) * 0.8;
    float core = sa.x + sb.x * 0.85;
    float glow = sa.y + sb.y * 0.85;
    col += vec3(0.20, 0.50, 1.00) * glow * 0.9;
    col += vec3(0.88, 0.96, 1.00) * core * 1.5;

    // film grain
    col += (hash21(gl_FragCoord.xy + fract(t)) - 0.5) * 0.010;

    fragColor = vec4(clamp(col, 0.0, 1.0), Params.y);
}
