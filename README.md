# follower-info-graphql-api
GraphQL API based on Java Spring that serves follower information saved to a database by "follower-info-polling"

## How to use

- Have [follower-info-polling](https://github.com/verfehlix/follower-info-polling) running in the background
- configure ```spring.datasource.url```, ```spring.datasource.username``` and ```spring.datasource.password```
  - either in ```resources/application.properties```
  - or as command line parameter (e.g. ```--spring.datasource.url=jdbc:postgresql://localhost:5433/followerDB```)

## GraphQL Schema

```graphql
type Query {
  followerInfo(
    startTimestamp: String,
    endTimestamp: String
  ) : [FollowerInfo]
}

type FollowerInfo {
    id: ID!
    timestamp: String!
    followerCount: Int
    followerList: [String]
}
```

## Example

Call:

```graphql
{
  followerInfo {
    timestamp
    followerCount
  }
}
```

Result:

```json
{
  "data": {
    "followerInfo": [
      {
        "timestamp": "2019-02-19 18:13:46.47",
        "followerCount": 131
      },
      {
        "timestamp": "2019-02-19 18:12:46.47",
        "followerCount": 131
      },
      {
        "timestamp": "2019-02-19 18:11:46.47",
        "followerCount": 130
      },
      {
        "timestamp": "2019-02-19 18:09:46.471",
        "followerCount": 129
      }
    ]
  }
}
```
