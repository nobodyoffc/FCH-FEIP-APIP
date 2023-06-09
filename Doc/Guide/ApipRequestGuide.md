```
Name: ApipRequestGuide
Version: 1.0.0
Description : A simple guide for APIP service customers 
Authors: C_armX
Language: en-US
CreateDate: 2023-06-09
UpdateDate: 2023-06-09
```

# ApipRequestGuide

## Free GET APIs:

[1. Get cid or fid information](https://cid.cash/APIP/freeGet/v1/getFidCid?id=FEk41Kqjar45fLDriztUDTUkdki7mmcjWK)

[2. Get avatar of a fid](https://cid.cash/APIP/freeGet/v1/getAvatar?fid=FEk41Kqjar45fLDriztUDTUkdki7mmcjWK)

[3. Get cashes(UTXOs) of a fid](https://cid.cash/APIP/freeGet/v1/getCashes?fid=FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX)

[4. Get service list](https://cid.cash/APIP/freeGet/v1/getServices)

[5. Get App list](https://cid.cash/APIP/freeGet/v1/getApps)

[6. Get the count of entries of all indices](https://cid.cash/APIP/freeGet/v1/getAllSums)

APIs above can be requested for free. 76 POST APIs need that the requester paid and signed in the service before to be requested.

## Buy service and SignIn:

### Before requesting POST APIs for data, you have to:
	(1) Buy service: send more than 1 fch to `FUmo2eez6VK2sfGWjek9i9aK5y1mdHSnqv` with below JSON attatched:
```
{
  "type": "APIP",
  "sn": "1",
  "ver": "1",
  "name": "OpenAPI",
  "pid": "",
  "data": {
    "op": "buy",
    "sid": "46c1df926598cf0b881f0f1ab2ac6340826a5f954dd690786459c36388d6c131"
  }
}
```
	(2) SignIn: POST https://cid.cash/APIP/apip1/v1/signIn
		
		* Request header:
```
		Sign = < The signature of `Request body` with the private Key of the pubKey in Request body also of the payer of the service. The sign algorithm is the same as in Bitcoin-Qt>
```
			Signature example:
```
	pubKey(hex) = 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
	priKey(base58check) = L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
	priKey(hex) = a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575
	message = {"data":"test"}
	Sign(Base64) = IMNLeiyEj2JA6nU04Tj/7rQoSokP2r+Ber5S3bXhsXJjc8uqgNnagwpBadJx45LFWd+9kKKgjP6/WmeDbckqXCw=
```

		* Request body:
```
{
	"url": "https://cid.cash/APIP/apip1/v1/signIn",
	"pubKey": <string. the public key of the fch address used when you baid this service>,
	"nonce": <int. random int>,
	"time": <long. timestamp in millisecond when you are requesting>
}
```
		* Response header:
```
		Code = <The same code as in request body, but in string>.
```

		* Response body:
```
{
	"code": 0,
	"message": "Success.",
	"balance": <long. your balance in satoshi, such as 2342800000>,
	"nonce": <int. the same nonce as in your request body>,
	"data": {
		"sessionKeyEncrypted": <string. the ciphertext of a 32 bytes session key in hex encrypted with the pubkey in your request body>,
		"sessionDays": <int. the days the sessionKey will be valid>
	}
}
```
			- Encrypt and decrypt with the [Code of AES algorithm in java](https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/AesEcc/AES256.java)
			- 示例:
```
		plain text(utf-8) = {"data":"test"}
		symKey(hex) = 7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08
		ciphertext(Base64) = qmlLu07UZb7lnzWC4F9Yrg==
```

There may also be some error codes and messeges if your request is unqualified.
See [APIP1V1_OpenAPI(zh-CN)](https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/Doc/APIP-document/APIP1V1_OpenAPI(zh-CN).md)

### Request POST API

	* URL: POST https://cid.cash/APIP/apip1/v1/<API name>
	* Request header:
```
	Sign = < The sha256x2 value of the bytes of `Request body` with your sessionKey added to the end>
	
```
		Signature example:
```
	data = {"name":"test"}
	sessionKey = 7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08
	Sign = 758298ca268bffa33e2d8d4e220c1d97a4c7be708026e9bc11102cc4a70d134c
```

	* Request body

```
{
	"url": <string. https://cid.cash/APIP/apip1/v1/<API name>. required>,
	"time": <long. timestamp in millisecond when you are requesting>,
	"nonce": <int. random int>,
    "fcdsl":{
        <object. Simplified Query languge for FreeConsensus based on ElasticSearch DSL. See [APIP1V1_OpenAPI(zh-CN)](https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/Doc/APIP-document/APIP1V1_OpenAPI(zh-CN).md)>
    }
}
```
	However, most APIs can be requested without "fcdsl" to list first 20 items in the default sort.
	
	You can build your own FCH parser, FEIP parser, and APIP service. See 
	
	* Response header:
```
		Code = <The same code as in request body, but in string>.
		Sign = < The sha256x2 value of the bytes of `Response body` with your sessionKey added to the end>
```

	* Response body:
```
{
	"code": 0,
	"message": "Success.",
	"balance": <long. your balance in satoshi, such as 2342800000>,
	"nonce": <int. the same nonce as in your request body>,
	"got": <int. how many entries of data in this response>,
	"total": <int. total hits of your request>,
	"bestHeight": <long. the best block height when the server makes this reponse>,
	"data": {
		<the data you want>
	}
}
```
## POST API list

1. signIn	// signIn to get sessionKey.
2. general	// for all fcdsl queries on a given ES index.
3. blockByIds
4. blockSearch
5. cashByIds
6. cashSearch
7. TxHasByIds
8. cashValid
9. txByIds
10. txSearch
11. blockHasByIds
12. opReturnByIds
13. opReturnSearch
14. addressByIds
15. addressSearch
16. cidInfoByIds
17. fidCidSeek
18. cidInfoSearch
19. usedCid
20. cidHistory
21. homepageHistory
22. noticeFeeHistory
23. reputationHistory
24. p2shByIds
25. p2shSearch
26. protocolByIds
27. protocolSearch
28. protocolOpHistory
29. protocolRateHistory
30. codeByIds
31. codeSearch
32. codeOpHistory
33. codeRateHistory
34. serviceByIds
35. serviceSearch
36. serviceOpHistory
37. serviceRateHistory
38. appByIds
39. appSearch
40. appOpHistory
41. appRateHistory
42. groupByIds
43. groupSearch
44. groupOpHistory
45. groupMembers
46. groupExMembers
47. myGroups
48. teamByIds
49. teamSearch
50. teamOpHistory
51. teamMembers
52. teamExMembers
53. teamOtherPersons
54. myTeams
55. teamRateHistory
56. contacts
57. contactsDeleted
58. secrets
59. secretsDeleted
60. mails
61. mailsDeleted
62. mailThread
63. statements
64. statementSearch
65. proofByIds
66. proofSearch
67. proofHistory
68. boxByIds
69. boxSearch
70. boxHistory
71. unconfirmed
72. broadcastTx
73. decodeRawTx
74. cashValidLive
75. avatars
76. cashValidForCd
77. cashValidForPay

## Indices in ES

1. block
2. block_has
3. tx
4. tx_has
5. cash
6. opreturn
7. address
8. p2sh
9. block_mark
10. cid
11. cid_history
12. reputation_history
13. protocol
14. code
15. service
16. app
17. protocol_history
18. code_history
19. service_history
20. app_history
21. contact
22. mail
23. secret
24. box
25. box_history
26. group
27. team
28. group_history
29. team_history
30. statement
31. proof
32. proof_history
