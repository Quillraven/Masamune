<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="interactTestObjects" tilewidth="16" tileheight="16" tilecount="4" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="hero/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="objType" propertytype="MapObjectType" value="HERO"/>
  </properties>
  <image width="16" height="16" source="objects/hero.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="-54" y="-54" width="128" height="128">
    <properties>
     <property name="isSensor" type="bool" value="true"/>
     <property name="userData" value="interact"/>
    </properties>
    <ellipse/>
   </object>
   <object id="2" type="FixtureDefinition" x="1" y="13" width="14" height="3"/>
  </objectgroup>
 </tile>
 <tile id="1" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="smith/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="smith_00"/>
   <property name="objType" propertytype="MapObjectType" value="SMITH"/>
  </properties>
  <image width="16" height="16" source="objects/smith.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="1" y="13" width="15" height="3"/>
  </objectgroup>
 </tile>
 <tile id="2" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="merchant/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="merchant_00"/>
   <property name="objType" propertytype="MapObjectType" value="MERCHANT"/>
  </properties>
  <image width="16" height="16" source="objects/merchant.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="13" width="16" height="3"/>
  </objectgroup>
 </tile>
 <tile id="3" type="MapObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="flower_girl/idle_down"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value="flower_girl_00"/>
   <property name="objType" propertytype="MapObjectType" value="FLOWER_GIRL"/>
  </properties>
  <image width="16" height="16" source="objects/flower_girl.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="13" width="16" height="3"/>
  </objectgroup>
 </tile>
</tileset>
