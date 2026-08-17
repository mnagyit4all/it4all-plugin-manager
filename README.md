# Dynamic Plugin Manager for Eclipse – Felhasználói Kézikönyv

A **Dynamic Plugin Manager** egy Eclipse bővítmény, amellyel külső `.jar` fájlokat importálhatsz az IDE-be, valamint egyszerűen ki- és bekapcsolhatod azokat egy központi felületen.

---

## 🚀 Főbb Funkciók

* **Bővítmények Importálása:** Új `.jar` formátumú pluginok hozzáadása a fájlrendszerből.
* **Egyszerű Állapotkezelés:** Pluginok engedélyezése (Enable) és tiltása (Disable) gombnyomásra.
* **Biztonságos Működés:** Az új vagy módosított pluginok az Eclipse stabilitásának megőrzése érdekében a következő újraindításkor lépnek érvénybe.
* **Automatikus Ellenőrzés:** Sérült vagy nem kompatibilis fájlok kiszűrése már a betöltéskor.

---

## 📖 Használati Útmutató

### 1. A Plugin Manager Nézet megnyitása
1. Indítsd el az Eclipse-t.
2. A felső menüsorban válaszd a **Window** > **Show View** > **Other...** lehetőséget.
3. A keresőbe írd be: **Plugin Manager**, majd kattints az **Open** gombra.

---

### 2. Új Bővítmény (Plugin) Importálása
1. Kattints a nézetben található **Import Plugin** gombra.
2. A megnyíló ablakban válaszd ki a telepíteni kívánt `.jar` fájlt.
3. A rendszer ellenőrzi a fájlt:
   * **Sikeres importálás:** A plugin megjelenik a listában **`DISABLED`** (Szürke/Piros) állapotjelzéssel.
   * **Sikertelen importálás:** Ha a fájl nem érvényes Eclipse plugin, hibaüzenet jelenik meg.

---

### 3. Plugin Engedélyezése és Tiltása
1. Jelöld ki a módosítani kívánt plugint a listában.
2. Használd a **Enable / Disable** gombot (vagy a jelölőnégyzetet) az állapot módosítására:
   * **`ENABLED` (Zöld):** A plugin aktív lesz a következő indításkor.
   * **`DISABLED` (Szürke / Piros):** A plugin inaktív marad.
3. A felületen egy figyelmeztetés jelenik meg: *„A változtatások az Eclipse újraindítása után lépnek életbe.”*

---

### 4. Változtatások Érvényesítése
A beállítások érvénybe léptetéséhez indítsd újra az Eclipse-t:
* Válaszd a **File** > **Restart** menüpontot.

---

## ⚠️ Állapotjelzések és Hibaelhárítás

| Állapot / Jelenség | Jelentés | Teendő |
| :--- | :--- | :--- |
| **`ENABLED` (Zöld)** | A plugin engedélyezve van. | Az Eclipse újraindítása után automatikusan betöltődik. |
| **`DISABLED` (Szürke / Piros)** | A plugin ki van kapcsolva. | Nem töltődik be az alkalmazás indításakor. |
| **Hibaüzenet importáláskor** | A kiválasztott `.jar` fájl sérült vagy nem tartalmazza a szükséges OSGi leírót. | Győződj meg róla, hogy érvényes Eclipse plugin `.jar` fájlt választottál ki. |
| **Már létező plugin figyelmeztetés** | Egy azonos nevű és verziójú plugin már be van importálva. | Erősítsd meg a felülírást, ha a frissebb verziót szeretnéd használni. |
