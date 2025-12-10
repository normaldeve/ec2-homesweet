import { POPULAR_KEYWORDS } from "../keywords/popular_keywords.js";
import { MID_KEYWORDS } from "../keywords/mid_keywords.js";

const WEIGHTED_TYPES = [
    "POPULAR", "POPULAR", "POPULAR", "POPULAR", "POPULAR", "POPULAR",
    "MID", "MID", "MID", "MID"
];

let index = 0;

export function pickKeyword() {
    const type = WEIGHTED_TYPES[index];

    // 인덱스 순환
    index = (index + 1) % WEIGHTED_TYPES.length;

    switch (type) {
        case "POPULAR":
            return POPULAR_KEYWORDS[Math.floor(Math.random() * POPULAR_KEYWORDS.length)];
        case "MID":
            return MID_KEYWORDS[Math.floor(Math.random() * MID_KEYWORDS.length)];
    }
}