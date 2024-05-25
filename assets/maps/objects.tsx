<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="objects" tilewidth="48" tileheight="48" tilecount="2" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="hero/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="HERO"/>
   <property name="speed" type="float" value="5"/>
  </properties>
  <image width="16" height="16" source="objects/hero.png"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="2" y="13" width="12" height="3">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="1" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="tree_green"/>
   <property name="bodyType" propertytype="BodyType" value="StaticBody"/>
  </properties>
  <image width="48" height="48" source="objects/tree_green.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="16" y="36" width="16" height="12"/>
  </objectgroup>
 </tile>
</tileset>
