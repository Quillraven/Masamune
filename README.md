[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-red.svg)](http://kotlinlang.org/)
[![Fleks](https://img.shields.io/badge/Fleks-2.10-purple.svg)](https://github.com/Quillraven/Fleks)

[![LibGDX](https://img.shields.io/badge/LibGDX-1.13.0-green.svg)](https://libgdx.com/)
[![LibKTX](https://img.shields.io/badge/LibKTX-1.12.1--rc2-blue.svg)](https://github.com/libktx/ktx)
[![Tiled](https://img.shields.io/badge/Tiled-1.11.0-teal.svg)](https://www.mapeditor.org/)

# Masamune

Imagine an epic story here. Will be updated later.

### Controls

- Move with W/A/S/D
- Interact with SPACE
- Open menu with CTRL_LEFT
- Close/Cancel menu with CTRL_LEFT or ESCAPE

### Screenshots

![image](https://github.com/user-attachments/assets/ac47d508-1ce7-40bb-90e3-a4faa4378b5b)
![image](https://github.com/user-attachments/assets/1a2200d2-22de-4738-9f1e-fa286e9b8f71)
![image](https://github.com/user-attachments/assets/7a2eceb5-9542-4b50-8f1a-8823c0438cfb)


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
- [RPG Icon Pack](https://clockworkraven.itch.io/free-rpg-icon-pack-100-accessories-and-armor-clockwork-raven-studios)
