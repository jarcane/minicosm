# minicosm

A simple functional-first game engine inspired by universe.rkt

## Concept

minicosm is a simple game engine inspired by Racket's "universe" library. minicosm provides a simple, functional, immutable game loop API, and an easy to use markup language based on canvas. The core mission is to create something easy to use, easy to learn, and purely functional from the API consumer's perspective, while handling all the messy business of canvas drawing behind the scenes.

## Usage

The core function in minicosm is `minicosm.core/start!`. This function accepts a map of event handlers, and on evaluation attaches to a `<canvas>` element with the id of `game`, and initializes the core game loop.

The handler map should contain the following keys, each containing a function with the given expected behavior:

* `:init (fn [] state)` - 
    A function that returns the initial game state, run before the loop starts
* `:assets (fn [] assets)` -
     A function that returns a map of keys to image asset urls, and returns a map of the same keys to `Image` objects. These images will first be loaded into memory *before* the main game loop begins
* `:on-key (fn [state keys] state)` - 
    A function that takes the current game state, and a set of current key codes pressed, and returns a new game state
* `:on-tick (fn [state time] state)` -
    A function that takes the current game state, and a DOMHighResTimeStamp, indicating the number of ms since time origin (https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin). Runs every frame, approx. 60fps. Returns a new game state.
* `:to-draw (fn [state assets] ddn)` -
    A function that takes the current game state and the image asset map, and returns a DDN vector of the graphics to be drawn. 

## DDN - the drawing language

DDN (Drawing Description Notation) is an EDN/Hiccup inspired declarative notation for specifying the graphics to be drawn on the canvas and their order. Similar to Hiccup syntax, each element is a vector. The first element is always a keyword indicating the element name, followed by a map of options, and finally any remaining contents of the element, if any.

An example from the demo: 
```clj
[:canvas {}
 [:map {:pos [0 0] :dim [32 24] :size 16} tilemap]
 [:sprite {:pos [x y]} sprite]
 [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
 [:group {:desc "lines"}
  [:rect {:style :fill :pos [300 200] :dim [64 32] :color "white"}]
  [:rect {:pos [200 150] :dim [32 32] :color "white"}]
  [:circ {:pos [400 50] :r [32 32] :color "white"}]
  [:line {:from [200 64] :to [350 150] :color "white"}]]]
```

### DDN elements

DDN defines a number of common elements, described as follows, with their options.

* `:canvas` - The root element of the canvas itself. Has no options, but contains multiple child elements which will be drawn sequentially to the canvas. The `:to-draw` handler given to `minicosm.core/start!` expects to find one of these.
* `:group` - A grouping of multiple elements to be drawn. This is primarily useful for providing a single value that can be returned by functions, containing a series of elements to be drawn. Options:
    - `:desc` (optional) - a string describing the nature of the group. useful for documentation purposes.
* `:image` - A static image to be drawn. The contents of the element should be an image value. Options:
    - `:pos` (optional) - the coordinates at which to place the image, as a pair of coordinates (ie. `[x y]`). If not provided, assumes `[0 0]` coordinates.
    - `:view` (optional) - Defines a sub-rectangle of the image to be drawn, in the form of a pair of pairs of coordinates (ie. `[[x1 y1] [x2 y2]]). If not provided, the whole image is displayed (limited by the size of the canvas)
* `:map` - A tilemap to be drawn. The contents of the element should be a 2D array of sprites. Options:
    - `:pos` (optional) - the coordinates at which to begin drawing the map, as a pair (ie. `[x y]`). If not provided assumes `[0 0]`.
    - `:dim` - the dimensions of the tile map in tiles, as a pair of width and height: `[w h]`.
    - `:size` - the dimensions of each tile in pixels, as an integer. Tiles are assumed to be square.
    - `:view` (optional) - Defines a sub-section of the map to be drawn, as a pair of pairs of x/y coordinates (ie. `[[x1 y1] [x2 y2]]). Note that this is by pixel, not tile, to allow for smooth scrolling of partial tiles. If not provided the whole map is drawn.
* `:sprite` - A sprite to be drawn. Contents should contain an image value. Options:
    - `:pos` - A pair of coordinates at which to draw the sprite (ie. `[x y]`).
* `:text` - Indicates text to be drawn. Contents should be a string or series of strings. Options:
    - `:pos` - a pair indicating the x/y coordinates at which to begin drawing the text.
    - `:color`: a string containing the CSS color value of the text.
    - `:font`: a string containing the CSS font value for the text.
* `:rect` - A rectangle to be drawn on screen. Options:
    - `:pos` - a pair indicating the x/y coordinates of the origin point of the rectangle
    - `:dim` - a pair indicating the width and height (`[w h]`) of the rect
    - `:style` - One of either `:stroke` (line only) or `:fill` (filled rect)
    - `:color` - A CSS color value for the color of the rect
* `:circ` - A circle or ellipse. Options:
    - `:pos` - a pair indicating the x/y coords of the center of the circ
    - `:r` - A pair indicating the major/minor (horizontal/vertical) radius of the circ
    - `:style` - One of either `:stroke` (line only) or `:fill` (filled circ)
    - `:color` - A CSS color value for the color of the circ
* `:line` - A line to be drawn. Options:
    - `:from` - a pair of x/y coordinates for the origin of the line
    - `:to` - a pair of x/y coords for the endpoint of the line
    - `:width` - the width of the line in pixels
    - `:color` - a CSS color value for the color of the line
* `:point` - A single pixel point. 
    - `:pos` - A pair of x/y coordinates (eg. `[45 12]`).
    - `:color` - A string containing a CSS color value.

## Development

To get an interactive development environment run:

    lein figwheel

A browser window will be launched running the test demo from `minicosm.demo`. Auto-reloading with Figwheel is enabled, however do note that because of peculiarities with the behavior of canvas elements and some of the internal JS event hooks, manual reloading on a change may still be necessary.

## License

Copyright © 2020 A.C. Danvers

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
