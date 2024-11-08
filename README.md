# M5ClientMX 🌟

Добро пожаловать в проект **M5ClientMX**! Это приложение создано для упрощения процесса прошивки микропрограмм на различные устройства M5, что позволяет вам легко поддерживать оборудование в актуальном состоянии и обеспечивать его оптимальную работу. 🚀

## Функции 🛠️
- **Удобный интерфейс**: Интуитивный дизайн для легкой навигации и работы.
- **Управление прошивками**: Легко скачивайте и прошивайте микропрограммы для различных устройств M5.
- **Отслеживание прогресса**: Отображение текущего статуса во время установки прошивки.
- **Установка драйверов**: Упростите процесс установки необходимых драйверов для ваших устройств.
- **Пользовательские уведомления**: Получайте уведомления об успехах или ошибках с полезной информацией.

## Начало работы 🚀

### Предварительные требования
- Java Development Kit (JDK) 21 или выше
- Grable (для сборки проекта)
- Совместимое устройство M5

# **Установка**

## Сборка и запуск приложения

### Клонируйте репозиторий
    git clone https://github.com/Miroshka000/M5ClientMX.git
    cd M5ClientMX

## **Соберите проект и запустите приложение**
    ./gradlew build
    ./gradlew run

## **Создание файла (.exe)**

    ./gradlew jpackage

 После выполнения команды `./gradlew jpackage`, установочный файл (.exe) для запуска приложения будет находиться в папке `build/installer/M5ClientMX`


### Использование 🖥️
- **Выберите ваше устройство**: Выберите подходящее устройство из выпадающего списка.
- **Выберите COM порт**: Выберите правильный COM порт, подключенный к вашему устройству.
- **Выберите прошивку**: Выберите прошивку, которую хотите установить, из доступных вариантов.
- **Установите драйверы**: При необходимости нажмите кнопку для установки требуемых драйверов для вашего устройства.
- **Прошивка устройства**: Нажмите кнопку "Установить", чтобы начать процесс прошивки, и наблюдайте за индикатором выполнения.

## Вклад в проект 🤝
Мы приветствуем участие сообщества! Если хотите помочь улучшить проект, создайте форк репозитория, внесите изменения и отправьте pull request.

### Сообщение об ошибках 🐛
Если вы столкнулись с проблемами или ошибками, пожалуйста, сообщите о них в [разделе проблем](https://github.com/Miroshka000/M5ClientMX/issues) этого репозитория.

## Лицензия 📄
Этот проект лицензирован по лицензии MIT - см. файл [LICENSE](LICENSE) для подробностей.

## Благодарности 🙏
- [Esptool](https://github.com/espressif/esptool) за предоставление инструмента для прошивки микропрограмм.
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/) за утилиты для работы с файлами.

---

Спасибо, что ознакомились с **M5ClientMX**! Мы надеемся, что этот инструмент поможет вам в ваших проектах и улучшит ваш опыт работы с устройствами M5 🎉