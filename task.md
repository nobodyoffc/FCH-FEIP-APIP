# 备忘任务

    private EccAesType type;

    private String alg;
    private byte[] msg;
    private byte[] symKey;
    private byte[] password;
    private byte[] pubKeyA;

    private byte[] pubKeyB;
    private byte[] priKeyA;
    private byte[] priKeyB;
    private byte[] iv;
    private byte[] sum;
    private byte[] cipher;



[ ] 定制incomeT的reward
[ ] 展示未支付
[ ] 测试 获取lasOrderId，unpaidReward
* debug
  [ ] jedisPool  
  [ ] jedis内部化，close

* 逻辑
  * 分配
    * 分配类型
      * 渠道分配：每周，按分成比例分配。
      * 运营分配：每周，按固定比例分配，每项目阶段调整分配比例。
    * 分配方式
      * 发放门槛：低于门槛值不分配，避免粉尘交易，累积下来。
      * 分配发放表：每周自动计算应分配数量，保存分配发放表在固定网页，出纳每周固定时间查看
      * 发放：出纳使用飞签先归整cash，然后扫描或复制分配表，自动进入交易签名页面，发送交易完成发放。
      * 记账：获取已记账发放之后的新发放交易，扣减渠道余额。
  * signIn改造，去掉加密
  * 用户登录文档修改
  * 数据服务微支付
  [ ] 备份两种via余额到ES。如何发放分成？发放后如何重新计算？发放-保存历史记录-原库余额归零。
  [x] 购买via分成
  [x] 消费via分成
  [x]order via
  [x] orderScanner:订单验证兼容无备注和有备注。增加了设置中的开关; 
  [x] orderScanner: via充值渠道记录
  [x] checkRequest: 增加via消费渠道记录
  [x] Replier:增加渠道记录。
  * Redis: balance 改为 service_user: fid, balance; service_consumerVia:fid,viaT
  * ES: service_user: fid, balance; service_via: fid,type(consume,topUp),viaT
* CcManager
  * order mempool
  * SignUp
  * SignIn
  * Cost
  * user manage

  [ ] explorer用户登录逻辑：

* 方案一
  * 独立于APIP的后端ccManager
  * 前端要求签名。 
  * 用户提交签名。
  * 前端验证通过。
  * 让用户设置pin码。
  * 前端计算（fid+pin）的哈希值作为symKey。 
  * 前端将用户fid和symKey（sessionKey异或一下）传给后台。
  * 后台查询fid余额是否大于价格。
  * 后台保存fid和symKey。 
  * 后台返回成功。
  * 用户再次登录，输入pin，前端提交fid和symKey，后端验证后通过。
  * 前端向APIP请求数据
  * 前端调用接口让后端扣费：fid，接口名
* 方案二
  * 前端要求用户签名。
  * 用户向前端提交签名。
  * 【登录接口】:前端调用本接口提交签名。后端验证签名、余额。通过后，后端生成随机token，保存fid和token，并返回给前端
  * 前端保存fid和token到用户本地，并让用户进入收费功能页面
  * 用户再次进入网页，前端检查用户本地fid和token，如果没有则转到第一步。
  * 【扣款接口】: 前端提交fid和用户请求成功的接口名称到后端。后端记录扣款。非收费功能不调用该接口。
  * 问题：前端与用户本地设备之间什么关系？token是否加密传递？
