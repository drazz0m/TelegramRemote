# TelegramRemoteSpigot
# EN
This is a Spigot plugin for Minecraft servers that enables remote management and notifications via Telegram. The bot interacts with the server console, provides administrative functions, and sends notifications based on specific events.

## Installation
1. **Telegram bot setup**
   - Create a bot on [BotFather](https://t.me/BotFather).
   - Obtain the bot token.

2. **Plugin installation**
   - Download the latest plugin version from the [releases page](https://github.com/drazz0m/TelegramRemote/releases).
   - Place the downloaded JAR file into the `plugins` folder of your Spigot server.

3. **Configuration**
   - Edit the `config.yml` file in the `plugins/TelegramRemote` folder.
   - Set the bot token and its username in the `token` property.
   - Set your ChatID obtained through [getmyid_bot](https://t.me/getmyid_bot).
   - Configure RCON for console and command operations in the bot:
      ```yaml
      rcon:
        enable: true
        rcon_host: 0.0.0.0
        rcon_port: 25575
        rcon_pass: qwerty
      ```
   - Configure other parameters as needed.

## Usage
- Start your Spigot server.
- Begin a private chat with the bot.
- Use `/start` to initiate communication with the bot.

## Administrative Functions
- **Bot settings**
  - Manage language, notifications, enable/disable features, and add administrators.
- **Console mode**
  - Enter console mode to send commands to the server.
- **Notifications**
  - Configure various notification parameters.
- **Administrator Panel**
  - Access administrative functions to manage the server.

## Telegram Notifications
Receive notifications about:
- Changes in operator status.
- Server startup.
- Server updates.

## Author
- drazz

## Issues and Contributions
Report issues and bugs.

## License
This project is licensed under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) license.

# RU
Это плагин для Spigot для серверов Minecraft, который обеспечивает удаленное управление и уведомления через Telegram. Бот взаимодействует с консолью сервера, предоставляет административные функции и отправляет уведомления на основе конкретных событий.

## Установка
1. **Настройка телеграм-бота**
   - Создайте бота на [BotFather](https://t.me/BotFather).
   - Получите токен бота.

2. **Установка плагина**
   - Скачайте последнюю версию плагина со [страницы релизов](https://github.com/drazz0m/TelegramRemote/releases).
   - Поместите загруженный файл JAR в папку `plugins` вашего сервера Spigot.

3. **Настройка**
   - Отредактируйте файл `config.yml` в папке `plugins/TelegramRemote`.
   - Установите токен бота и его юзернейм в свойстве `token`.
   - Установите свой ChatID, полученный с помощью [getmyid_bot](https://t.me/getmyid_bot).
   - Настройте RCON для работы с консолью и командами в боте:
      ```yaml
      rcon:
        enable: true
        rcon_host: 0.0.0.0
        rcon_port: 25575
        rcon_pass: qwerty
      ```
   - Настройте другие параметры по необходимости.

## Использование
- Запустите свой сервер Spigot.
- Начните личный чат с ботом.
- Используйте `/start`, чтобы начать общение с ботом.

## Административные функции
- **Настройки бота**
  - Управление языком, уведомлениями, включение и отключение параметров, добавление администраторов.
- **Режим консоли**
  - Вход в режим консоли для отправки команд на сервер.
- **Уведомления**
  - Настройка различных параметров уведомлений.
- **Панель администратора**
  - Доступ к функциям администрирования для управления сервером.

## Уведомления в Telegram
Получайте уведомления:
- Изменениях статуса оператора.
- Запуске сервера.
- Обновлениях сервера.

## Автор
- drazz

## Проблемы и Вклад
Сообщайте о проблемах и багах.

## Лицензия
Этот проект лицензирован в соответствии с [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

