<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="objects" tilewidth="48" tileheight="48" tilecount="2" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="hero/idle_down"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="HERO"/>
  </properties>
  <image width="16" height="16" source="objects/hero.png"/>
 </tile>
 <tile id="1" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="tree_green"/>
  </properties>
  <image width="48" height="48" source="objects/tree_green.png"/>
 </tile>
</tileset>
