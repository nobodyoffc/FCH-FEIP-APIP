# 备忘任务

* CcManager
  * order scanner
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

