# blackbox-stats

## How to run
```
sbt run
```
blackbox should be located in the working directory (`./blackbox`).

## How to use

Navigate to http://localhost:8080/stats:

```json
{
    "stats": [
        {
            "eventType": "baz",
            "wordsCount": {
                "lorem": 3,
                "amet": 2,
                "ipsum": 1,
                "sit": 1
            }
        },
        {
            "eventType": "foo",
            "wordsCount": {
                "lorem": 3,
                "ipsum": 3,
                "dolor": 2
            }
        },
        {
            "eventType": "bar",
            "wordsCount": {
                "dolor": 4,
                "lorem": 3,
                "ipsum": 2,
                "amet": 2,
                "sit": 1
            }
        }
    ]
}
```
The default window size is 10 seconds.
