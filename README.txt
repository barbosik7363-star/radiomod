RADIO MOD (Forge 1.20.1, 47.3.0)

СПОСОБ 1 — на своём компьютере (нужен JDK 17 и Gradle 8.1.1):
  1. Открой терминал в этой папке.
  2. Выполни:  gradle build
     (если установлен Forge MDK, можно скопировать из него папку gradle и gradlew и использовать ./gradlew build)
  3. Готовый мод: build/libs/radiomod-1.0.0.jar -> положи в папку mods.

СПОСОБ 2 — без установки ничего (GitHub):
  1. Создай репозиторий на github.com и загрузи в него все файлы этой папки (включая .github).
  2. Вкладка Actions -> "Build mod" -> дождись зелёной галочки.
  3. Внутри запуска скачай artifact "radiomod-jar" — там твой .jar.

ВАЖНО: в build/libs берётся файл БЕЗ суффиксов -sources/-slim (обычный radiomod-1.0.0.jar).

ДОБАВИТЬ ПЕСНЮ: .ogg (МОНО!) в assets/radiomod/sounds/songs/, строка в sounds.json
  (с "attenuation_distance": 10) и запись в RadioPlaylist.ALL (ticks = секунды * 20).
