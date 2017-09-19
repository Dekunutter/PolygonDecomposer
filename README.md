# PolygonDecomposer
Parses images into Box2d polygons

Takes in simple 2D images. Please use small images (like 100x100) pixels. Something you'd expect from a retro 2D game. It will work with larger images but in its current state it is slow.
Performs the following tasks:
- Accepts an image as input
- Traces a pixel-perfect output of the image, trimming off any fully transparent alpha pixels in the image
- Performs some simplification algorithms to simplify the shape into something more generalized, with less vertices to compute. This is controlled via several parameters you can alter
- Decomposes the resulting points into a bunch of triangles
- Recomposes all the triangles into polygons that make up the full body of the shape (since polygons need to be convex)
- Outputs the result in JSON which can be read by Box2d projects if you set up a parser

Overall it works pretty well but it can crash on odd shapes and its quite slow. I haven't worked on this in years but I decided to put it on GitHub in case there was interest.

Do not have a list of sources or references anymore as its been too long. Apologies for that, can fill them in later if I find them.
