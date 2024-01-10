package constants;

import java.util.ArrayList;

public class ApiNames {
    public static String[] OpenAPIs;
    public static String[] FreeGetAPIs;
    public static String[] BlockchainAPIs;
    public static String[] IdentityAPIs;
    public static String[] OrganizeAPIs;
    public static String[] ConstructAPIs;
    public static String[] PersonalAPIs;
    public static String[] PublishAPIs;
    public static String[] WalletAPIs;
    public static String[] CryptoToolsAPIs;
    //APIP path
    public static final String APIP0V1Path = "/apip0/v1/";
    public static final String APIP1V1Path = "/apip1/v1/";
    public static final String APIP2V1Path = "/apip2/v1/";
    public static final String APIP3V1Path = "/apip3/v1/";
    public static final String APIP4V1Path = "/apip4/v1/";
    public static final String APIP5V1Path = "/apip5/v1/";
    public static final String APIP6V1Path = "/apip6/v1/";
    public static final String APIP7V1Path = "/apip7/v1/";
    public static final String APIP8V1Path = "/apip8/v1/";
    public static final String APIP9V1Path = "/apip9/v1/";
    public static final String APIP10V1Path = "/apip10/v1/";
    public static final String APIP11V1Path = "/apip11/v1/";
    public static final String APIP12V1Path = "/apip12/v1/";
    public static final String APIP13V1Path = "/apip13/v1/";
    public static final String APIP14V1Path = "/apip14/v1/";
    public static final String APIP15V1Path = "/apip15/v1/";
    public static final String APIP16V1Path = "/apip16/v1/";
    public static final String APIP17V1Path = "/apip17/v1/";
    public static final String APIP18V1Path = "/apip18/v1/";
    public static final String APIP19V1Path = "/apip19/v1/";
    public static final String APIP20V1Path = "/apip20/v1/";
    public static final String APIP21V1Path = "/apip21/v1/";
    public static final String FreeGetPath = "/freeGet/v1/";
    public static final String ToolsPath = "/tools/";
    public static final String SignInAPI = "signIn";
    public static final String SignInEccAPI = "signInEcc";
    public static final String GeneralAPI = "general";
    public static final String TotalsAPI = "totals";
    public static final String BlockByIdsAPI = "blockByIds";
    public static final String BlockSearchAPI = "blockSearch";
    public static final String BlockByHeightsAPI = "blockByHeights";
    public static final String CashByIdsAPI = "cashByIds";
    public static final String GetCashesAPI = "getCashes";
    public static final String CashSearchAPI = "cashSearch";
    public static final String TxByIdsAPI = "txByIds";
    public static final String TxSearchAPI = "txSearch";
    public static final String OpReturnByIdsAPI = "opReturnByIds";
    public static final String OpReturnSearchAPI = "opReturnSearch";
    public static final String UnconfirmedAPI = "unconfirmed";
    public static final String BlockHasByIdsAPI = "blockHasByIds";
    public static final String TxHasByIdsAPI = "TxHasByIds";
    public static final String CashValidAPI = "cashValid";
    public static final String FidByIdsAPI = "fidByIds";
    public static final String CidInfoByIdsAPI = "cidInfoByIds";
    public static final String GetFidCidAPI = "getFidCid";
    public static final String FidSearchAPI = "fidSearch";
    public static final String FidCidSeekAPI = "fidCidSeek";
    public static final String CidByIdsAPI = "cidByIds";
    public static final String CidInfoSearchAPI = "cidInfoSearch";
    public static final String CidSearchAPI = "cidSearch";
    public static final String CidHistoryAPI = "cidHistory";
    public static final String HomepageHistoryAPI = "homepageHistory";
    public static final String NoticeFeeHistoryAPI = "noticeFeeHistory";
    public static final String ReputationHistoryAPI = "reputationHistory";
    public static final String NobodySearchAPI = "nobodySearch";
    public static final String P2shByIdsAPI = "p2shByIds";
    public static final String P2shSearchAPI = "p2shSearch";
    public static final String ProtocolByIdsAPI = "protocolByIds";
    public static final String ProtocolSearchAPI = "protocolSearch";
    public static final String ProtocolOpHistoryAPI = "protocolOpHistory";
    public static final String ProtocolRateHistoryAPI = "protocolRateHistory";
    public static final String CodeByIdsAPI = "codeByIds";
    public static final String CodeSearchAPI = "codeSearch";
    public static final String CodeOpHistoryAPI = "codeOpHistory";
    public static final String CodeRateHistoryAPI = "codeRateHistory";
    public static final String ServiceByIdsAPI = "serviceByIds";
    public static final String GetBestBlockAPI = "getBestBlock";
    public static final String GetServicesAPI = "getServices";
    public static final String GetServiceAPI = "getService";
    public static final String GetFreeServiceAPI = "getFreeService";
    public static final String ServiceSearchAPI = "serviceSearch";
    public static final String ServiceOpHistoryAPI = "serviceOpHistory";
    public static final String ServiceRateHistoryAPI = "serviceRateHistory";
    public static final String AppByIdsAPI = "appByIds";
    public static final String GetAppsAPI = "getApps";
    public static final String AppSearchAPI = "appSearch";
    public static final String AppOpHistoryAPI = "appOpHistory";
    public static final String AppRateHistoryAPI = "appRateHistory";
    public static final String GroupByIdsAPI = "groupByIds";
    public static final String GroupSearchAPI = "groupSearch";
    public static final String GroupMembersAPI = "groupMembers";
    public static final String GroupExMembersAPI = "groupExMembers";
    public static final String MyGroupsAPI = "myGroups";
    public static final String GroupOpHistoryAPI = "groupOpHistory";
    public static final String TeamByIdsAPI = "teamByIds";
    public static final String TeamSearchAPI = "teamSearch";
    public static final String TeamMembersAPI = "teamMembers";
    public static final String TeamExMembersAPI = "teamExMembers";
    public static final String TeamOtherPersonsAPI = "teamOtherPersons";
    public static final String MyTeamsAPI = "myTeams";
    public static final String TeamOpHistoryAPI = "teamOpHistory";
    public static final String TeamRateHistoryAPI = "teamRateHistory";
    public static final String ContactByIdsAPI = "contactByIds";
    public static final String ContactsAPI = "contacts";
    public static final String ContactsDeletedAPI = "contactsDeleted";
    public static final String SecretByIdsAPI = "secretByIds";
    public static final String SecretsAPI = "secrets";
    public static final String SecretsDeletedAPI = "secretsDeleted";
    public static final String MailByIdsAPI = "mailByIds";
    public static final String MailsAPI = "mails";
    public static final String MailsDeletedAPI = "mailsDeleted";
    public static final String MailThreadAPI = "mailThread";
    public static final String StatementByIdsAPI = "statementByIds";
    public static final String StatementsAPI = "statements";
    public static final String StatementSearchAPI = "statementSearch";
    public static final String ProofByIdsAPI = "proofByIds";
    public static final String ProofSearchAPI = "proofSearch";
    public static final String ProofHistoryAPI = "proofHistory";
    public static final String BoxByIdsAPI = "boxByIds";
    public static final String BoxSearchAPI = "boxSearch";
    public static final String BoxHistoryAPI = "boxHistory";
    public static final String AvatarsAPI = "avatars";
    public static final String GetAvatarAPI = "getAvatar";
    public static final String DecodeRawTxAPI = "decodeRawTx";
    public static final String BroadcastTxAPI = "broadcastTx";
    public static final String BroadcastAPI = "broadcast";
    public static final String CashValidLiveAPI = "cashValidLive";
    public static final String CashValidForCdAPI = "cashValidForCd";
    public static final String CashValidForPayAPI = "cashValidForPay";
    public static final String GetTotalsAPI = "getTotals";
    public static final String GetPricesAPI = "getPrices";
    public static final String NidSearchAPI = "nidSearch";
    public static final String EncryptAPI = "encrypt";
    public static final String Sha256API = "sha256";
    public static final String Sha256x2API = "sha256x2";
    public static final String Sha256BytesAPI = "sha256Bytes";
    public static final String Sha256x2BytesAPI = "sha256x2Bytes";
    public static final String VerifyAPI = "verify";
    public static final String OffLineTxAPI = "offLineTx";
    public static final String OffLineTxByCdAPI = "offLineTxByCd";
    public static final String AddressesAPI = "addresses";
    public static final String NewCashByFidsAPI = "newCashByFids";
    public static final String NobodyByIdsAPI ="nobodyByIds";

