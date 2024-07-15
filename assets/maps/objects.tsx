<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.0" name="objects" tilewidth="16" tileheight="16" tilecount="8" columns="0">
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
  <image source="objects/hero.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="2" type="FixtureDefinition" x="2" y="13" width="12" height="3">
    <ellipse/>
   </object>
   <object id="4" type="FixtureDefinition" x="-8" y="-8" width="32" height="32">
    <properties>
     <property name="isSensor" type="bool" value="true"/>
     <property name="userData" value="interact"/>
    </properties>
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="2" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="elder/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value=""/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="ELDER"/>
   <property name="speed" type="float" value="2"/>
   <property name="triggerName" value="elder"/>
  </properties>
  <image source="objects/elder.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="1" y="12" width="14" height="4">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="flower_girl/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="flower_girl_00"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="FLOWER_GIRL"/>
   <property name="speed" type="float" value="3.5"/>
  </properties>
  <image source="objects/flower_girl.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="2" y="13" width="12" height="3">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="4" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="merchant/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="merchant_00"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="MERCHANT"/>
   <property name="speed" type="float" value="3"/>
  </properties>
  <image source="objects/merchant.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="2" y="12" width="12" height="4">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="5" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="smith/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="smith_00"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="SMITH"/>
   <property name="speed" type="float" value="3"/>
  </properties>
  <image source="objects/smith.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="2" y="13" width="12" height="3">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="6" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="butterfly/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
  </properties>
  <image source="objects/butterfly.png" width="16" height="16"/>
 </tile>
 <tile id="7" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="larva/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
  </properties>
  <image source="objects/larva.png" width="16" height="16"/>
 </tile>
 <tile id="8" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="mushroom/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
  </properties>
  <image source="objects/mushroom.png" width="16" height="16"/>
 </tile>
</tileset>
