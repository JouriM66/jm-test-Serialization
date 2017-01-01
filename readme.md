Kotlin (Java) Serialization test
--------------------------------
Application to test of speed for several different serialization methods.

Implemented methods are:
* Java native Serializable interface.
* Java native Externalizable interface.
* Serialization using [fasterXML-jackson](https://github.com/FasterXML/jackson) library.
* Serialization using [minimal-json](https://github.com/ralfstx/minimal-json) library.
* Serialization to Java SDK XML format (org.w3c.dom).

## Depends
Application depends on:
* [JM base](https://github.com/JouriM66/jm-lib-kotlin) library.
* [fasterXML-jackson](https://github.com/FasterXML/jackson) library.
* [minimal-json](https://github.com/ralfstx/minimal-json) library.

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

Where:
- First number - is the test place (from best to worst) calculated by total time spend.
- "Best" and "Worst" columns displays difference between current test and best or worst in category.
- Save - time spend by test storing test data. 
- Load - time spend by test loading stored data.
- Total - time spend by test in total.
