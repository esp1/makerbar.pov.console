(ns makerbar.pov.ui.processing
  (:import [processing.core PApplet PGraphics]))


; Applet binding

(def ^:dynamic *applet* nil)
(defn ^PApplet current-applet [] *applet*)

(defmacro with-applet [applet & body]
  "Binds dynamic var to current applet."
  `(binding [*applet* ~applet]
     ~@body))

; Applet functions

(defn width [] (.-width (current-applet)))
(defn height [] (.-height (current-applet)))

(defn size
  ([width height] (.size (current-applet) width height)))

(defn display-width [] (.displayWidth (current-applet)))
(defn display-height [] (.displayHeight (current-applet)))

(defn create-graphics
  ([width height] (.createGraphics (current-applet) width height)))

(defn load-image [filename] (.loadImage (current-applet) filename))

(defn cursor
  ([c] (.cursor (current-applet) c)))

(defn frame-rate [r] (.frameRate (current-applet) r))

; Graphics binding

(def ^{:dynamic true
       :private true}
  *graphics* nil)

(defn
  ^{:requires-bindings true
    :category "Environment"
    :subcategory nil
    :added "2.0"
    :tag PGraphics}
  current-graphics
  "Graphics currently used for drawing. By default it is sketch graphics,
  but if called inside with-graphics macro - graphics passed to the macro
  is returned. This method should be used if you need to call some methods
  that are not implemented by quil. Example (.beginDraw (current-graphics))."
  []
  (or *graphics* (.-g (current-applet))))

(defmacro
  ^{:requires-bindings true
    :processing-name nil
    :category "Rendering"
    :added "1.7"}
  with-graphics
  "All subsequent calls of any drawing function will draw on given graphics.
  'with-graphics' cannot be nested (you can draw simultaneously only on 1 graphics)"
  [graphics & body]
  `(binding [*graphics* ~graphics]
     (.beginDraw ~graphics)
     ~@body
     (.endDraw ~graphics)))

; Graphics functions

(defn background
  ([gray] (.background (current-graphics) gray)))

(defmacro draw
  [& body]
  `(do
     (.beginDraw (current-graphics))
     ~@body
     (.endDraw (current-graphics))))

(defn fill
  ([r g b] (fill r g b 1))
  ([r g b a] (.fill (current-graphics) r g b (or a 1)))
  ([gray a] (fill gray gray gray a)))

(defn image
  ([img [x y]] (image img x y))
  ([img x y] (.image (current-graphics) img x y)))

(defn no-fill [] (.noFill (current-graphics)))

(defn rect
  ([x y width height] (.rect (current-graphics) x y width height)))

(defn scale
  ([s] (.scale (current-graphics) s))
  ([sx sy] (.scale (current-graphics) sx sy)))

(defn stroke
  ([r g b] (stroke r g b 1))
  ([r g b a] (.stroke (current-graphics) r g b a))
  ([gray] (stroke gray 1))
  ([gray a] (.stroke (current-graphics) gray)))

(defn text
  ([string x y] (.text (current-graphics) string (float x) (float y))))

(defn text-descent [] (.textDescent (current-graphics)))

(defn translate
  ([[x y]] (translate x y))
  ([x y] (.translate (current-graphics) (float x) (float y))))

(defmacro with-matrix
  [& body]
  `(do
     (.pushMatrix (current-graphics))
     ~@body
     (.popMatrix (current-graphics))))

(defmacro with-style
  [& body]
  `(do
     (.pushStyle (current-graphics))
     ~@body
     (.popStyle (current-graphics))))
