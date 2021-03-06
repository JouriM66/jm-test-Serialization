Kotlin (Java) тест сериализации
--------------------------------
#### Ссылки

Link to English readme is [HERE](readme.md)

Ссылка на статью с описанием методов сериализации находится [ТУТ](SerialDescription.rus.md).
 
# Описание 

Программа для тестирования скорости различных алгоритмов сериализации для Java.

В тесте используются:
* Родной для Java интерфейс Serializable.
* Родной для Java интерфейс Externalizable.
* Сериализация с использованием библиотеки [fasterXML-jackson](https://github.com/FasterXML/jackson).
* Сериализация с использованием библиотеки [minimal-json](https://github.com/ralfstx/minimal-json).
* Сериализация во входящий в состав Java SDK формат XML format (org.w3c.dom).

## Зависимости
Приложение зависит от следующих библиотек:
* Базовая библиотека [JM parent](https://github.com/JouriM66/jm-lib-kotlin).
* Библиотека [fasterXML-jackson](https://github.com/FasterXML/jackson).
* Библиотека [minimal-json](https://github.com/ralfstx/minimal-json).

## Действия теста
1. Генерируются случайные данные в форме простого дерева из строковых объектов.
2. Запусаются по очереди все тесты для сгенерированных данных. Каждый тест сохраняет нданные на диск, читает их с него и проверяет соответствие прочитанного оригиналу. 
3. Повтор запуска всех тестовуказанное в параметре "retry" число раз.

## Интерфейс
```
>sb.bat -?

Test for store classes tree structure
USAGE: SerializableTest.jar [-opts]

Where OPTS are:
  -Count=<number>        - set number of items to generate
  -Retry=<number>        - set number of iterations for each test
  -Out=<file>            - set file name to output
  -Nout                  - disable items output
  -gc                    - run gc after every test
```

##Results
Для тестирования запустите утилиту с нужными параметрами:
```
sb -n "-c=30000" "-r=3"
```
После того как будет произведено тестирование программа распечатает в текущую консоль таблицу с результатами сравнения:
```
Output file       : test_out
Number of elements: 30000
Number of retries : 3
Tests complete in 0:00:11.164 sec :: Save 0:00:03.880, Load 0:00:04.217, Total 0:00:08.112, Waste 0:00:03.052
----------------------------------------------------------------------------------------------------------------------------------
N               Name |            Save |   Best |  Worst |            Load |   Best |  Worst |           Total |   Best |  Worst
----------------------------------------------------------------------------------------------------------------------------------
6    SerialFull      |     0:00:00.754 |   2,15 |   0,28 |     0:00:00.601 |   1,09 |   1,22 |     0:00:01.358 |   1,39 |   0,70 |
1    ExternFull      |     0:00:00.281 |   0,18 |   2,43 |     0:00:00.287 |     == |   3,65 |     0:00:00.569 |     == |   3,05 |
4    Extern+Ser      |     0:00:00.549 |   1,30 |   0,76 |     0:00:00.490 |   0,71 |   1,72 |     0:00:01.042 |   0,83 |   1,21 |
7    XMLw3c          |     0:00:00.965 |   3,04 |     == |     0:00:01.334 |   3,65 |     == |     0:00:02.302 |   3,05 |     == |
5    JsJsonMini      |     0:00:00.501 |   1,10 |   0,93 |     0:00:00.729 |   1,54 |   0,83 |     0:00:01.231 |   1,16 |   0,87 |
3    JsJackAnn       |     0:00:00.591 |   1,47 |   0,63 |     0:00:00.367 |   0,28 |   2,63 |     0:00:00.959 |   0,69 |   1,40 |
2    JsJackSream     |     0:00:00.239 |     == |   3,04 |     0:00:00.409 |   0,43 |   2,26 |     0:00:00.651 |   0,14 |   2,54 |
----------------------------------------------------------------------------------------------------------------------------------
```

Где:
- Первая цифра - место, которое занял тест, от лучшего к худшему.
- Колонки "Best" и "Worst" содержат отличие текущего теста от лучшего и худшего в категории.
- Save - время затраченное тестом на сохранение данных. 
- Load - время затраченное тестом на загрузку данных.
- Total - Общее время затраченное тестом.
