# TODO – Eclipse dropins/temp alapú plugin manager

## 1) Eclipse mappa automatikus felismerése

- [ ] Implementáld az Eclipse telepítési útvonal automatikus felismerését (futó alkalmazás helyéből)
- [ ] Ebből képezd a célmappákat: `dropins` és `dropins/temp`
- [ ] Ha a `dropins/temp` nem létezik, hozd létre induláskor
- [ ] Ha az automatikus felismerés sikertelen, jelenjen meg egy hibaüzenet és ne történjen fájlművelet

## 2) Forrás pluginok felismerése a temp mappából

- [ ] A `dropins/temp` tartalmát induláskor olvasd be
- [ ] Ismerd fel a plugin fájlok nevét (jar/folder név mint fájlnév)
- [ ] Derítsd ki a plugin "real name" értékét a plugin metából (pl. Bundle-Name)
- [ ] Kezeld azt az esetet is, ha a real name nem olvasható (fallback: fájlnév)

## 3) JSON registry (egyetlen igaz forrás)

- [ ] Készíts JSON registry fájlt a manager adataihoz
- [ ] Egy rekord mezői: `fileName`, `realName`, `state`
- [ ] A `state` csak két értéket vehet fel: `TEMP` vagy `DROPINS`
- [ ] A registry csak azokat a pluginokat tartalmazza, amelyek legalább egyszer már a `dropins/temp` mappában voltak
- [ ] Registry frissítés szabály: temp scan után upsert, törlés csak explicit cleanup lépésben

## 4) Regisztrációs szabály (fix whitelist viselkedés)

- [ ] Implementáld a fix szabályt: kizárólag korábban temp-ben látott plugin kapcsolható
- [ ] Új plugin csak úgy kerülhet listába, ha fizikailag bekerült egyszer a `dropins/temp` mappába
- [ ] A manager ne kezeljen olyan plugint, ami közvetlenül került a `dropins` mappába temp érintés nélkül

## 5) UI viselkedés checkbox alapján

- [ ] A listában a plugin neve (`realName`) és fájlneve (`fileName`) is jelenjen meg
- [ ] Checkbox állapot jelentése: bejelölve = `DROPINS`, nincs bejelölve = `TEMP`
- [ ] Alkalmazáskor csak a módosult tételekre történjen fájlművelet

## 6) Fájlművelet logika (átmozgatás)

- [ ] Bekapcsoláskor mozgasd a plugint `dropins/temp` → `dropins`
- [ ] Kikapcsoláskor mozgasd vissza `dropins` → `dropins/temp`
- [ ] Sikeres mozgatás után frissítsd azonnal a JSON `state` mezőt
- [ ] Ütközésnél (célfájl már létezik) kezeld determinisztikusan: skip vagy felülírási policy szerint

## 7) Restart és életciklus

- [ ] Alkalmazás után jelenjen meg kötelező restart üzenet
- [ ] Dokumentáld, hogy a plugin láthatóság/menük csak restart után garantáltan konzisztens

## 8) Hibakezelés és adatkonzisztencia

- [ ] Minden fájlművelet legyen try/catch + részletes log
- [ ] Részleges hiba esetén rollback stratégia vagy legalább konzisztencia-javítás a registry-ben
- [ ] Ha JSON sérült, induláskor készíts mentést és állíts vissza üres/repair állapotot

## 9) Tesztforgatókönyvek

- [ ] Első indulás: temp automatikus létrejön
- [ ] Temp-be tett új plugin megjelenik a listában és bekerül a registry-be
- [ ] Checkbox be: plugin átkerül `dropins`-ba, `state=DROPINS`
- [ ] Checkbox ki: plugin visszakerül `temp`-be, `state=TEMP`
- [ ] Kézi másolás csak `dropins`-ba: manager ne vegye fel új regisztrált pluginnak
