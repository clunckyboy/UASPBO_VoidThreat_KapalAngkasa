# Void Threat
Void Threat adalah game 2D yang dibuat menggunakan Bahasa Java dan framework JavaFX
untuk platform desktop. Tujuan dari player di game ini adalah melindungi kapal luar angkasanya 
dari kapal atau benda luar angkasa yang akan menabrak kapal player, dengan cara menembaki benda 
tersebut agar hancur. Setiap benda/kapal yang berhasil ditembak, poin player akan bertambah.

## Fitur : 
### 🔫 Shooting 
Player menggerakkan kapalnya menggunakan touchpad atau mouse ke kiri dan ke kanan, 
lalu menekan kiri untuk mulai menembak benda/kapal. Jika kapal player terkena benda/kapal, 
permainan akan berakhir

### ❤️ Lives
Player diberi 3 nyawa saat permainan dimulai. Ketika nyawa 0, maka game berakhir.

### 🎯 Skor
Saat pemain berhasil menembak kapal/benda, skor pemain akan bertambah

### ⚡ Buff & Difficulty 
Player juga akan mendapatkan buff jika skor makin tinggi. Difficulty semakin meningkat seiring meningkatnya skor pemain

### 🥇 Leaderboard 
Pemain bisa melihat nama dan skor pemain lain dari menu leaderboard. Menu leaderboard menampilkan top 10 pemain dengan skor tertinggi.
(Informasi data nama pemain dan skor kami simpan di Neon Serverless Postgres)


## Dependencies
- Java IDE (rekomendasi : IntelliJ IDEA)
- Oracle OpenJDK 23.0.2
- JavaFX SDK 17.0.6
- PostgreSQL JDBC Driver (42.7.5)

## Instalasi dan Menjalankan Aplikasi
1. Clone repositori ini

```
git clone https://github.com/clunckyboy/UASPBO_VoidThreat_KapalAngkasa.git
```

2. Buka folder tersebut menggunakan IntelliJ
3. Pergi ke ```src/main/java/com/game/uaspbo_voidthreat_kapalangkasa/GameApp.java```
4. Tekan tombol Run ▶️ pada bagian atas
5. Selamat bermain

## Link Video Presentasi
https://youtu.be/yT6mubNFHpM




