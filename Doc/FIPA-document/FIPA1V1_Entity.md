```
ProtocolType: FIPA
SerialNumber: 1
ProtocolName: Entity
Version: 1
Description : Define bisic entities of Free Consensus 
Authors: C_armX
Language: en-US
CreateDate: 2023-05-12
UpdateDate: 2023-05-07
```

# FIPA1V1_Entity

## Contents

[Consensuses of FIPA](#consensuses-of-fipa)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Publish](#publish)

[Update](#update)

[Stop](#stop)

[Recover](#recover)

[Close](#close)

[Rate](#rate)

[QR code](#qr-code)

## Consensuses of FIPA

1. FIPA (Freecash Improvement Protocols for Assist) make improvements to the FCH ecosystem. 

2. To implement FIPA will not lead to a hard fork of the main network of Freecash. 

3. Actions appling these protocols do not write data in OP_RETURN.

4. It is strongly recommend to implement FIPA protocols in Free Consensus society.

## Consensuses of this protocol

## Entity

## ID

eid：entity identify
txid---txId
通用身份————> subject: fid（free identity. object: did (sha256x2(data))

## Name

通用名称
	subject:<cid>
	object: 
		- <object name>@cid（cid命名和解析，应指向唯一did）。e.g. FCH-Parser@cid
		- cid/<object name>

cid或did均可定义自己的namepace

## Location

通用定位————> fc://fci or fcn/sublocation/。。。

fcl是eid或通过eid的命名空间可以得到的元素位置。

增加feip协议 namespace

`@`, `:`, `/`, or `_`


## 关系符
	# 类型符：pid#3243，3243的类型为pid。可省略。
	@ 命名符：A@B，A在B的命名空间内。
	/ 定位符：A/B，B在A的命名空间内。
	\ 转义符：之后的1个字符不解释为关系符
	
	关系解析由左至右
	\优先于#优先于@优先于/：A@cid#B/C@D = cid#B/A/D/C or C@D@A@cid#B。 A/cid#B@C/D = A/C/cid#B/D or D@cid#B@C@A
	方法：
	1）#两端保持不变
	2）将所有@两端对换，得到全/
	3）将全/逆序，得到全@

保留id名称，aid，bid，cid，did，eid，fid，gid，pid，sid，tid