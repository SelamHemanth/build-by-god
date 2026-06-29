#!/usr/bin/env python3
"""Add a derived `difficulty` (BEGINNER/INTERMEDIATE/ADVANCED) to each exercise.

The bundled free-exercise-db has no difficulty, so we infer one from the
exercise type, equipment and name keywords. This is heuristic but gives the
library a sensible beginner -> advanced progression inside each muscle group.
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(ROOT, "app", "src", "main", "assets", "exercises.json")

ADVANCED_KW = [
    "muscle up", "muscle-up", "planche", "front lever", "back lever", "human flag",
    "one arm", "one-arm", "single arm", "one leg", "one-leg", "single leg",
    "pistol", "snatch", "clean and jerk", "clean & jerk", "power clean", "hang clean",
    "jerk", "overhead squat", "handstand", "skin the cat", "l-sit", "l sit",
    "dragon", "iron cross", "windmill", "turkish", "pull over", "pullover",
    "deficit", "explosive", "plyo", "clap", "archer", "typewriter", "ring",
    "deadlift", "thruster", "burpee", "pistol squat", "sumo deadlift",
]

BEGINNER_KW = [
    "wall", "knee", "kneeling", "seated", "lying", "machine", "assisted",
    "march", "step-up", "step up", "calf raise", "glute bridge", "bridge",
    "bird dog", "dead bug", "plank", "crunch", "sit-up", "sit up", "leg raise",
    "mountain climber", "air squat", "bodyweight squat", "incline push",
    "stretch", "foam roll", "band ", "resistance band",
]


def difficulty(ex):
    name = ex.get("name", "").lower()
    typ = ex.get("type", "MAIN")
    equip = ex.get("equipment", "BODYWEIGHT")

    if typ in ("WARMUP", "STRETCH"):
        return "BEGINNER"

    if any(k in name for k in ADVANCED_KW):
        return "ADVANCED"

    if equip == "MACHINE" or any(k in name for k in BEGINNER_KW):
        return "BEGINNER"

    if equip in ("BARBELL", "KETTLEBELL"):
        return "ADVANCED" if any(k in name for k in ("clean", "snatch", "jerk", "squat", "deadlift")) else "INTERMEDIATE"

    if equip == "BODYWEIGHT":
        # simple compound bodyweight = beginner, otherwise intermediate
        if any(k in name for k in ("push-up", "pushup", "push up", "squat", "lunge", "dip")):
            return "BEGINNER"
        return "INTERMEDIATE"

    return "INTERMEDIATE"


def main():
    data = json.load(open(PATH))
    counts = {"BEGINNER": 0, "INTERMEDIATE": 0, "ADVANCED": 0}
    for ex in data:
        d = difficulty(ex)
        ex["difficulty"] = d
        counts[d] += 1
    json.dump(data, open(PATH, "w"), ensure_ascii=False, indent=0)
    print("counts:", counts, "total:", len(data))


if __name__ == "__main__":
    main()
