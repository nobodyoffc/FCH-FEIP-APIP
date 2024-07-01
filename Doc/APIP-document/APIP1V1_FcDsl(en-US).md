```
Type: APIP
SerialNumber: 1
ProtocolName: FcDsl
Version: 1
Description : 定义Free Consensus APIP 简易查询语法。
Author: C_armX
Language: zh-CN
CreateDate: 2022-11-30
UpdateDate: 2023-02-01
```

# APIP1V1_FcDsl(zh-CN)

## 目录


[关于APIP](#关于APIP)

[APIP的发布](#APIP的发布)

[接口标识](#接口标识)

[接口url的构成](#接口url的构成)

[时间戳与密码算法](#时间戳与密码算法)

[示例身份信息](#示例身份信息)

[API服务流程](#API服务流程)

[谁是API的请求方](#谁是API的请求方)

[如何购买API服务](#如何购买API服务)

[signIn接口](#signIn接口)

[响应状态](#响应状态)

[数据请求接口](#数据请求接口)

[APIP简易查询语法](#APIP简易查询语法)

[general通用接口](#general通用接口)

## 关于APIP

`APIP`(Application Programming Interface Protocols)是自由共识生态协议的一种类型，用于创建和发布开放的API文档，供API服务方开发部署通用API服务，以实现数据服务的去中心化。
API需求方可以按照APIP协议,从遵循该协议的任何一个API服务方那里获取数据服务。

`《APIP1_OpenAPI》`规范了APIP类型协议的`协议发布`、`接口标识`、`接口URL构成`、`时间戳格式`、`密码算法`、`服务流程`、`商业模式`、`connect接口`，`数据接口`和`查询语法`的通用规则。
设计、开发或应用APIP协议前，应先参考《APIP1_OpenAPI》协议。

## 示例数据

- `requester`: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
- requester的公钥`pubKey`: 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
- requester的私钥`priKey`: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
- requester获得的对称密钥`symKey`（即`sessionKey`): d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09
- 服务商链上发布的`urlHead`: https://localhost:8080/api/

## 主要共识

本协议定义Free Consensus 简易查询语法如下：

* `fcdsl`参考ElasticSearch DSL，进行简化，更多查询功能和语法由API服务商自行定义和发布。
* 语句包括
  - 独立查询语句：all和ids
  - 容器语句：query，filter，except
  - 容器查询语句：terms，part，match，range，exists，unexists
  - 分页语句：sort，after，size
  - 索引语句：index
  - 其他语句：other
* all、ids、query、filter、except不可相互包含。
* ids不与query、filter、except同时使用。
* 除了ids和all以外，所有查询命令均为多字段查询，字段名在"fields"数组中给出，跨字段关系见相应命令说明。
* 查询命令以"s"结尾，表示可以同时查询多个值，值在"values"数组中给出，否则为单值查询，在"value"中给出。
* 所有查询值均为字符串类型。
* query、filter和except可组合使用，组合关系为"and"。
* 除了ids和all以外的查询命令在query、filter或except内组合使用，组合方式为"and"。
* query、filter或except只是容器，包含查询语句后使用。
* 查询的优先次序为：ids > all > query、filter或except
* size、sort、after可省略,省略时采用默认值：
    - sort的默认值由各数据相应协议给出。
    - size默认值为20条
    - after缺省为从首条记录开始获取
* 查询命令执行的优先顺序为：
    1. 无fcdsl项的all查询
    2. ids查询
    3. 有fcdsl项的all查询
    4. 其他查询
* 查询命令如下：

## all: 全查询

* 获取全部条目。	
* 无fcdsl对象, 或fcdsl对象中无query、filter、except项
* 基于ES DSL的matchAll语句
```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697
}
//查询全部
```

```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "size":20,
        "sort":{
            "lastHeight": "asc"
        },
        "after":["677611"]
    }
}
//查询全部，按lastHeight升序，从lastHeight=677611之后开始取20条。
```

## ids: id查询
* 按照ID获取多个值
* 不与其他语句组合使用
* 基于ES DSL的mget语句
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/txList",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["754ecc6adc0556f7c79181abe28ebae2b2f7c4f8237c013f2ce149ed19965df8","0f96070e66dff2909570fc0b78fff572eff0c37cd17938e05c4c3a2fef0ef90f"]
    }
}
//查询id为"754ecc6adc0556f7c79181abe28ebae2b2f7c4f8237c013f2ce149ed19965df8"或"0f96070e66dff2909570fc0b78fff572eff0c37cd17938e05c4c3a2fef0ef90f"的条目
```

## query: 主查询块
* `query`内可组合多个查询语句，组合关系为"and"
* 可包含的查询命令如下：

### 1. terms: 精确查询
* 字符类型，多字段，多值，精确匹配。
* 多字段关系: or
* 多值关系: or
* 基于ES DSL的terms和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["id","guide"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK","FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
            }
        },
        "size":10
    }
}
//查询字符串字段“id”或"guide"中值为"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"或"FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"的条目。
```

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["cd","cdd"],
                "values":["2","3"]
            }
        },
        "size":10
    }
}
//查询字段“cd”或“cdd”的值等于"2"或"3"的条目。
```
### 2. part: 部分查询
* 字符类型，多字段，单值，部分匹配。	
* 多字段关系: or
* isCaseInsensitive为"true"时不区分大小写
* 基于ES DSL的wildcard和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["cid","id"],
                "value":"arm",
                "isCaseInsensitive":"true"
            }
        }
    }
}
//查询“cid”和"id"字段,值在任何位置包含“arm“的条目,不区分大小写。
```
### 3. match: 分词查询
* 文本类型，多字段，单值，分词模糊查询
* 多字段关系: or
* 基于ES DSL的match和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/opReturnSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "match":{
                "fields":["opReturn"],
                "value":"free cash"
            }
        }
    }
}
//在"opReturn"字段，对“free cash”做分词查询，不区分大小写

```
### 4. exists: 非空查询
* 字符类型，多字段，全非空查询
* 多字段关系: and
* 基于ES DSL的exists和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "exists":["pubKey","btcAddr"]
        }
    }
}
//查询“pubKey”和"btcAddr"字段都非null的条目。
```

### 5. unexists: 为空查询
* 字符类型，多字段，全为空查询
* 多字段关系: and
* 基于ES DSL的exists和bool语句
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "unexists":["pubKey","btcAddr"]
        }
    }
}
//查询“pubKey”和"btcAddr"字段都为null的条目。
```

### 6. range: 范围查询
* 多字段同时满足多个相同条件组合查询
* 多字段关系: or
* 条件包括：
    gt（>，大于）
    gte（>=，大于等于）
    lt（<，小于）
    lte（<=，小于等于）
* 条件可组合使用，关系为：and
* 基于ES DSL的range语句
		
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000的条目。
```

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["id","guide"],
                "gt": "FA",
                "lte": "FE"
            }
        }
    }
} 
//查询id和guide字段,前两个字母为FB、FC或FE的条目。
```

## filter 筛选查询块
* 按此部分的条件筛选条目
* 命令和用法与query相同，略
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        },
        "filter":{
            "range": {
                "fields": ["guide"],
                "gt": "FA",
                "lte": "FE"
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000, 并且"guide"值前两个字母为FB、FC或FE的条目，
```

