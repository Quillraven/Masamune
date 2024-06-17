[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-red.svg)](http://kotlinlang.org/)
[![Fleks](https://img.shields.io/badge/Fleks-2.7-purple.svg)](https://github.com/Quillraven/Fleks)

[![LibGDX](https://img.shields.io/badge/LibGDX-1.12.1-green.svg)](https://libgdx.com/)
[![LibKTX](https://img.shields.io/badge/LibKTX-1.12.1--SNAPSHOT-blue.svg)](https://github.com/libktx/ktx)
[![Tiled](https://img.shields.io/badge/Tiled-1.10.2-teal.svg)](https://www.mapeditor.org/)

# TBD

### Credits

- [ImageMagick](https://imagemagick.org/index.php)
  ```
  // split Idle.png and Walk.png into single frames
  magick '.\Idle.png' -crop 16x16 'idle_%02d.png'; magick '.\Walk.png' -crop 16x16 'walk_%02d.png'
  
  // combine images together in a tile layout
  // in this example two 16x16 images are combined into a single image with 1 image per row and 2 rows in total (=-tile 1x2)
  magick montage .\FlagBlue16x16.png .\FlagGray16x16.png -tile 1x2 -geometry 16x16+0+0< -background none Flags.png
  ```
- [Ninja Adventure Asset Pack](https://pixel-boy.itch.io/ninja-adventure-asset-pack)
- [Pixel Arial 11 font](https://www.dafont.com/de/search.php?q=pixel+arial+11)
- [Fipps font](https://www.dafont.com/de/search.php?q=fipps)
- [Typing label](https://github.com/rafaskb/typing-label)