    public static ArrayList<String> freeApiList = new ArrayList<>();
    public static final String GetFreeKeyAPI="getFreeKey";


    static {

        OpenAPIs = new String[]{
                GetServiceAPI,SignInAPI,SignInEccAPI,TotalsAPI,GeneralAPI
        };

        BlockchainAPIs = new String[]{
                BlockByIdsAPI, BlockSearchAPI, BlockByHeightsAPI,
                CashByIdsAPI, CashSearchAPI,CashValidAPI,
                FidByIdsAPI,FidSearchAPI,
                OpReturnByIdsAPI,OpReturnSearchAPI,
                P2shByIdsAPI,P2shSearchAPI,
                TxByIdsAPI,TxSearchAPI
        };

        IdentityAPIs = new String[]{
                CidInfoByIdsAPI,CidInfoSearchAPI,CidHistoryAPI,
                FidCidSeekAPI,GetFidCidAPI,
                NobodyByIdsAPI, NobodySearchAPI,
                HomepageHistoryAPI,NoticeFeeHistoryAPI,ReputationHistoryAPI,
                AvatarsAPI,GetAvatarAPI
        };

        OrganizeAPIs = new String[]{
                GroupByIdsAPI,GroupSearchAPI,GroupMembersAPI,GroupOpHistoryAPI,MyGroupsAPI,
                TeamByIdsAPI,TeamSearchAPI,TeamMembersAPI,TeamExMembersAPI,
                TeamOpHistoryAPI,TeamRateHistoryAPI,TeamOtherPersonsAPI,MyTeamsAPI
        };

        ConstructAPIs = new String[]{
                ProtocolByIdsAPI,ProtocolSearchAPI,ProtocolOpHistoryAPI,ProtocolRateHistoryAPI,
                CodeByIdsAPI,CodeSearchAPI,CodeOpHistoryAPI,CodeRateHistoryAPI,
                ServiceByIdsAPI,ServiceSearchAPI,ServiceOpHistoryAPI,ServiceRateHistoryAPI,
                AppByIdsAPI,AppSearchAPI,AppOpHistoryAPI,AppRateHistoryAPI
        };

        PersonalAPIs = new String[]{
                BoxByIdsAPI,BoxSearchAPI,BoxHistoryAPI,
                ContactByIdsAPI,ContactsAPI,ContactsDeletedAPI,
                SecretByIdsAPI,SecretsAPI,SecretsDeletedAPI,
                MailByIdsAPI,MailsAPI,MailsDeletedAPI,MailThreadAPI
        };

        PublishAPIs = new String[]{
                ProofByIdsAPI,ProofSearchAPI,ProofHistoryAPI,
                StatementByIdsAPI,StatementSearchAPI,
                NidSearchAPI
        };

        WalletAPIs = new String[]{
                BroadcastTxAPI,DecodeRawTxAPI,
                CashValidForPayAPI,CashValidForCdAPI,
                UnconfirmedAPI
        };

        CryptoToolsAPIs = new String[]{
                AddressesAPI,
                EncryptAPI,VerifyAPI,
                Sha256API,Sha256x2API,Sha256BytesAPI,Sha256x2BytesAPI,
                OffLineTxAPI,OffLineTxByCdAPI
        };

        FreeGetAPIs = new String[]{
                GetBestBlockAPI,
                BroadcastAPI,GetAppsAPI,GetServicesAPI,GetAvatarAPI,GetCashesAPI,
                GetFidCidAPI,GetFreeServiceAPI,GetServicesAPI,GetTotalsAPI,
                GetPricesAPI
        };
        freeApiList.add(ApiNames.GetBestBlockAPI);
        freeApiList.add(ApiNames.GetFreeServiceAPI);
        freeApiList.add(ApiNames.GetAvatarAPI);
        freeApiList.add(ApiNames.GetTotalsAPI);
        freeApiList.add(ApiNames.GetPricesAPI);
        freeApiList.add(ApiNames.GetAppsAPI);
        freeApiList.add(ApiNames.GetCashesAPI);
        freeApiList.add(ApiNames.GetFidCidAPI);
        freeApiList.add(ApiNames.GetServicesAPI);
    }

