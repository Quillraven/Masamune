<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.0" name="objects" tilewidth="16" tileheight="16" tilecount="9" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="MapObject">
  <properties>
   <property name="animationType" propertytype="AnimationType" value="IDLE"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="hero"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="objType" propertytype="MapObjectType" value="HERO"/>
   <property name="speed" type="float" value="5"/>
   <property name="stats" type="class" propertytype="Stats">
    <properties>
     <property name="agility" type="float" value="6"/>
     <property name="arcaneStrike" type="float" value="0.1"/>
     <property name="constitution" type="float" value="5"/>
     <property name="criticalStrike" type="float" value="0.2"/>
     <property name="intelligence" type="float" value="8"/>
     <property name="life" type="float" value="60"/>
     <property name="lifeMax" type="float" value="60"/>
     <property name="mana" type="float" value="20"/>
     <property name="manaMax" type="float" value="20"/>
     <property name="strength" type="float" value="10"/>
    </properties>
   </property>
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
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="elder"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value=""/>
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
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="flower_girl"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value=""/>
   <property name="objType" propertytype="MapObjectType" value="FLOWER_GIRL"/>
   <property name="speed" type="float" value="3.5"/>
   <property name="triggerName" value="flower_girl"/>
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
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="merchant"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value=""/>
   <property name="objType" propertytype="MapObjectType" value="MERCHANT"/>
   <property name="speed" type="float" value="3"/>
   <property name="triggerName" value="merchant"/>
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
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="smith"/>
   <property name="bodyType" propertytype="BodyType" value="DynamicBody"/>
   <property name="dialogName" value=""/>
   <property name="objType" propertytype="MapObjectType" value="SMITH"/>
   <property name="speed" type="float" value="3"/>
   <property name="triggerName" value="smith"/>
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
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="butterfly"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
   <property name="objType" propertytype="MapObjectType" value="ENEMY"/>
  </properties>
  <image source="objects/butterfly.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="0" y="1" width="16" height="15">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="7" type="MapObject">
  <properties>
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="larva"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
   <property name="objType" propertytype="MapObjectType" value="ENEMY"/>
  </properties>
  <image source="objects/larva.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="1" y="0" width="14" height="15">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="8" type="MapObject">
  <properties>
   <property name="animationType" propertytype="AnimationType" value="WALK"/>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="mushroom"/>
   <property name="bodyType" propertytype="BodyType" value="KinematicBody"/>
   <property name="objType" propertytype="MapObjectType" value="ENEMY"/>
  </properties>
  <image source="objects/mushroom.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" type="FixtureDefinition" x="0" y="0" width="16" height="16">
    <properties>
     <property name="density" type="float" value="50000"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="9" type="ItemObject">
  <properties>
   <property name="atlas" propertytype="AtlasAsset" value="CHARS_AND_PROPS"/>
   <property name="atlasRegionKey" value="items/elder_sword"/>
   <property name="itemType" propertytype="ItemType" value="ELDER_SWORD"/>
   <property name="stats" type="class" propertytype="Stats">
    <properties>
     <property name="damage" type="float" value="3"/>
     <property name="intelligence" type="float" value="1"/>
     <property name="physicalDamage" type="float" value="3"/>
    </properties>
   </property>
  </properties>
  <image source="objects/elder_sword.png" width="6" height="15"/>
 </tile>
</tileset>
