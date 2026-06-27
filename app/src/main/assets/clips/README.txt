Drop short looping demo clips here (e.g. pushup.mp4, squat.gif).

To wire a clip to an exercise, set its `clipAsset` in
app/src/main/java/com/buildbygod/data/local/SeedData.kt to the path relative
to the assets folder, e.g. "clips/pushup.mp4". It will then play inline via
ExoPlayer on the exercise detail and session screens. When clipAsset is null,
a glossy placeholder is shown and the "Watch full video" YouTube link is used.
