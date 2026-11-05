## Collection and hover-tooltip edition

A modified edition of [Feeeh/Kotoba-Keep](https://github.com/Feeeh/Kotoba-Keep),
based on upstream 1.5.0. Original mod by glitchk.id. This is a fork release.

### Added in this edition
- Press **B** to open a searchable item collection with filters for missing items,
  words never answered correctly, collected items, and collected words needing practice.
- Gold and green markers track **Collected** and **Said correctly** independently.
  Progress saves per player and world; correct-answer milestones survive failed reviews.
- Hover an item to see its Japanese Minecraft name, hiragana reading, and romaji.
- Press **K → Item Hover Tooltips** to toggle those three lines independently.
  Challenge hints retain their own settings.

Existing inventories and recoverable answer history are imported. Earlier consumed or
dropped items cannot all be recovered; the original history did not retain successes
overwritten by failed reviews. The catalogue includes creative-only base item types.

Validated with 30 automated tests and in-game checks of search, filters, saved progress,
the B shortcut, and independent tooltip settings.

### Install
1. Replace the older Kotoba Keep JAR in your instance's `mods/` folder with the release
   JAR below. Keep only one Kotoba Keep JAR installed.
2. Install Fabric API **0.165.0+26.3** and Simple Voice Chat **2.6.27+26.3** alongside it.
   Get them from [Fabric API](https://modrinth.com/mod/fabric-api) and
   [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat).
3. Download [vosk-model-small-ja-0.22.zip](https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip)
   and extract its folder into `config/kotobakeep/`.
4. Launch the Fabric profile and select your microphone with **V**.

Requires Minecraft **26.3**, Fabric Loader **0.20.1+**, and Java **25**.
For multiplayer collection tracking, install this edition on the server and participating clients.
Only clients need the speech model. Existing learning rules and voice challenges are retained.

Voice: **VOICEVOX:冥鳴ひまり**. Unofficial mod, not affiliated with Mojang or Microsoft.