* 方案三
  * 特点：不独立计费，浏览器作为apip服务方的渠道，请求apip服务时，标注渠道的fid，获取apip服务方的分成。
  * personal和tools之外的功能免费。采用浏览器公共服务接口
    * 获取当前apip服务的接口：freeGet/v1/getFreeService
    * 后端返回：sid，name, urlHead，account，minPayment, pricePerKBytes, sessionDays 和sessionKey
    * 保存在用户端
  * personal和tools功能在登录后出现。
  * 登录
    * 用户点击登录按钮；
    * 网站给出签名Json
    * 用户端（一般是钱包，如飞签、密签）填入nonce，time，pubKey
    * 用户端用pubKey的私钥对msg的值签名后填入Sign
    * 用户通过复制粘贴或扫码将签名后的json填入前端。
    * 前端对url的值发起post请求：将sign的值填入header的"Sign", 将msg的值填入body（必须与签名时严格一致，哈希相等）
    * 后端验证签名和余额无误后，返回sessionKey（不再用公钥加密，https传输），取前12字符为sessionName。
    * 用户端保存：apip:{fid、sid(当前apip服务的sid）、urlHead（当前apip服务的urlHead）、sessionName、sessionKey}
  * 访问
    * 所有免费接口采用免费sessionKey访问，过期则调用getKey重新获取。
    * 检查本地是否有apip项，有的话取urlHead拼装url，采用用户sessionKey，请求该用户信息（如下），成功则进入已登录状态
    * 失败则显示错误信息：
      * 如果是1004，显示购买提示json(如下），将该提示的data.via赋值为本运营的收款fid:"FSbkZGGZi4SRprgUTNu7LQUwxM9Yujcash"。
      * 进入无登录状态。
    * 访问所有收费接口采用用户的sessionKey，请求body中加入“via”项，值为本运营收款fid，以便获取apip服务方分成。
```登录签名json
{
	"meta": "FV",
	"op": "sign",
	"data": {
		"msg": {
			"url": "https://cid.cash/explorer/signin",
			"nonce": 0,
			"time": 0,
			"pubKey": ""
		},
		"sign": ""
	}
}
```

```收费访问json
{
	"url": "https://cid.cash/APIP/apip3/v1/cidInfoByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":[<fid>]
    }
}
```

```购买服务的opReturn json
{
  "type": "APIP",
  "sn": "1",
  "ver": "1",
  "name": "OpenAPI",
  "data": {
    "op": "buy",
    "sid": <string. apip服务的sid>,
    "via":"FSbkZGGZi4SRprgUTNu7LQUwxM9Yujcash"
  }
}

```
[ ] 发布加密算法code
[ ] 命名空间
[ ] FEIP类中增加了did，待测试
[ ] 发布service，测试sessionDays


# 会议讨论
1. 密码算法
2. 头像升级
# 已测试
[x] 主控身份加上加密私钥
[x] 密信加上单独密文
[x] 相关FEIP操作是否要1币天？？？要！200万高度之后。
[x] contact，mail，group，team等都需要返回cid。:不返回了，再次调用接口获取，本地缓存。
[x] ConfigBase 122
[x] EsTools 342: sort, height,index.
[pending] 数据请求与响应的签名改为AES加密
[x] 系列get接口：getServices，getApps，getAvater，getCidFid，getCashes
[x] connect改为signIn
[x] id或addr改为fid
[x] txid改为txId
[x] connect：pubkey 移入body做测试
[x] weight: FCH-writeEs-makeAddress;FEIP-reputationParser; cidSearch; cidAddrByIds
[x] rate service
[x] weight的查询
[x] 测试p2sh Parser
[x] 恢复请求检查
[x] 恢复order和mempool
[x] cash field names
[x] nobody for abandon
[x] 测试cid注销
[x] 测试未确认交易cash解析
[x] 过滤cash时，cash的id不同于order的id（txid）。at Test.orderTest.filterValidOrderList(orderTest.java:129)
[x] redis 设置avatar路径
[x] 设置rpc用户名ip和port
[x] APIP-manager加入set nPrice
[x] APIP-manager的通用类转出去
[x] 修改fchParser的Address的utxo为cash
[x] Address的balance和utxo为负值问题
[x] cidInfo 添加weight
[x] FEIP协议增加proof
[x] FEIP1加入保留ID声明，aid,bid,cid,did,gid,pid,sid,tid
```
{
    "id": "FTJWhn9NfsrPtoux6ZHgAjPieCgPLKuB6d",
    "balance": -100809000000,
    "utxo": -1,
}
```
[x] cid的script转移到p2sh
[x] cidHistory的nodiceFee 由double改为String；
# 测试顺序

[x] Hello world

[x] java class

[x] servlet

[x] redis client

[x] 导入FEIP-Parser

[x] es client

[x] servlet init with redis and es

[x] jersey。放弃。

[x] @WebServlet for connectAPI

[x] post. json in and out.

[x] sort json

[x] create order index

[x] 转义符方法工具化

[x] connectAPI

