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

# FIPA1V1_Freeverse

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

## FVE Entity

eid：entity identify

* Entity: Information
  * Subject: Key
  * Object: Data

## FVI 身份

* type
  * digitalId: hash of entity
  * humanReadableId: Named by human

* EID
	* FID
		* CID
          * guide
          * master
          * messenger
          * waiter
          * maker
          * teller
          * dealer
          * ...
	* OID
		* DID（DataID）
        * RID（ReferredID）:
          * bid
          * aid
          * pid
          * sid
          * ...
        * NID
        * IID 
          * #cidByIds/Request-body@(pid)00000000000
          * #ECC256/encrypt/String-String-String@(codeId)00000000



## FVN  名称 可读身份

    CID
    NID
    IID

cid或did均可定义自己的namepace
did内部如果定义了多级标题，则所有标题构成该did的namespace

## FVL 定位 Location

通用定位————> fc://fci or fcn/sublocation/。。。

fcl是eid或通过eid的命名空间可以得到的元素位置。

增加feip协议 namespace

对FVN和FVL中的`@`, `:`, `/`, `_`，`#`使用转义字符 `\` .

## 关系符

    DID = sha256(sha256(DigitData)) = sha256x2(DigitData)

    # 位置标记符: #A，A指向某数据的特定内部位置。
	@ 命名符：A@B，A在B的命名空间内。
	/ 定位符：A/B，B在A的命名空间内。
	() 转型符：(pid)3243,将did3243转换为pid类型。
	\ 转义符：之后的1个字符不解释为关系符
	
	关系解析由左至右
	\优先于#优先于()优先于@优先于/：big\@dog@(cid)B/C@D = (cid)B/big\@dog/D/C = C@D@big\@dog@(cid)B。 A/(cid)B@C/D = A/C/cid#B/D = D@cid#B@C@A
	方法：
	1）#两端保持不变
	2）将所有@两端对换，得到全/
	3）将全/逆序，得到全@

保留id名称，aid，bid，cid，did，eid，fid，gid，nid, pid，sid，tid