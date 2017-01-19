Kotlin (Java) Serialization test
--------------------------------
Ссылка на описание на русском языке находится [ТУТ](readme.rus.md).

Application to test of speed for several different serialization methods.

Implemented methods are:
* Java native Serializable interface.
* Java native Externalizable interface.
* Serialization using [fasterXML-jackson](https://github.com/FasterXML/jackson) library.
* Serialization using [minimal-json](https://github.com/ralfstx/minimal-json) library.
* Serialization to Java SDK XML format (org.w3c.dom).

## Changes

* 19.01.2017
  - Added "NanoXML" library (actually very slow).
  - Added option ``-Test=<name>`` to run single test.

## Depends

Application depends on:

* [JM base](https://github.com/JouriM66/jm-lib-kotlin) library.
* [fasterXML-jackson](https://github.com/FasterXML/jackson) library.
* [minimal-json](https://github.com/ralfstx/minimal-json) library.
* [nanoXML](http://nanoxml.sourceforge.net) library.

##Test sequence
1. Generate random data contains simple tree of elements with string data.
2. Run all tests one by one with same data. Each test will store data to the disk, read it and compare loaded with original. 
3. Repeat run cycle by specified number of "retry".

##Usage
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
For test call application with desired parameters:
```
sb -n "-c=30000" "-r=3"
```
after running all tests u`ll see at current console test results as below:
```
Output file       : test_out
Number of elements: 30000
Number of retries : 3
Tests complete in 0:00:21.319 sec :: Save 0:00:06.905, Load 0:00:09.836, Total 0:00:16.759, Waste 0:00:04.560
```

Result table
------------
 N |            Name |            Save |   Best |  Worst |            Load |   Best |  Worst |           Total |   Best |  Worst
---|-----------------|-----------------|-------:|-------:|-----------------|-------:|-------:|-----------------|-------:|--------:
8  | NanoXML         |     0:00:02.483 |  10,34 |     == |     0:00:04.993 |  13,06 |     == |     0:00:07.480 |  11,12 |     == |
5  | SerialFull      |     0:00:00.743 |   2,39 |   2,34 |     0:00:00.646 |   0,82 |   6,73 |     0:00:01.391 |   1,25 |   4,38 |
4  | ExternFull      |     0:00:00.269 |   0,23 |   8,23 |     0:00:00.992 |   1,79 |   4,03 |     0:00:01.264 |   1,05 |   4,92 |
6  | Extern+Ser      |     0:00:00.922 |   3,21 |   1,69 |     0:00:00.551 |   0,55 |   8,06 |     0:00:01.473 |   1,39 |   4,08 |
7  | XMLw3c          |     0:00:01.068 |   3,88 |   1,32 |     0:00:01.345 |   2,79 |   2,71 |     0:00:02.416 |   2,92 |   2,10 |
3  | JsJsonMini      |     0:00:00.635 |   1,90 |   2,91 |     0:00:00.557 |   0,57 |   7,96 |     0:00:01.195 |   0,94 |   5,26 |
2  | JsJackAnn       |     0:00:00.566 |   1,58 |   3,39 |     0:00:00.355 |     == |  13,06 |     0:00:00.923 |   0,50 |   7,10 |
1  | JsJackSream     |     0:00:00.219 |     == |  10,34 |     0:00:00.397 |   0,12 |  11,58 |     0:00:00.617 |     == |  11,12 |


Where:
- First number - is the test place (from best to worst) calculated by total time spend.
- "Best" and "Worst" columns displays difference between current test and best or worst in category.
- Save - time spend by test storing test data. 
- Load - time spend by test loading stored data.
- Total - time spend by test in total.
