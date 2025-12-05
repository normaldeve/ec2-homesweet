import { POPULAR_KEYWORDS } from "../keywords/popular_keywords.js";
import { MID_KEYWORDS } from "../keywords/mid_keywords.js";
import { LONGTAIL_KEYWORDS } from "../keywords/longtail_keywords.js";

export function pickKeyword() {
    const r = Math.random();

    if (r < 0.6) {
        return POPULAR_KEYWORDS[Math.floor(Math.random() * POPULAR_KEYWORDS.length)];
    }
    if (r < 0.9) {
        return MID_KEYWORDS[Math.floor(Math.random() * MID_KEYWORDS.length)];
    }
    return LONGTAIL_KEYWORDS[Math.floor(Math.random() * LONGTAIL_KEYWORDS.length)];
}
