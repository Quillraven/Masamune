[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-red.svg)](http://kotlinlang.org/)
[![Fleks](https://img.shields.io/badge/Fleks-2.11-purple.svg)](https://github.com/Quillraven/Fleks)

[![LibGDX](https://img.shields.io/badge/LibGDX-1.13.1-green.svg)](https://libgdx.com/)
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
<p float="left">
    <img src="https://github.com/user-attachments/assets/ac47d508-1ce7-40bb-90e3-a4faa4378b5b" width="500">
    <img src="https://github.com/user-attachments/assets/1a2200d2-22de-4738-9f1e-fa286e9b8f71" width="500">
</p>
<p float="left">
    <img src="https://github.com/user-attachments/assets/7a2eceb5-9542-4b50-8f1a-8823c0438cfb" width="500">
    <img src="https://github.com/user-attachments/assets/c8553500-47d4-4b89-a535-6b720c19039a" width="500">
</p>
<p float="left">
    <img src="https://github.com/user-attachments/assets/2af86b28-4783-4d0a-a95e-93101c96e079" width="500">
    <img src="https://github.com/user-attachments/assets/4fdfbfea-f67a-436d-ae9f-0d1e015d7950" width="500">
</p>

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
- [8-Bit Fantasy Adventure Music Pack](https://xdeviruchi.itch.io/8-bit-fantasy-adventure-music-pack)
- [Pixel Arial 11 font](https://www.dafont.com/de/search.php?q=pixel+arial+11)
- [Fipps font](https://www.dafont.com/de/search.php?q=fipps)
- [Typing label](https://github.com/rafaskb/typing-label)
- [RPG Icon Pack](https://clockworkraven.itch.io/free-rpg-icon-pack-100-accessories-and-armor-clockwork-raven-studios)
- [JRPG Collection](https://opengameart.org/content/jrpg-collection)
- [Minifantasy Dungeon SFX Pack](https://leohpaz.itch.io/minifantasy-dungeon-sfx-pack)
- [jsfxr](https://sfxr.me/)
- [Effect and FX Pixel Part 12](https://bdragon1727.itch.io/effect-and-fx-pixel-part-12)
- [Effect and FX Pixel Part 13](https://bdragon1727.itch.io/effect-and-fx-pixel-part-13)
