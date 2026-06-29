#!/usr/bin/env python3
"""Turn the bundled 2-frame exercise demos into smooth, natural-looking loops.

Each source clip in assets/clips is a 2-frame animated WebP (start + end pose from the
public-domain free-exercise-db). This script:

  1. Estimates dense optical flow between the two poses (Farneback).
  2. Synthesises N in-between frames by flow-warping + blending both poses, so the body
     appears to move continuously instead of snapping between 2 frames.
  3. Builds a ping-pong loop (start -> end -> start) with eased timing for a realistic rep.
  4. Highlights the working muscle: the pixels that move the most between poses are exactly
     the body part doing the work, so we paint a soft, pulsing warm glow over that region.

Output is written as an animated WebP with the same filename. Resolution/quality are tuned
to keep the per-clip size small so the APK stays reasonable.

Usage:
  python3 tools/animate_clips.py --limit 6 --out /tmp/clip_preview     # sample run
  python3 tools/animate_clips.py --inplace                              # regenerate all
"""
import argparse
import glob
import os

import cv2
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(ROOT, "app", "src", "main", "assets", "clips")

# Highlight colour for the working muscle (warm "heat", RGB).
HEAT = np.array([255, 120, 40], dtype=np.float32)


def load_two_frames(path):
    """Return the first and last frame of an animated WebP as RGB float arrays."""
    im = Image.open(path)
    n = getattr(im, "n_frames", 1)
    im.seek(0)
    a = im.convert("RGB")
    im.seek(n - 1)
    b = im.convert("RGB")
    return np.asarray(a, dtype=np.float32), np.asarray(b, dtype=np.float32)


def smoothstep(t):
    return t * t * (3.0 - 2.0 * t)


def warp(img, flow):
    """Backward-warp img by flow (sample img at grid+flow)."""
    h, w = img.shape[:2]
    gx, gy = np.meshgrid(np.arange(w, dtype=np.float32), np.arange(h, dtype=np.float32))
    mx = gx + flow[..., 0]
    my = gy + flow[..., 1]
    return cv2.remap(img, mx, my, interpolation=cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)


def make_frames(a, b, n_half, scale):
    if scale != 1.0:
        a = cv2.resize(a, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)
        b = cv2.resize(b, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)

    h, w = a.shape[:2]
    ga = cv2.cvtColor(a.astype(np.uint8), cv2.COLOR_RGB2GRAY)
    gb = cv2.cvtColor(b.astype(np.uint8), cv2.COLOR_RGB2GRAY)

    fkw = dict(pyr_scale=0.5, levels=5, winsize=35, iterations=5,
               poly_n=7, poly_sigma=1.5, flags=0)
    flow_ab = cv2.calcOpticalFlowFarneback(ga, gb, None, **fkw)  # a -> b
    flow_ba = cv2.calcOpticalFlowFarneback(gb, ga, None, **fkw)  # b -> a
    # Smooth the flow fields to suppress speckle warping artifacts.
    flow_ab = cv2.GaussianBlur(flow_ab, (0, 0), sigmaX=3)
    flow_ba = cv2.GaussianBlur(flow_ba, (0, 0), sigmaX=3)

    # Locate the working region: where the body moves most between the two poses.
    mag = np.linalg.norm(flow_ab, axis=2)
    thr = max(np.percentile(mag, 90.0), 1.0)
    strong = np.maximum(mag - thr, 0)
    glow_mask = np.zeros((h, w), np.float32)
    if strong.sum() > 1e-3:
        ys, xs = np.mgrid[0:h, 0:w]
        wsum = strong.sum()
        cx = float((xs * strong).sum() / wsum)
        cy = float((ys * strong).sum() / wsum)
        sx = float(np.sqrt(((xs - cx) ** 2 * strong).sum() / wsum))
        sy = float(np.sqrt(((ys - cy) ** 2 * strong).sum() / wsum))
        rx = max(sx * 1.6, w * 0.12)
        ry = max(sy * 1.6, h * 0.12)
        # Soft radial spotlight on the working area (no harsh per-pixel tint).
        glow_mask = np.exp(-(((xs - cx) / rx) ** 2 + ((ys - cy) / ry) ** 2))
        glow_mask = glow_mask.astype(np.float32)
    glow_mask = glow_mask[..., None]
    heat = HEAT[None, None, :] / 255.0

    forward = []
    for i in range(n_half + 1):
        t = smoothstep(i / n_half)
        wa = warp(a, t * flow_ab)
        wb = warp(b, (1.0 - t) * flow_ba)
        inter = np.clip((1.0 - t) * wa + t * wb, 0, 255) / 255.0

        # Pulsing warm spotlight, screen-blended so detail/texture is preserved.
        pulse = 0.32 + 0.38 * t
        g = glow_mask * pulse
        inter = 1.0 - (1.0 - inter) * (1.0 - heat * g)
        forward.append(np.clip(inter * 255.0, 0, 255).astype(np.uint8))

    # Ping-pong: a->b then b->a, dropping shared endpoints to avoid stutter.
    frames = forward + forward[-2:0:-1]
    return [Image.fromarray(f) for f in frames]


def encode(frames, out_path, quality, fps):
    dur = int(1000 / fps)
    frames[0].save(
        out_path, format="WEBP", save_all=True, append_images=frames[1:],
        duration=dur, loop=0, quality=quality, method=4,
    )


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=SRC_DIR)
    ap.add_argument("--out", default=None, help="output dir (default: temp preview)")
    ap.add_argument("--inplace", action="store_true", help="overwrite source clips")
    ap.add_argument("--limit", type=int, default=0, help="only first N clips (0 = all)")
    ap.add_argument("--frames", type=int, default=14, help="frames per half-rep (loop ~= 2x)")
    ap.add_argument("--scale", type=float, default=1.0)
    ap.add_argument("--quality", type=int, default=55)
    ap.add_argument("--fps", type=int, default=18)
    args = ap.parse_args()

    out_dir = args.src if args.inplace else (args.out or "/tmp/clip_preview")
    os.makedirs(out_dir, exist_ok=True)

    files = sorted(glob.glob(os.path.join(args.src, "*.webp")))
    if args.limit:
        files = files[: args.limit]

    total_in = total_out = 0
    for idx, path in enumerate(files, 1):
        name = os.path.basename(path)
        try:
            a, b = load_two_frames(path)
            frames = make_frames(a, b, args.frames, args.scale)
            out_path = os.path.join(out_dir, name)
            encode(frames, out_path, args.quality, args.fps)
            total_in += os.path.getsize(path)
            total_out += os.path.getsize(out_path)
            if idx % 50 == 0 or args.limit:
                print(f"[{idx}/{len(files)}] {name}: {len(frames)} frames, "
                      f"{os.path.getsize(out_path)//1024} KB")
        except Exception as e:  # noqa: BLE001
            print(f"!! {name}: {e}")

    print(f"\nDone. {len(files)} clips. in={total_in//1024//1024}MB "
          f"out~={total_out//1024//1024}MB")


if __name__ == "__main__":
    main()
