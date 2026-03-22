# DemirTV

<p align="center">
  <img src="app/src/main/res/drawable/logo_demirtv.png" width="160" alt="DemirTV" />
</p>

<p align="center">
  <strong>Premium aile odakli canli yayin uygulamasi</strong><br/>
  Android TV · Telefon · Tablet
</p>

---

## Genel Bakis
DemirTV, aile icin tasarlanmis premium bir canli yayin deneyimi sunar. Akici arayuz, profil odakli kullanim ve guvenli icerik yaklasimi ile tek merkezden izleme amaci tasir.

## Ozellikler
- Premium arayuz ve akici deneyim
- Profil secimi
- Canli TV oynatma (HLS)
- Guncellenebilir kanal listesi (M3U)
- Uygulama ici guncelleme kontrolu
- Zorunlu guncelleme destegi

## Hedef Platformlar
- Android TV (MiBox, Android TV cihazlari)
- Android telefon / tablet

## Kurulum
1) Android Studio ile projeyi acin
2) `app` modulunu calistirin
3) Cihazda veya emulator/TV'de test edin

## Guncelleme Akisi
Uygulama acilisinda `update.json` kontrol edilir. Yeni surum varsa uygulama icinden indirme ve kurulum baslatilir.

`update.json` ornegi:
```json
{
  "versionCode": 20,
  "versionName": "1.9",
  "downloadUrl": "https://github.com/eticin60/DemirTV/releases/download/v1.9/DemirTV-v1.9.apk",
  "forceUpdate": false
}
```

## Release / CI Pipeline
Tag atildiginda GitHub Actions otomatik:
- Surum numaralarini gunceller
- Imzali Release APK uretir
- GitHub Release olusturur
- `update.json` dosyasini gunceller

Ornek tag:
```bash
git tag -a v1.9 -m "normal update"
git push DemirTV v1.9
```

## Kanal Listesi (M3U)
Bu uygulama yalnizca lisansli ve kullanim izni alinan yayinlar ile calistirilmalidir.

## Lisans
MIT

## Degisiklikler
Detaylar icin `CHANGELOG.md` dosyasina bakiniz.
