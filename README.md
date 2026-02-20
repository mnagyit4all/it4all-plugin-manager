# IT4All Plugin Manager

Eclipse plugin, amely a `dropins` és `dropins/temp` mappák között mozgatva kezeli a korábban regisztrált pluginek aktiválását/inaktiválását.

## Fő funkciók

- Eclipse telepítési útvonal automatikus felismerése (`dropins` alapján).
- `dropins/temp` automatikus létrehozása induláskor.
- Temp mappa scan (plugin mappa vagy `.jar` fájl).
- Plugin meta olvasás (`Bundle-Name`), fallback fájlnévre.
- JSON registry egyetlen igaz forrásként (`fileName`, `realName`, `state`).
- Checkbox alapú UI:
  - bejelölve: `DROPINS`
  - nincs bejelölve: `TEMP`
- Apply csak módosult tételekre futtat fájlműveletet.
- Mozgatás ütközéskezeléssel (`SKIP` policy).
- Részleges hiba esetén rollback + registry konzisztencia-javítás.
- Sikeres Apply után kötelező restart üzenet.

## Projektstruktúra

- `src/it4all_plugin_manager/core`: core logika (scan, registry, service, IO)
- `src/it4all_plugin_manager/ui`: nézet, presenter, handler
- `src/it4all_plugin_manager/lifecycle`: bootstrap
- `META-INF/MANIFEST.MF`: bundle deklaráció
- `plugin.xml`: view + command + handler + menu/toolbar hozzárendelés

## Működési elv röviden

1. Induláskor a plugin felismeri az Eclipse gyökeret.
2. Létrehozza a `dropins/temp` mappát, ha még nem létezik.
3. Beolvassa a temp mappát és frissíti a registry-t (upsert).
4. A UI a registry tartalmát mutatja.
5. Apply esetén a módosult pluginokat mozgatja:
   - `TEMP -> DROPINS` (aktiválás)
   - `DROPINS -> TEMP` (inaktiválás)
6. Siker után restart szükséges.

## Telepítés és futtatás (fejlesztői)

1. Importáld a projektet Eclipse PDE plugin projektként.
2. Ellenőrizd, hogy a `MANIFEST.MF` és `plugin.xml` rendben betöltődik.
3. Indíts Eclipse Application-t (Run As > Eclipse Application).
4. Nyisd meg a nézetet:
   - Window menüből: **IT4All Plugin Manager**
   - vagy fő toolbar gombról.

## Használat

1. Másold a kezelni kívánt plugint a `dropins/temp` mappába.
2. Nyisd meg az IT4All Plugin Manager nézetet.
3. Pipáld be az aktiválni kívánt plugineket.
4. `Apply`.
5. Indítsd újra az Eclipse-t.

Inaktiválás:

1. Vedd ki a pipát az adott plugin elől.
2. `Apply`.
3. A plugin visszakerül `dropins/temp` mappába.
4. Indítsd újra az Eclipse-t.

## Ismert viselkedés

- A manager csak olyan plugint kezel, amely legalább egyszer megjelent a `dropins/temp` mappában.
- A közvetlenül `dropins` mappába másolt, korábban nem regisztrált plugin nem kerül felvételre új tételként.
- Ütközés esetén (célfájl létezik) a jelenlegi policy: `SKIP`.

## Hibaelhárítás

- **Path felismerési hiba induláskor**:
  - Ellenőrizd, hogy valóban Eclipse környezetből fut a plugin.
  - Ellenőrizd, hogy a `dropins` mappa létezik az Eclipse gyökérben.
- **Mozgatási hiba Apply közben**:
  - Ellenőrizd, hogy a plugin fájl nincs lockolva.
  - Nézd meg az Eclipse Error Logot a részletekért.
- **Sérült registry**:
  - A rendszer backupot készít (`.broken.bak`) és üres állapotra resetel.

## Rövid manuális checklist

- Első indulás: `dropins/temp` létrejön.
- Temp-be tett új plugin megjelenik a listában.
- Pipa be + Apply: plugin átkerül `dropins`-ba.
- Pipa ki + Apply: plugin visszakerül `dropins/temp`-be.
- Ütközés esetén nincs felülírás (`SKIP`).
- Apply után restart üzenet megjelenik.