## except 剔除查询块
* 按此部分的条件剔除条目
* 命令和用法与query相同，略
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        },
        "except":{
            "terms":{
                "fields":["guide"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000, 排除"guide"为"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"的条目，
```

## size: 请求数量
* 请求返回的条目数量，以字符串形式提交。
* 基于ES DSL的size语句

局部示例：
```
"size":"1000"
//请求返回1000条。
```

## sort: 请求排序
* 查询结果的排序条件
* filed给出排序字段
* order值为“asc”时升序，“desc”降序
* 多个字段依次实现多级排序
* 基于ES DSL的sort语句
* 响应数据包含"last"字段，给出最后条目的sort值

局部示例：	
```
"sort":[{
		"field": "guide",
		"order": "asc"
	},
	{
		"field": "cdd",
		"order": "desc"
	}
]
//第一级按"guide"升序，第二级按cdd降序排列条目。
```

## after: 起始位置
* 定位从某项之后开始获取。
* 基于ES DSL的的after语句。
* after必须与sort一起使用，获取相应sort位置之后的条目。
* after的值的次序与sort的字段次序严格对应。
* after的所有值均转换为String。
* 没有“after”则从排序第一项开始获取。
* 连续请求时，after可取上次请求响应数据中`last`字段的值。

局部示例：	
```
"after":[
        "FAEYhvxWZPE522v6EogroXSTWqxNoBvp7K",
        "1429"
    ]
//按照address一级升序和cdd二级降序的排序，从guide为FAEYhvxWZPE522v6EogroXSTWqxNoBvp7K，cdd为1429之后的那一项开始获取。
```

## index: 索引名称
仅用于general接口，用于指明查询的索引，只能是单一索引。其他接口无需用户给定索引。

## other: 其他参数
由具体协议自定义查询内容。

## 综合示例

查询符合以下条件的条目：
    1）"cd"和"cdd"字段值均大于0；
    2）"pubKey"字段不为空；
    3）排除"cash"字段值为0的条目；
    4) 按guide一级升序，cd二级降序；
    5）从guide="F5rhTTYgQYetQHM3k9xDRbbiNDZS61oAFa"，cd= "1121521"的条目之后开始获取；
    6）请求2条。
    
```
{
	"url": "http://localhost:8080/APIP/apip1/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0"	
            }
        },
        "filter":{
            "query":{
                "exists":["pubKey"]
            }
        },
        "except":{
            "terms":{
                "fields":["cash"],
                "values":["0"]
            }
        },
        "sort":[{
            "field":"guide",
            "order":"asc"
            },
            {
            "field":"cd",
            "order":"desc"
        }],
        "after": [
        "F5rhTTYgQYetQHM3k9xDRbbiNDZS61oAFa",
        "1121521"
        ],
        "size":"2"
	}
}
```
响应数据
```
{
    "code": 0,
    "message": "Success.",
    "balance": 15137089340,
    "got": 2,
    "total": 2425,
    "bestHeight": 1726809,
    "data": [
        {
            "id": "FNiX79EiGQAajnbURHPE79arDButdzDQQE",
            "pubKey": "039f66926a9c4bbba4cca7028ba3a1593f74335d7af96e0f1f552283f33f289d1b",
            "balance": 99376620,
            "income": 1895797472,
            "expend": 1796420852,
            "guide": "F5xMDt2r929zNKqnmcEK9TuQrpA81Rcbhr",
            "birthHeight": 743167,
            "lastHeight": 752407,
            "cdd": 2,
            "cd": 685,
            "cash": 1,
        },
        {
            "id": "FQ9KvLMbDd3EJy92pZzMKXBF9XASF4Bb4P",
            "pubKey": "0323156856677ab2b1308fa1ede716a06ff03fd50aad338396d1b6ea8fa46a59ac",
            "balance": 29071127,
            "income": 10001894765853,
            "expend": 10001865694726,
            "guide": "F5zFVnLzzSaw2r8wrggT5eYPJHmHGiMPxq",
            "birthHeight": 660683,
            "lastHeight": 1538260,
            "cdd": 359,
            "cd": 38,
            "cash": 2,
        }
    ],
    "last": [
        "F5zFVnLzzSaw2r8wrggT5eYPJHmHGiMPxq",
        "38"
    ]
}
```

## general 接口

按照fcdsl语法查询的通用接口。

示例：
URL：http://localhost:8080/APIP/apip1/v1/general

* Request body 
    在数据请求接口基础上，在"fcdsl"项下增加 "index"字段，用于指定所查询的索引库。
示例：
```
{
	"url": "http://localhost:8080/APIP/apip1/v1/general",
	"time": 1677673821267,
	"nonce": 1,
	"fcdsl": {
		"index": "cid",
		"query": {
			"part": {
				"fields": ["cid", "id"],
				"value": "*ar*",
				"isCaseInsensitive": "true"
			}
		},
        "sort":[
				{
				"field":"id",
				"order":"asc"
			}
		],
        "last": [
            "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6"
        ],
        "size":"2"
	}
}
```

* Response body
    与数据请求响应一致。
示例：
```
{
    "code": 0,
    "message": "Success.",
    "balance": 15136389340,
    "got": 2,
    "total": 38,
    "bestHeight": 1726961,
    "data": [
        {
            "id": "F6BGi3Hzo9GUgParKZzm66xRfcBxm2YX5Z",
            "cid": "?????_YX5Z",
            "usedCids": [
                "?????_YX5Z"
            ],
            "reputation": 0,
            "hot": 0,
            "nameTime": 1634804353,
            "lastHeight": 943560
        },
        {
            "id": "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6",
            "cid": "Maka_w2C6",
            "usedCids": [
                "Maka_w2C6"
            ],
            "reputation": 0,
            "hot": 0,
            "nameTime": 1623158294,
            "lastHeight": 772712
        }
    ],
    "last": [
        "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6"
    ]
}
```

# FCDSL in URL
Fcdsl to URL parameters:

index = <String indexName>
ids = <String[] IDs>
match = field1,field2,...,value
part = field1,field2,...,value
terms = field,value1,value2...
equals = field,value1,value2,...
range = field,lt,value1,lte,value2,gt,value3,gte,value4
exists = field1,field2,...
unexists = field1,field2,...

sort = field1,order1,field2,order2...
size = <String of int>
after = <List<String> last>
other = <String obj>

Filter and Except is forbidden.






