# sprblog

## Описание

Реализация простого блога с помощью SpringBoot

Реализована функциональность:
- Вывода списка постов с пагинацией и фильтрацией по тегу
- Вывод конкретного поста со списком его комментариев
- Возможность редактировать содержимое поста, удалять и добавлять новые посты
- Возможность добавлять, редактировать и удалять комментарии
- Возможность ставить постам лайки


## Команды для сборки, тестирования и запуска

- Тесты
```shell
./gradlew test
```

- Пересобрать
```shell
./gradlew clean bootJar
```

- Запустить
```shell
build/libs/sprblog-0.0.1-SNAPSHOT.jar
```

Главная страница со списком постов будет доступна по адресу `http://127.0.0.1:8080/posts`
