# minicosm
[![Clojars Project](https://img.shields.io/clojars/v/minicosm.svg)](https://clojars.org/minicosm) | [![cljdoc badge](https://cljdoc.org/badge/minicosm/minicosm)](https://cljdoc.org/d/minicosm/minicosm/CURRENT)



A simple functional-first game engine inspired by universe.rkt

## Concept

minicosm is a simple game engine inspired by Racket's "universe" library. minicosm provides a simple, functional, immutable game loop API, and an easy to use markup language based on canvas. The core mission is to create something easy to use, easy to learn, and purely functional from the API consumer's perspective, while handling all the messy business of canvas drawing behind the scenes.

A example game is available [on Github](https://github.com/jarcane/minicosm-demo), and playable on [itch.io](https://annarcana.itch.io/minicosm-demo)

## Getting started

You can generate a new minicosm project for yourself with [Leiningen](https://github.com/technomancy/leiningen) by running the following, where `myproject` is the name of your new game project.

```
lein new minicosm myproject
```

## Usage

The core function in minicosm is `minicosm.core/start!`. This function accepts a map of event handlers, and on evaluation attaches to a `<canvas>` element with the id of `game`, and initializes the core game loop.

The handler map should contain the following keys, each containing a function with the given expected behavior:

* `:init (fn [] state)` - 
    A function that returns the initial game state, run before the loop starts
* `:assets (fn [] assets)` -
     A function that returns a map of keys to vector pairs of a keyword for asset type (`:image/:audio`) and the url for the asset, and returns a map of the same keys to `Image`/`Audio` objects. These images will first be loaded into memory *before* the main game loop begins
* `:on-key (fn [state keys] state)` - 
    A function that takes the current game state, and a set of current key codes pressed, and returns a new game state
* `:on-tick (fn [state time] state)` -
    A function that takes the current game state, and a DOMHighResTimeStamp, indicating the number of ms since time origin (https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin). Runs every frame, approx. 60fps. Returns a new game state.
* `:to-play (fn [state assets sound-state] sound-state)` -
    A function that taks the current game state, assets, and the current sound-state
    and returns a map describing sounds to play, in the form:
    `{:music #{<sound assets to loop} :effects #{<sound assets to play once>}}`. If a sound is in the sound-state you receive, it is currently playing. Effects will be removed from the state when they finish - to loop a sound effect, add it as long as some condition holds. To stop a currently-playing sound, remove it from the sound-state. This will reset the sound, rather than pause it in-place.
* `:to-draw (fn [state assets] ddn)` -
    A function that takes the current game state and the image asset map, and returns a DDN vector of the graphics to be drawn. 

## DDN - the drawing language

DDN (Drawing Description Notation) is an EDN/Hiccup inspired declarative notation for specifying the graphics to be drawn on the canvas and their order. Similar to Hiccup syntax, each element is a vector. The first element is always a keyword indicating the element name, followed by a map of options, and finally any remaining contents of the element, if any.

An example from the demo: 
```clj
[:group {:desc "base"}
   [:map {:pos [0 0] :dim [32 24] :size 16} tilemap]
   [:sprite {:pos [x y]} (:ship assets)]
   [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
   [:group {:desc "shapes"}
    [:rect {:style :fill :pos [300 200] :dim [64 32] :color "white"}]
    [:rect {:pos [200 150] :dim [32 32] :color "white"}]
    [:circ {:pos [400 50] :r [32 32] :color "white"}]
    [:line {:from [200 64] :to [350 150] :color "white"}]
    [:point {:pos [45 100] :color "purple"}]]]
```

### DDN elements

DDN defines a number of common elements, described as follows, with their options.

* `:group` - A grouping of multiple elements to be drawn. This is useful both as the base element for an entire drawing, or for providing a single value that contains multiple elements to be drawn. The body should contain *either* 1 or more child elements which will be drawn sequentially to the canvas, *or* a list containing 1 or more child elements. `:group` will ignore any `nil?` elements in the body; this is to allow safe use of conditional contents. Options:
    - `:desc` (optional) - a string describing the nature of the group. useful for documentation purposes.
    - `:pos` (optional) - the coordinates at which to begin drawing the group, as a pair (ie. `[x y]`). If not provided assumes `[0 0]`. **When provided, the `:pos` of all children will be _relative_ to this point.**
    - `:scale` (optional) - a number by which the group will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - In radians, the extent of rotation of the entire group (_not_ children individually) clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the group will be rotated. Defaults to `[0 0]`.
* `:image` - A static image to be drawn. The contents of the element should be an image value. Options:
    - `:pos` (optional) - the coordinates at which to place the image, as a pair of coordinates (ie. `[x y]`). If not provided, assumes `[0 0]` coordinates.
    - `:scale` (optional) - a number by which the image will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the image will be rotated. Defaults to `[0 0]`.
    - `:view` (optional) - Defines a sub-rectangle of the image to be drawn, in the form of a pair of pairs of coordinates (ie. `[[x1 y1] [x2 y2]]). If not provided, the whole image is displayed (limited by the size of the canvas)
* `:map` - A tilemap to be drawn. The contents of the element should be a 2D array of sprites. Options:

    - `:dim` - the dimensions of the tile map in tiles, as a pair of width and height: `[w h]`.
    - `:size` - the dimensions of each tile in pixels, as an integer. Tiles are assumed to be square. 
    - `:view` (optional) - Defines a sub-section of the map to be drawn, as a pair of pairs of x/y coordinates (ie. `[[x1 y1] [x2 y2]]). Note that this is by pixel, not tile, to allow for smooth scrolling of partial tiles. If not provided the whole map is drawn.
    - `:pos` (optional) - the coordinates at which to begin drawing the map, as a pair (ie. `[x y]`). If not provided assumes `[0 0]`.
    - `:scale` (optional) - a number by which the map will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation of the entire map (_not_ tiles individually) clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the map will be rotated. Defaults to `[0 0]`.
* `:sprite` - A sprite to be drawn. Contents should contain an image value. Options:
    - `:pos` - A pair of coordinates at which to draw the sprite (ie. `[x y]`).
    - `:scale` (optional) - a number by which the sprite will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the sprite will be rotated. Defaults to `[0 0]`.
* `:text` - Indicates text to be drawn. Contents should be a string or series of strings. Options:
    - `:pos` - a pair indicating the x/y coordinates at which to begin drawing the text.
    - `:color`: a string containing the CSS color value of the text.
    - `:font`: a string containing the CSS font value for the text.
    - `:scale` (optional) - a number by which the text will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the text will be rotated. Defaults to `[0 0]`.
* `:rect` - A rectangle to be drawn on screen. Options:
    - `:pos` - a pair indicating the x/y coordinates of the origin point of the rectangle
    - `:dim` - a pair indicating the width and height (`[w h]`) of the rect
    - `:style` - One of either `:stroke` (line only) or `:fill` (filled rect)
    - `:color` - A CSS color value for the color of the rect
    - `:scale` (optional) - a number by which the rect will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the rect will be rotated. Defaults to `[0 0]`.
* `:circ` - A circle or ellipse. Options:
    - `:pos` - a pair indicating the x/y coords of the center of the circ
    - `:r` - A pair indicating the major/minor (horizontal/vertical) radius of the circ
    - `:style` - One of either `:stroke` (line only) or `:fill` (filled circ)
    - `:color` - A CSS color value for the color of the circ
    - `:scale` (optional) - a number by which the circ will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the circ will be rotated. Defaults to `[0 0]`.
* `:line` - A line to be drawn. Options:
    - `:from` - a pair of x/y coordinates for the origin of the line
    - `:to` - a pair of x/y coords for the endpoint of the line
    - `:width` - the width of the line in pixels
    - `:color` - a CSS color value for the color of the line
    - `:pos` (optional) - if provided, makes `:from` and `:to` **relative** to the provided point, rather than to the global canvas. Defaults to `[0 0]`.
    - `:scale` (optional) - a number by which the line will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the line will be rotated. Defaults to `[0 0]`.
* `:point` - A single pixel point. 
    - `:pos` - A pair of x/y coordinates (eg. `[45 12]`).
    - `:color` - A string containing a CSS color value.
    
* `:path` - A path defining a shape drawn with arbitrary lines. The body should be a vector of x/y pairs, starting from the origin point, and ending with the final point of the shape. 
    - `:width` - the width in pixels of the lines
    - `:color` - the CSS color value for the line/fill of the shape
    - `:style` - one of `:stroke`/`:fill`
    - `:pos` (optional) - if provided, makes the coordinates provided in the body **relative** to the provided point, rather than to the global canvas. Defaults to `[0 0]`.
    - `:scale` (optional) - a number by which the path will be uniformly scaled. Defaults to `1`.
    - `:rotate` (optional) - in radians, the extent of rotation clockwise around `:pivot`. Defaults to `0`.
    - `:pivot` (optional) - a point, **relative to `:pos`**, around which the path will be rotated. Defaults to `[0 0]`.

## Development

To get an interactive development environment run:

    `clj -M:fig:build`

A browser window will be launched running the test playground from `minicosm.demo`. Auto-reloading with Figwheel is enabled, however do note that because of peculiarities with the behavior of canvas elements and some of the internal JS event hooks, manual reloading on a change may still be necessary.

Whenever possible, this project seeks to stick with vanilla ClojureScript/JavaScript, without additional dependencies beyond ClojureScript itself.

## License

Copyright © 2020 A.C. Danvers

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
