```
FIPA8: AES Algorithm
Version: 1
Language: zh-CN
Author: C_armX
Status: draft

```

# FIPA8_AesAlgorithm(en-US)

## AES-ECC数字信封协议文档

### 概述

AES-ECC数字信封加密算法库，是基于AES256，以及椭圆曲线secp256k1实现的对任意字节序列实现数字信封加密的算法库。

对数据使用AES对称密钥$key$进行加密，再通过椭圆曲线算法，使用公钥$pubKey$加密$key$，实现数据加密。解密过程与之相反，通过公钥对应的私钥$priKey$解密，得到对称密钥$key$，再通过$key$解密得到原始数据。

### AES算法说明

AES(Advanced Encryption Standard，高级加密标准)，是一种常见的加密算法，采用对称分组密码体制。本算法库采用的是AES-256-CBC-PKCS7Padding加密方式，此种方式下加解密需要一个16字节的初始化向量(Initialzation Vector, IV)，同时密钥的长度为256位，即32字节；并采用CBC模式加密，即先将明文切分成若干个小段，每一个小段与初始块或上一段的密文进行异或运算后，再与密钥进行加密；采用PKCS7Padding填充模式，保证明文分段时，对于数据不齐时，对数据按字节进行补齐，若数据本身已经对齐，则会填充一块长度为块大小的数据，每个字节都是块大小。

算法具体描述如下：

**明文分段**

对于任意长度的明文$Plaintext$，首先根据PKCS7Padding进行补齐，补齐后将数据分段，每块大小为16字节。分段后得到n个明文段，即$Plaintext_1,...,Plaintext_n$。

**加密数据**

1. $Plaintext_1$与IV进行异或运算，得到$Input_1$做为AES加密算法的输入，与密钥$key$进行计算，得到密文$Ciphertext_1$。
2. $Plaintext_2$与$Ciphertext_1$进行异或运算，得到$Input_2$做为AES加密算法的输入，与密钥$key$进行计算，得到密文$Ciphertext_2$。
3. 循环往复，直至将所有的明文段加密完，最后一次得到密文$Ciphertext_n$。
4. 将$Ciphertext_1，...,Ciphertext_n$拼接在一起，即为最终的密文$Ciphertext$。

![加密AES-256-CBC-PKCS7Pandding](https://cdn.jsdelivr.net/gh/jzhchu/picture-for-md/20211016225358.png)

**解密数据**

1. 将$Ciphertext$按16字节，划分成n个密文段$Ciphertext_1,...,Ciphertext_n$。
2. $Ciphertext_1$与密钥$key$进行解密运算，得$Output_1$，$Output_1$与IV进行异或，得到第一段明文$Plaintext_1$。
3. $Ciphertext_2$与密钥$key$进行解密运算，得到$Output_2$，$Output_2$与$Ciphertext_1$进行异或，得到第二段明文$Plaintext_2$。
4. 循环往复，即可得到左右的明文段，最后将$Plaintext_1,...,Plaintext_n$拼接在一起，即可得到最终的明文$Plaintext$。

![解密AES-256-CBC-PKCS7Pandding](https://cdn.jsdelivr.net/gh/jzhchu/picture-for-md/20211016225435.png)



### ECC算法说明

ECC(Elliptic Curve Cryptography，椭圆曲线密码学)，基于有限域的椭圆曲线和复杂的椭圆曲线离散对数，实现的一种非对称加密系统。

在本算法库中的椭圆曲线加密算法是基于secp256k1实现的。 大多数常用的曲线都具有随机结构，但 secp256k1 是以特殊的非随机方式构建的，因此可以实现特别高效的计算。因此，如果实现充分优化，它通常比其他曲线快 30% 以上。

而本算法库所实现的是基于椭圆曲线的一种集成加密方案，ECIES(elliptic curve integrate encrypt scheme)。其中密钥派生函数、校验码的生成算法和对称加密方案是根据需要设计的，本算法库选择的密钥派生函数为SHA512哈希函数，校验码生成算法为SHA256哈希函数，对称加密方案选择的是AES/CBC/PKCS7Padding。

**密钥生成**

在椭圆曲线$Ep(a,b)$中选择两个点$G,K$，满足$K=kG$，$n$为$G$的阶$(nG=O_{\infty})$，$k$为小于$n$的整数。则给定$k$和$G$，计算$K$是很容易的，而给定$K$和$G$计算$k$是非常困难的，同时由于$n,p$的取值是非常大的，因此想要把各个点逐一算出是不可能的，基于此构建非对称密码算法。

1. 在椭圆曲线上随机选取一点$G$，称为基点。
2. $k(k<n)$为私钥。
3. $K$为公钥。

在使用椭圆曲线进行加密时，用户需要公开自己的公钥$K$，其他人可以通过公钥加密明文数据得到密文发送给用户，而用户可以通过自己的私钥$k$进行解密。而其他人在不知道私钥的情况下是无法解密的。

**加密算法**

为加密信息$m$，需要以下步骤：

1. 生成一个随机数$r\in[1,n-1]$，并计算$R=rG$。
2. 将秘密$S$映射到椭圆曲线上，令$S=P_x$，其中$P=(P_x, P_y)=rK(P\neq O_{\infty})$。
3. 使用哈希算法SHA512，对秘密$S$进行哈希运算，得到的值分别做为对称加密的密钥以及校验密文是否正确的密钥，$k_E||k_M=\mathsf{SHA512}(S)$。
4. 对秘密进行加密$c=E(k_E;m)$
5. 计算加密信息的校验码$d=\mathsf{SHA256}(k_M;c)$
6. 输出$R||c||d$。

**解密算法**

当获得$R||c||d$后执行解密：

1. 求出$P=(P_x,P_y)=kR$，因为$P=kR=krG=rkG=rK$，其中$S=P_x$。
2. 生成加密密钥和校验密钥$k_E||k_M=\mathsf{SHA512}(S)$。
3. 计算校验码是否正确，即$d$与$\mathsf{SHA256}(k_M;c)$。
4. 使用对称密钥进行解密$m=D(k_E;c)$。



