<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="objects" tilewidth="48" tileheight="48" tilecount="3" columns="0">
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
 <tile id="2" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="hero/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="HERO"/>
  </properties>
  <image width="16" height="16" source="objects/hero.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="14" width="16" height="2"/>
   <object id="2" x="0" y="0" width="5" height="5">
    <ellipse/>
   </object>
   <object id="3" x="0" y="11">
    <polyline points="0,0 16,0"/>
   </object>
   <object id="4" x="1" y="9">
    <polygon points="0,0 14,0 13,-2 11,-1 8,-2 5,-1 2,-2"/>
   </object>
   <object id="5" x="6" y="1" width="10" height="4">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
</tileset>