    public static ArrayList<String> apiList = new ArrayList<String>();

    static {

        ApiNames.apiList.add(ApiNames.SignInAPI);
        ApiNames.apiList.add(ApiNames.SignInEccAPI);
        ApiNames.apiList.add(ApiNames.GeneralAPI);
        ApiNames.apiList.add(ApiNames.TotalsAPI);

        ApiNames.apiList.add(ApiNames.BlockByIdsAPI);
        ApiNames.apiList.add(ApiNames.BlockSearchAPI);
        ApiNames.apiList.add(ApiNames.BlockByHeightsAPI);
        ApiNames.apiList.add(ApiNames.CashByIdsAPI);
        ApiNames.apiList.add(ApiNames.CashSearchAPI);
        ApiNames.apiList.add(ApiNames.TxHasByIdsAPI);
        ApiNames.apiList.add(ApiNames.CashValidAPI);
        ApiNames.apiList.add(ApiNames.TxByIdsAPI);
        ApiNames.apiList.add(ApiNames.TxSearchAPI);
        ApiNames.apiList.add(ApiNames.BlockHasByIdsAPI);
        ApiNames.apiList.add(ApiNames.OpReturnByIdsAPI);
        ApiNames.apiList.add(ApiNames.OpReturnSearchAPI);
        ApiNames.apiList.add(ApiNames.FidByIdsAPI);
        ApiNames.apiList.add(ApiNames.FidSearchAPI);
        ApiNames.apiList.add(ApiNames.P2shByIdsAPI);
        ApiNames.apiList.add(ApiNames.P2shSearchAPI);

        ApiNames.apiList.add(ApiNames.CidInfoByIdsAPI);
        ApiNames.apiList.add(ApiNames.CidByIdsAPI);
        ApiNames.apiList.add(ApiNames.FidCidSeekAPI);
        ApiNames.apiList.add(ApiNames.CidInfoSearchAPI);
        ApiNames.apiList.add(ApiNames.CidHistoryAPI);
        ApiNames.apiList.add(ApiNames.HomepageHistoryAPI);
        ApiNames.apiList.add(ApiNames.NoticeFeeHistoryAPI);
        ApiNames.apiList.add(ApiNames.ReputationHistoryAPI);
        ApiNames.apiList.add(ApiNames.NobodySearchAPI);
        ApiNames.apiList.add(ApiNames.NobodyByIdsAPI);

        ApiNames.apiList.add(ApiNames.ProtocolByIdsAPI);
        ApiNames.apiList.add(ApiNames.ProtocolSearchAPI);
        ApiNames.apiList.add(ApiNames.ProtocolOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.ProtocolRateHistoryAPI);

        ApiNames.apiList.add(ApiNames.CodeByIdsAPI);
        ApiNames.apiList.add(ApiNames.CodeSearchAPI);
        ApiNames.apiList.add(ApiNames.CodeOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.CodeRateHistoryAPI);

        ApiNames.apiList.add(ApiNames.ServiceByIdsAPI);
        ApiNames.apiList.add(ApiNames.ServiceSearchAPI);
        ApiNames.apiList.add(ApiNames.ServiceOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.ServiceRateHistoryAPI);

        ApiNames.apiList.add(ApiNames.AppByIdsAPI);
        ApiNames.apiList.add(ApiNames.AppSearchAPI);
        ApiNames.apiList.add(ApiNames.AppOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.AppRateHistoryAPI);

        ApiNames.apiList.add(ApiNames.GroupByIdsAPI);
        ApiNames.apiList.add(ApiNames.GroupSearchAPI);
        ApiNames.apiList.add(ApiNames.GroupOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.GroupMembersAPI);
        ApiNames.apiList.add(ApiNames.GroupExMembersAPI);
        ApiNames.apiList.add(ApiNames.MyGroupsAPI);

        ApiNames.apiList.add(ApiNames.TeamByIdsAPI);
        ApiNames.apiList.add(ApiNames.TeamSearchAPI);
        ApiNames.apiList.add(ApiNames.TeamOpHistoryAPI);
        ApiNames.apiList.add(ApiNames.TeamMembersAPI);
        ApiNames.apiList.add(ApiNames.TeamExMembersAPI);
        ApiNames.apiList.add(ApiNames.TeamOtherPersonsAPI);
        ApiNames.apiList.add(ApiNames.MyTeamsAPI);
        ApiNames.apiList.add(ApiNames.TeamRateHistoryAPI);

        ApiNames.apiList.add(ApiNames.BoxByIdsAPI);
        ApiNames.apiList.add(ApiNames.BoxSearchAPI);
        ApiNames.apiList.add(ApiNames.BoxHistoryAPI);

        ApiNames.apiList.add(ApiNames.ContactByIdsAPI);
        ApiNames.apiList.add(ApiNames.ContactsAPI);
        ApiNames.apiList.add(ApiNames.ContactsDeletedAPI);

        ApiNames.apiList.add(ApiNames.SecretByIdsAPI);
        ApiNames.apiList.add(ApiNames.SecretsAPI);
        ApiNames.apiList.add(ApiNames.SecretsDeletedAPI);

        ApiNames.apiList.add(ApiNames.MailByIdsAPI);
        ApiNames.apiList.add(ApiNames.MailsAPI);
        ApiNames.apiList.add(ApiNames.MailsDeletedAPI);
        ApiNames.apiList.add(ApiNames.MailThreadAPI);

        ApiNames.apiList.add(ApiNames.ProofByIdsAPI);
        ApiNames.apiList.add(ApiNames.ProofSearchAPI);
        ApiNames.apiList.add(ApiNames.ProofHistoryAPI);

        ApiNames.apiList.add(ApiNames.StatementByIdsAPI);
        ApiNames.apiList.add(ApiNames.StatementsAPI);
        ApiNames.apiList.add(ApiNames.StatementSearchAPI);
        ApiNames.apiList.add(ApiNames.NidSearchAPI);

        ApiNames.apiList.add(ApiNames.AvatarsAPI);

        ApiNames.apiList.add(ApiNames.UnconfirmedAPI);
        ApiNames.apiList.add(ApiNames.CashValidLiveAPI);
        ApiNames.apiList.add(ApiNames.CashValidForCdAPI);
        ApiNames.apiList.add(ApiNames.CashValidForPayAPI);
        ApiNames.apiList.add(ApiNames.DecodeRawTxAPI);
        ApiNames.apiList.add(ApiNames.BroadcastTxAPI);

        ApiNames.apiList.add(ApiNames.OffLineTxAPI);
        ApiNames.apiList.add(ApiNames.OffLineTxByCdAPI);
        ApiNames.apiList.add(ApiNames.EncryptAPI);
        ApiNames.apiList.add(ApiNames.VerifyAPI);
        ApiNames.apiList.add(ApiNames.AddressesAPI);
        ApiNames.apiList.add(ApiNames.Sha256API);
        ApiNames.apiList.add(ApiNames.Sha256x2API);
        ApiNames.apiList.add(ApiNames.Sha256BytesAPI);
        ApiNames.apiList.add(ApiNames.Sha256x2BytesAPI);

        ApiNames.apiList.add(ApiNames.NewCashByFidsAPI);
    }
}
