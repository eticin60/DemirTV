# Memory Bank — DemirTV

## Proje Ozeti
- DemirTV: Android TV + telefon + tablet icin canli yayin uygulamasi.
- Jetpack Compose UI, Media3/ExoPlayer ile HLS oynatma.
- Profil secimi, premium arayuz, uygulama ici guncelleme akisi.

## Calisma Dizini
- Ana repo: `C:\AndroidStudioProjects\DemirTV`
- GitHub: `https://github.com/eticin60/DemirTV`
- Remote adi: `DemirTV`

## M3U / Kanal Akisi
- M3U, GitHub raw uzerinden cekiliyor.
- URL: `https://raw.githubusercontent.com/eticin60/DemirTV/main/DemirTV.m3u`
- M3U parser: `app/src/main/java/com/onurcan/demirtv/data/parser/M3UParser.kt`
- Cocuk filtresi yok; yasakli anahtar kelimeler filtreleniyor.
- Oncelikli kanal sirasi (ATV, Show TV, Kanal D, Star TV, TRT 1, TV8, NOW, FOX).
- Izlenme sayacina gore ikincil siralama.

## Guncelleme Sistemi
- `UpdateManager` her acilista `update.json` kontrol eder.
- Cache-buster var: `?ts=`.
- Uygulama ici indirme (DownloadManager) + kurulum ekranina yonlendirme.
- Zorunlu guncelleme (forceUpdate) destekli.
- Download ilerleme, hiz ve MB gosterimi var.
- Guncelleme UI koyu/premium stil.

## Update.json
- Konum: repo root `update.json`.
- Ornek:
```
{
  "versionCode": 22,
  "versionName": "2.2",
  "downloadUrl": "https://github.com/eticin60/DemirTV/releases/download/v2.2/DemirTV-v2.2.apk",
  "forceUpdate": false
}
```

## Versioning (CI)
- Tag ile surum: `v2.2` → `versionName=2.2`.
- `versionCode` hesap: `major*10 + minor` (2.2→22, 2.3→23).
- Onemli not: eski yuksek versionCode kurulu cihaz yoksa 22/23 guvenli.

## CI / GitHub Actions (publish.yml)
- Workflow: `.github/workflows/publish.yml`
- Tag push ile calisir.
- Imzali release APK uretir.
- Release olusturur.
- `update.json` ve `app/build.gradle.kts` gunceller ve main’e pushlar.
- Keystore secrets:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`

## Imza / Keystore
- Keystore: `C:\Users\Onurcan DEMİR\demirtv_keystore.jks`
- Alias: `demirtv`
- Secrets GitHub Actions icin eklendi.

## Dikkat Edilecekler
- `versionCode` APK’da `update.json` ile ayni olmali; aksi halde uygulama tekrar guncelleme ister.
- Uygulama icinde eski cache gorunmesi icin cache-buster var.
- Release imza farki: debug’dan release’e geciste uygulama kaldirilip yeniden kurulmalı.

## UI / Player Davranisi
- Arka plana gidince ExoPlayer pause.
- Geri gelince live edge’e atlayip devam eder.
- Dosya: `app/src/main/java/com/onurcan/demirtv/ui/screens/PlayerScreen.kt`

## Logo / TV Banner
- TV banner: `app/src/main/res/drawable/banner_tv.png`
- Manifest: `android:banner` → `@drawable/banner_tv`

## README / CHANGELOG
- README kurumsal hale getirildi.
- `CHANGELOG.md` eklendi.

## Git Notlar
- `main` tek branch.
- Commit/push olmadan GitHub’a yansimaz.
- Kodsuz Pull: Android Studio → Git → Pull…

## Son Durum / Ozet
- Update workflow stabil.
- VersionCode/Name CI ile otomatik.
- In-app update stabil.
- Kanal sirasi oncelikli.
- Backgroud playback fix.
