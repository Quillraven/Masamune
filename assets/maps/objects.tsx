<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="objects" tilewidth="48" tileheight="48" tilecount="6" columns="0">
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
 <tile id="2" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="elder/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="elder_00"/>
   <property name="hasAnimation" type="bool" value="true"/>
   <property name="objType" propertytype="MapObjectType" value="ELDER"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <image width="16" height="16" source="objects/elder.png"/>
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
  <image width="16" height="16" source="objects/flower_girl.png"/>
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
  <image width="16" height="16" source="objects/merchant.png"/>
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
  <image width="16" height="16" source="objects/smith.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="2" y="13" width="12" height="3">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
</tileset>
